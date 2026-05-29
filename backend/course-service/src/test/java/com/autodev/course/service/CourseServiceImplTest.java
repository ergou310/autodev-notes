package com.autodev.course.service;

import com.autodev.course.dto.CourseDTO;
import com.autodev.course.entity.Course;
import com.autodev.course.repository.CourseRepository;
import com.autodev.course.service.impl.CourseServiceImpl;
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
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        sampleCourse = new Course();
        sampleCourse.setId(1L);
        sampleCourse.setName("Math 101");
        sampleCourse.setDescription("Introduction to Mathematics");
        sampleCourse.setCategory("Science");
        sampleCourse.setTeacherId(100L);
        sampleCourse.setTeacherName("Dr. Smith");
        sampleCourse.setCoverImage("http://example.com/cover.jpg");
        sampleCourse.setCredit(3);
        sampleCourse.setStatus(Course.CourseStatus.DRAFT);
        sampleCourse.setCreatedAt(LocalDateTime.now());
        sampleCourse.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createCourse")
    class CreateCourse {

        @Test
        @DisplayName("should create course and return DTO")
        void createCourse_success() {
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            CourseDTO input = new CourseDTO();
            input.setName("Math 101");
            input.setDescription("Introduction to Mathematics");
            input.setCategory("Science");
            input.setCoverImage("http://example.com/cover.jpg");
            input.setCredit(3);

            CourseDTO result = courseService.createCourse(input, 100L, "Dr. Smith");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Math 101");
            assertThat(result.getTeacherId()).isEqualTo(100L);
            assertThat(result.getTeacherName()).isEqualTo("Dr. Smith");

            verify(courseRepository).save(any(Course.class));
        }
    }

    @Nested
    @DisplayName("updateCourse")
    class UpdateCourse {

        @Test
        @DisplayName("should update existing course fields")
        void updateCourse_success() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CourseDTO update = new CourseDTO();
            update.setName("Math 202");
            update.setDescription("Updated description");

            CourseDTO result = courseService.updateCourse(1L, update);

            assertThat(result.getName()).isEqualTo("Math 202");
            assertThat(result.getDescription()).isEqualTo("Updated description");
            // Category should remain unchanged since not set in update
            assertThat(result.getCategory()).isEqualTo("Science");

            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("should throw RuntimeException when course not found")
        void updateCourse_notFound() {
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            CourseDTO update = new CourseDTO();
            update.setName("New name");

            assertThatThrownBy(() -> courseService.updateCourse(999L, update))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("课程不存在");

            verify(courseRepository, never()).save(any());
        }

        @Test
        @DisplayName("should not overwrite fields with null values")
        void updateCourse_partialUpdate() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CourseDTO update = new CourseDTO();
            update.setName("New Name Only");
            // all other fields null

            CourseDTO result = courseService.updateCourse(1L, update);

            assertThat(result.getName()).isEqualTo("New Name Only");
            assertThat(result.getDescription()).isEqualTo("Introduction to Mathematics");
            assertThat(result.getCategory()).isEqualTo("Science");
            assertThat(result.getCredit()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("deleteCourse")
    class DeleteCourse {

        @Test
        @DisplayName("should delete course when it exists")
        void deleteCourse_success() {
            when(courseRepository.existsById(1L)).thenReturn(true);
            doNothing().when(courseRepository).deleteById(1L);

            courseService.deleteCourse(1L);

            verify(courseRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw RuntimeException when course does not exist")
        void deleteCourse_notFound() {
            when(courseRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> courseService.deleteCourse(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("课程不存在");

            verify(courseRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("getCourseById")
    class GetCourseById {

        @Test
        @DisplayName("should return course DTO when found")
        void getCourseById_success() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

            CourseDTO result = courseService.getCourseById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Math 101");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("should throw RuntimeException when not found")
        void getCourseById_notFound() {
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseById(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("课程不存在");
        }
    }

    @Nested
    @DisplayName("getAllCourses")
    class GetAllCourses {

        @Test
        @DisplayName("should return list of all courses")
        void getAllCourses_success() {
            Course course2 = new Course();
            course2.setId(2L);
            course2.setName("Physics 101");
            course2.setTeacherId(100L);
            course2.setStatus(Course.CourseStatus.PUBLISHED);

            when(courseRepository.findAll()).thenReturn(List.of(sampleCourse, course2));

            List<CourseDTO> result = courseService.getAllCourses();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Math 101");
            assertThat(result.get(1).getName()).isEqualTo("Physics 101");
        }

        @Test
        @DisplayName("should return empty list when no courses")
        void getAllCourses_empty() {
            when(courseRepository.findAll()).thenReturn(List.of());

            List<CourseDTO> result = courseService.getAllCourses();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCoursesByTeacher")
    class GetCoursesByTeacher {

        @Test
        @DisplayName("should return courses for a given teacher")
        void getCoursesByTeacher_success() {
            when(courseRepository.findByTeacherId(100L)).thenReturn(List.of(sampleCourse));

            List<CourseDTO> result = courseService.getCoursesByTeacher(100L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTeacherId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("searchCourses")
    class SearchCourses {

        @Test
        @DisplayName("should return courses matching keyword")
        void searchCourses_success() {
            when(courseRepository.findByNameContaining("Math")).thenReturn(List.of(sampleCourse));

            List<CourseDTO> result = courseService.searchCourses("Math");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("Math");
        }

        @Test
        @DisplayName("should return empty list when no matches")
        void searchCourses_noResults() {
            when(courseRepository.findByNameContaining("xyz")).thenReturn(List.of());

            List<CourseDTO> result = courseService.searchCourses("xyz");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("publishCourse")
    class PublishCourse {

        @Test
        @DisplayName("should set status to PUBLISHED")
        void publishCourse_success() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CourseDTO result = courseService.publishCourse(1L);

            assertThat(result.getStatus()).isEqualTo("PUBLISHED");
            assertThat(sampleCourse.getStatus()).isEqualTo(Course.CourseStatus.PUBLISHED);
            verify(courseRepository).save(sampleCourse);
        }

        @Test
        @DisplayName("should throw when course not found")
        void publishCourse_notFound() {
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.publishCourse(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("课程不存在");
        }
    }

    @Nested
    @DisplayName("closeCourse")
    class CloseCourse {

        @Test
        @DisplayName("should set status to CLOSED")
        void closeCourse_success() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CourseDTO result = courseService.closeCourse(1L);

            assertThat(result.getStatus()).isEqualTo("CLOSED");
            assertThat(sampleCourse.getStatus()).isEqualTo(Course.CourseStatus.CLOSED);
            verify(courseRepository).save(sampleCourse);
        }

        @Test
        @DisplayName("should throw when course not found")
        void closeCourse_notFound() {
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.closeCourse(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("课程不存在");
        }
    }
}
