package com.autodev.course.service.impl;

import com.autodev.course.dto.ClassRoomDTO;
import com.autodev.course.entity.ClassRoom;
import com.autodev.course.entity.Course;
import com.autodev.course.entity.StudentCourse;
import com.autodev.course.repository.ClassRoomRepository;
import com.autodev.course.repository.CourseRepository;
import com.autodev.course.repository.StudentCourseRepository;
import com.autodev.course.service.EnrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollServiceImpl implements EnrollService {

    private final StudentCourseRepository studentCourseRepo;
    private final ClassRoomRepository classRoomRepo;
    private final CourseRepository courseRepo;

    @Override
    @Transactional
    public void enroll(Long studentId, Long courseId, Long classroomId) {
        // 检查课程是否存在且已发布
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        if (course.getStatus() != Course.CourseStatus.PUBLISHED) {
            throw new RuntimeException("课程未发布，无法选课");
        }

        // 检查是否已选
        if (studentCourseRepo.existsByStudentIdAndCourseIdAndStatus(
                studentId, courseId, StudentCourse.EnrollStatus.ENROLLED)) {
            throw new RuntimeException("已选过该课程");
        }

        // 检查班级容量
        ClassRoom classRoom = classRoomRepo.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        if (classRoom.getMaxStudents() != null
                && classRoom.getCurrentStudents() >= classRoom.getMaxStudents()) {
            throw new RuntimeException("班级已满");
        }

        // 选课
        StudentCourse sc = new StudentCourse();
        sc.setStudentId(studentId);
        sc.setCourseId(courseId);
        sc.setClassroomId(classroomId);
        studentCourseRepo.save(sc);

        // 更新班级人数
        classRoom.setCurrentStudents(classRoom.getCurrentStudents() + 1);
        classRoomRepo.save(classRoom);
    }

    @Override
    @Transactional
    public void drop(Long studentId, Long courseId) {
        StudentCourse sc = studentCourseRepo.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("未选过该课程"));
        if (sc.getStatus() == StudentCourse.EnrollStatus.DROPPED) {
            throw new RuntimeException("已退课");
        }
        sc.setStatus(StudentCourse.EnrollStatus.DROPPED);
        sc.setDroppedAt(LocalDateTime.now());
        studentCourseRepo.save(sc);

        // 更新班级人数
        ClassRoom classRoom = classRoomRepo.findById(sc.getClassroomId())
                .orElse(null);
        if (classRoom != null && classRoom.getCurrentStudents() > 0) {
            classRoom.setCurrentStudents(classRoom.getCurrentStudents() - 1);
            classRoomRepo.save(classRoom);
        }
    }

    @Override
    public List<Long> getEnrolledCourseIds(Long studentId) {
        return studentCourseRepo.findByStudentIdAndStatus(studentId, StudentCourse.EnrollStatus.ENROLLED)
                .stream().map(StudentCourse::getCourseId).collect(Collectors.toList());
    }

    @Override
    public List<Long> getEnrolledStudentIds(Long courseId) {
        return studentCourseRepo.findByCourseIdAndStatus(courseId, StudentCourse.EnrollStatus.ENROLLED)
                .stream().map(StudentCourse::getStudentId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClassRoomDTO createClassRoom(ClassRoomDTO dto) {
        ClassRoom cr = new ClassRoom();
        cr.setName(dto.getName());
        cr.setDescription(dto.getDescription());
        cr.setCourseId(dto.getCourseId());
        cr.setSemester(dto.getSemester());
        cr.setMaxStudents(dto.getMaxStudents());
        cr = classRoomRepo.save(cr);
        return ClassRoomDTO.fromEntity(cr);
    }

    @Override
    public List<ClassRoomDTO> getClassRoomsByCourse(Long courseId) {
        return classRoomRepo.findByCourseId(courseId).stream()
                .map(ClassRoomDTO::fromEntity).collect(Collectors.toList());
    }
}
