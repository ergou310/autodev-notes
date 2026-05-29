package com.autodev.course.controller;

import com.autodev.course.common.Result;
import com.autodev.course.dto.CourseDTO;
import com.autodev.course.exception.GlobalExceptionHandler;
import com.autodev.course.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.config.enabled=false"
})
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseDTO sampleCourseDTO;

    @BeforeEach
    void setUp() {
        sampleCourseDTO = new CourseDTO();
        sampleCourseDTO.setId(1L);
        sampleCourseDTO.setName("Math 101");
        sampleCourseDTO.setDescription("Introduction to Mathematics");
        sampleCourseDTO.setCategory("Science");
        sampleCourseDTO.setTeacherId(100L);
        sampleCourseDTO.setTeacherName("Dr. Smith");
        sampleCourseDTO.setCoverImage("http://example.com/cover.jpg");
        sampleCourseDTO.setCredit(3);
        sampleCourseDTO.setStatus("DRAFT");
        sampleCourseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/course")
    class CreateCourse {

        @Test
        @DisplayName("should create course and return 200 with course data")
        void createCourse_success() throws Exception {
            when(courseService.createCourse(any(CourseDTO.class), eq(100L), eq("Dr. Smith")))
                    .thenReturn(sampleCourseDTO);

            mockMvc.perform(post("/api/course")
                            .header("X-User-Id", 100L)
                            .header("X-User-Name", "Dr. Smith")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCourseDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.message", is("success")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name", is("Math 101")))
                    .andExpect(jsonPath("$.data.teacherName", is("Dr. Smith")));

            verify(courseService).createCourse(any(CourseDTO.class), eq(100L), eq("Dr. Smith"));
        }
    }

    @Nested
    @DisplayName("PUT /api/course/{id}")
    class UpdateCourse {

        @Test
        @DisplayName("should update course and return updated data")
        void updateCourse_success() throws Exception {
            CourseDTO updated = new CourseDTO();
            updated.setId(1L);
            updated.setName("Math 202");
            updated.setDescription("Advanced Mathematics");
            updated.setCategory("Science");
            updated.setTeacherId(100L);
            updated.setTeacherName("Dr. Smith");
            updated.setStatus("DRAFT");

            when(courseService.updateCourse(eq(1L), any(CourseDTO.class))).thenReturn(updated);

            mockMvc.perform(put("/api/course/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updated)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data.name", is("Math 202")));
        }

        @Test
        @DisplayName("should return 400 when course not found")
        void updateCourse_notFound() throws Exception {
            when(courseService.updateCourse(eq(999L), any(CourseDTO.class)))
                    .thenThrow(new RuntimeException("课程不存在"));

            mockMvc.perform(put("/api/course/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCourseDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", containsString("课程不存在")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/course/{id}")
    class DeleteCourse {

        @Test
        @DisplayName("should delete course and return success")
        void deleteCourse_success() throws Exception {
            doNothing().when(courseService).deleteCourse(1L);

            mockMvc.perform(delete("/api/course/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)));

            verify(courseService).deleteCourse(1L);
        }

        @Test
        @DisplayName("should return 400 when course to delete not found")
        void deleteCourse_notFound() throws Exception {
            doThrow(new RuntimeException("课程不存在")).when(courseService).deleteCourse(999L);

            mockMvc.perform(delete("/api/course/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)));
        }
    }

    @Nested
    @DisplayName("GET /api/course/{id}")
    class GetCourse {

        @Test
        @DisplayName("should return course by id")
        void getCourse_success() throws Exception {
            when(courseService.getCourseById(1L)).thenReturn(sampleCourseDTO);

            mockMvc.perform(get("/api/course/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name", is("Math 101")));
        }

        @Test
        @DisplayName("should return 400 when course not found")
        void getCourse_notFound() throws Exception {
            when(courseService.getCourseById(999L))
                    .thenThrow(new RuntimeException("课程不存在"));

            mockMvc.perform(get("/api/course/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)));
        }
    }

    @Nested
    @DisplayName("GET /api/course")
    class GetAllCourses {

        @Test
        @DisplayName("should return all courses")
        void getAllCourses_success() throws Exception {
            CourseDTO course2 = new CourseDTO();
            course2.setId(2L);
            course2.setName("Physics 101");

            when(courseService.getAllCourses()).thenReturn(List.of(sampleCourseDTO, course2));

            mockMvc.perform(get("/api/course"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name", is("Math 101")))
                    .andExpect(jsonPath("$.data[1].name", is("Physics 101")));
        }

        @Test
        @DisplayName("should return empty list when no courses exist")
        void getAllCourses_empty() throws Exception {
            when(courseService.getAllCourses()).thenReturn(List.of());

            mockMvc.perform(get("/api/course"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/course/teacher/{teacherId}")
    class GetCoursesByTeacher {

        @Test
        @DisplayName("should return courses for a teacher")
        void getCoursesByTeacher_success() throws Exception {
            when(courseService.getCoursesByTeacher(100L))
                    .thenReturn(List.of(sampleCourseDTO));

            mockMvc.perform(get("/api/course/teacher/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].teacherId", is(100)));
        }
    }

    @Nested
    @DisplayName("GET /api/course/search")
    class SearchCourses {

        @Test
        @DisplayName("should return courses matching keyword")
        void searchCourses_success() throws Exception {
            when(courseService.searchCourses("Math")).thenReturn(List.of(sampleCourseDTO));

            mockMvc.perform(get("/api/course/search")
                            .param("keyword", "Math"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].name", is("Math 101")));
        }

        @Test
        @DisplayName("should return empty list when no matches found")
        void searchCourses_noResults() throws Exception {
            when(courseService.searchCourses("nonexistent")).thenReturn(List.of());

            mockMvc.perform(get("/api/course/search")
                            .param("keyword", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PUT /api/course/{id}/publish")
    class PublishCourse {

        @Test
        @DisplayName("should publish course")
        void publishCourse_success() throws Exception {
            sampleCourseDTO.setStatus("PUBLISHED");
            when(courseService.publishCourse(1L)).thenReturn(sampleCourseDTO);

            mockMvc.perform(put("/api/course/1/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status", is("PUBLISHED")));
        }
    }

    @Nested
    @DisplayName("PUT /api/course/{id}/close")
    class CloseCourse {

        @Test
        @DisplayName("should close course")
        void closeCourse_success() throws Exception {
            sampleCourseDTO.setStatus("CLOSED");
            when(courseService.closeCourse(1L)).thenReturn(sampleCourseDTO);

            mockMvc.perform(put("/api/course/1/close"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status", is("CLOSED")));
        }
    }
}
