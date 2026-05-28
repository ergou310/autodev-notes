package com.autodev.course.service.impl;

import com.autodev.course.dto.CourseDTO;
import com.autodev.course.entity.Course;
import com.autodev.course.repository.CourseRepository;
import com.autodev.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public CourseDTO createCourse(CourseDTO dto, Long teacherId, String teacherName) {
        Course course = new Course();
        course.setName(dto.getName());
        course.setDescription(dto.getDescription());
        course.setCategory(dto.getCategory());
        course.setTeacherId(teacherId);
        course.setTeacherName(teacherName);
        course.setCoverImage(dto.getCoverImage());
        course.setCredit(dto.getCredit());
        course = courseRepository.save(course);
        return CourseDTO.fromEntity(course);
    }

    @Override
    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        if (dto.getName() != null) course.setName(dto.getName());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getCategory() != null) course.setCategory(dto.getCategory());
        if (dto.getCoverImage() != null) course.setCoverImage(dto.getCoverImage());
        if (dto.getCredit() != null) course.setCredit(dto.getCredit());
        course = courseRepository.save(course);
        return CourseDTO.fromEntity(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("课程不存在");
        }
        courseRepository.deleteById(id);
    }

    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        return CourseDTO.fromEntity(course);
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> searchCourses(String keyword) {
        return courseRepository.findByNameContaining(keyword).stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseDTO publishCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        course.setStatus(Course.CourseStatus.PUBLISHED);
        return CourseDTO.fromEntity(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseDTO closeCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        course.setStatus(Course.CourseStatus.CLOSED);
        return CourseDTO.fromEntity(courseRepository.save(course));
    }
}
