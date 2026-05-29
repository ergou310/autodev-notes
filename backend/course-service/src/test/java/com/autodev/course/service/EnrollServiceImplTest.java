package com.autodev.course.service;

import com.autodev.course.dto.ClassRoomDTO;
import com.autodev.course.entity.ClassRoom;
import com.autodev.course.entity.Course;
import com.autodev.course.entity.StudentCourse;
import com.autodev.course.repository.ClassRoomRepository;
import com.autodev.course.repository.CourseRepository;
import com.autodev.course.repository.StudentCourseRepository;
import com.autodev.course.service.impl.EnrollServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollServiceImplTest {

    @Mock
    private StudentCourseRepository studentCourseRepo;

    @Mock
    private ClassRoomRepository classRoomRepo;

    @Mock
    private CourseRepository courseRepo;

    @InjectMocks
    private EnrollServiceImpl enrollService;

    private Course publishedCourse;
    private Course draftCourse;
    private ClassRoom classRoom;

    @BeforeEach
    void setUp() {
        publishedCourse = new Course();
        publishedCourse.setId(1L);
        publishedCourse.setName("Math 101");
        publishedCourse.setTeacherId(100L);
        publishedCourse.setStatus(Course.CourseStatus.PUBLISHED);

        draftCourse = new Course();
        draftCourse.setId(2L);
        draftCourse.setName("Draft Course");
        draftCourse.setTeacherId(100L);
        draftCourse.setStatus(Course.CourseStatus.DRAFT);

        classRoom = new ClassRoom();
        classRoom.setId(10L);
        classRoom.setName("Class A");
        classRoom.setCourseId(1L);
        classRoom.setMaxStudents(50);
        classRoom.setCurrentStudents(0);
    }

    @Nested
    @DisplayName("enroll")
    class Enroll {

        @Test
        @DisplayName("should enroll student in published course")
        void enroll_success() {
            when(courseRepo.findById(1L)).thenReturn(Optional.of(publishedCourse));
            when(studentCourseRepo.existsByStudentIdAndCourseIdAndStatus(
                    1L, 1L, StudentCourse.EnrollStatus.ENROLLED)).thenReturn(false);
            when(classRoomRepo.findById(10L)).thenReturn(Optional.of(classRoom));
            when(studentCourseRepo.save(any(StudentCourse.class))).thenAnswer(inv -> inv.getArgument(0));
            when(classRoomRepo.save(any(ClassRoom.class))).thenAnswer(inv -> inv.getArgument(0));

            enrollService.enroll(1L, 1L, 10L);

            verify(studentCourseRepo).save(any(StudentCourse.class));
            assertThat(classRoom.getCurrentStudents()).isEqualTo(1);
            verify(classRoomRepo).save(classRoom);
        }

        @Test
        @DisplayName("should throw when course not found")
        void enroll_courseNotFound() {
            when(courseRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollService.enroll(1L, 999L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("课程不存在");
        }

        @Test
        @DisplayName("should throw when course not published")
        void enroll_courseNotPublished() {
            when(courseRepo.findById(2L)).thenReturn(Optional.of(draftCourse));

            assertThatThrownBy(() -> enrollService.enroll(1L, 2L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("课程未发布，无法选课");
        }

        @Test
        @DisplayName("should throw when student already enrolled")
        void enroll_alreadyEnrolled() {
            when(courseRepo.findById(1L)).thenReturn(Optional.of(publishedCourse));
            when(studentCourseRepo.existsByStudentIdAndCourseIdAndStatus(
                    1L, 1L, StudentCourse.EnrollStatus.ENROLLED)).thenReturn(true);

            assertThatThrownBy(() -> enrollService.enroll(1L, 1L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("已选过该课程");
        }

        @Test
        @DisplayName("should throw when classroom not found")
        void enroll_classroomNotFound() {
            when(courseRepo.findById(1L)).thenReturn(Optional.of(publishedCourse));
            when(studentCourseRepo.existsByStudentIdAndCourseIdAndStatus(
                    1L, 1L, StudentCourse.EnrollStatus.ENROLLED)).thenReturn(false);
            when(classRoomRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollService.enroll(1L, 1L, 999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("班级不存在");
        }

        @Test
        @DisplayName("should throw when classroom is full")
        void enroll_classroomFull() {
            classRoom.setCurrentStudents(50); // at max capacity
            when(courseRepo.findById(1L)).thenReturn(Optional.of(publishedCourse));
            when(studentCourseRepo.existsByStudentIdAndCourseIdAndStatus(
                    1L, 1L, StudentCourse.EnrollStatus.ENROLLED)).thenReturn(false);
            when(classRoomRepo.findById(10L)).thenReturn(Optional.of(classRoom));

            assertThatThrownBy(() -> enrollService.enroll(1L, 1L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("班级已满");
        }
    }

    @Nested
    @DisplayName("drop")
    class Drop {

        @Test
        @DisplayName("should drop enrolled student from course")
        void drop_success() {
            StudentCourse sc = new StudentCourse();
            sc.setId(1L);
            sc.setStudentId(1L);
            sc.setCourseId(1L);
            sc.setClassroomId(10L);
            sc.setStatus(StudentCourse.EnrollStatus.ENROLLED);

            when(studentCourseRepo.findByStudentIdAndCourseId(1L, 1L))
                    .thenReturn(Optional.of(sc));
            when(studentCourseRepo.save(any(StudentCourse.class))).thenAnswer(inv -> inv.getArgument(0));
            when(classRoomRepo.findById(10L)).thenReturn(Optional.of(classRoom));
            when(classRoomRepo.save(any(ClassRoom.class))).thenAnswer(inv -> inv.getArgument(0));

            classRoom.setCurrentStudents(5);

            enrollService.drop(1L, 1L);

            assertThat(sc.getStatus()).isEqualTo(StudentCourse.EnrollStatus.DROPPED);
            assertThat(sc.getDroppedAt()).isNotNull();
            assertThat(classRoom.getCurrentStudents()).isEqualTo(4);
        }

        @Test
        @DisplayName("should throw when student not enrolled")
        void drop_notEnrolled() {
            when(studentCourseRepo.findByStudentIdAndCourseId(1L, 99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollService.drop(1L, 99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("未选过该课程");
        }

        @Test
        @DisplayName("should throw when already dropped")
        void drop_alreadyDropped() {
            StudentCourse sc = new StudentCourse();
            sc.setStatus(StudentCourse.EnrollStatus.DROPPED);

            when(studentCourseRepo.findByStudentIdAndCourseId(1L, 1L))
                    .thenReturn(Optional.of(sc));

            assertThatThrownBy(() -> enrollService.drop(1L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("已退课");
        }
    }

    @Nested
    @DisplayName("getEnrolledCourseIds")
    class GetEnrolledCourseIds {

        @Test
        @DisplayName("should return list of enrolled course IDs")
        void getEnrolledCourseIds_success() {
            StudentCourse sc1 = new StudentCourse();
            sc1.setCourseId(10L);
            StudentCourse sc2 = new StudentCourse();
            sc2.setCourseId(20L);

            when(studentCourseRepo.findByStudentIdAndStatus(1L, StudentCourse.EnrollStatus.ENROLLED))
                    .thenReturn(List.of(sc1, sc2));

            List<Long> result = enrollService.getEnrolledCourseIds(1L);

            assertThat(result).containsExactly(10L, 20L);
        }
    }

    @Nested
    @DisplayName("getEnrolledStudentIds")
    class GetEnrolledStudentIds {

        @Test
        @DisplayName("should return list of enrolled student IDs")
        void getEnrolledStudentIds_success() {
            StudentCourse sc1 = new StudentCourse();
            sc1.setStudentId(1L);
            StudentCourse sc2 = new StudentCourse();
            sc2.setStudentId(2L);

            when(studentCourseRepo.findByCourseIdAndStatus(10L, StudentCourse.EnrollStatus.ENROLLED))
                    .thenReturn(List.of(sc1, sc2));

            List<Long> result = enrollService.getEnrolledStudentIds(10L);

            assertThat(result).containsExactly(1L, 2L);
        }
    }

    @Nested
    @DisplayName("createClassRoom")
    class CreateClassRoom {

        @Test
        @DisplayName("should create classroom and return DTO")
        void createClassRoom_success() {
            when(classRoomRepo.save(any(ClassRoom.class))).thenAnswer(inv -> {
                ClassRoom cr = inv.getArgument(0);
                cr.setId(1L);
                cr.setCreatedAt(LocalDateTime.now());
                return cr;
            });

            ClassRoomDTO input = new ClassRoomDTO();
            input.setName("Class A");
            input.setDescription("Desc");
            input.setCourseId(10L);
            input.setSemester("2024-2025-1");
            input.setMaxStudents(50);

            ClassRoomDTO result = enrollService.createClassRoom(input);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Class A");
            assertThat(result.getCourseId()).isEqualTo(10L);
            assertThat(result.getMaxStudents()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("getClassRoomsByCourse")
    class GetClassRoomsByCourse {

        @Test
        @DisplayName("should return classrooms for course")
        void getClassRoomsByCourse_success() {
            ClassRoom cr = new ClassRoom();
            cr.setId(1L);
            cr.setName("Class A");
            cr.setCourseId(10L);

            when(classRoomRepo.findByCourseId(10L)).thenReturn(List.of(cr));

            List<ClassRoomDTO> result = enrollService.getClassRoomsByCourse(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Class A");
        }

        @Test
        @DisplayName("should return empty list when no classrooms")
        void getClassRoomsByCourse_empty() {
            when(classRoomRepo.findByCourseId(99L)).thenReturn(List.of());

            List<ClassRoomDTO> result = enrollService.getClassRoomsByCourse(99L);

            assertThat(result).isEmpty();
        }
    }
}
