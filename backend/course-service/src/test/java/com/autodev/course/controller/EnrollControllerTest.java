package com.autodev.course.controller;

import com.autodev.course.dto.ClassRoomDTO;
import com.autodev.course.exception.GlobalExceptionHandler;
import com.autodev.course.service.EnrollService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.config.enabled=false"
})
class EnrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollService enrollService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/enroll")
    class Enroll {

        @Test
        @DisplayName("should enroll student and return success")
        void enroll_success() throws Exception {
            doNothing().when(enrollService).enroll(1L, 2L, 3L);

            Map<String, Long> body = Map.of(
                    "studentId", 1L,
                    "courseId", 2L,
                    "classroomId", 3L
            );

            mockMvc.perform(post("/api/enroll")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)));

            verify(enrollService).enroll(1L, 2L, 3L);
        }

        @Test
        @DisplayName("should return 400 when enrollment fails")
        void enroll_courseNotPublished() throws Exception {
            doThrow(new RuntimeException("课程未发布，无法选课"))
                    .when(enrollService).enroll(eq(1L), eq(2L), eq(3L));

            Map<String, Long> body = Map.of(
                    "studentId", 1L,
                    "courseId", 2L,
                    "classroomId", 3L
            );

            mockMvc.perform(post("/api/enroll")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", containsString("课程未发布")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/enroll")
    class Drop {

        @Test
        @DisplayName("should drop course and return success")
        void drop_success() throws Exception {
            doNothing().when(enrollService).drop(1L, 2L);

            mockMvc.perform(delete("/api/enroll")
                            .param("studentId", "1")
                            .param("courseId", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)));

            verify(enrollService).drop(1L, 2L);
        }
    }

    @Nested
    @DisplayName("GET /api/enroll/student/{studentId}/courses")
    class GetEnrolledCourses {

        @Test
        @DisplayName("should return enrolled course IDs")
        void getEnrolledCourses_success() throws Exception {
            when(enrollService.getEnrolledCourseIds(1L)).thenReturn(List.of(10L, 20L, 30L));

            mockMvc.perform(get("/api/enroll/student/1/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(3)))
                    .andExpect(jsonPath("$.data[0]", is(10)))
                    .andExpect(jsonPath("$.data[1]", is(20)))
                    .andExpect(jsonPath("$.data[2]", is(30)));
        }

        @Test
        @DisplayName("should return empty list when no enrollments")
        void getEnrolledCourses_empty() throws Exception {
            when(enrollService.getEnrolledCourseIds(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/enroll/student/1/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/enroll/course/{courseId}/students")
    class GetEnrolledStudents {

        @Test
        @DisplayName("should return enrolled student IDs")
        void getEnrolledStudents_success() throws Exception {
            when(enrollService.getEnrolledStudentIds(10L)).thenReturn(List.of(1L, 2L));

            mockMvc.perform(get("/api/enroll/course/10/students"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("POST /api/enroll/classroom")
    class CreateClassRoom {

        @Test
        @DisplayName("should create classroom and return DTO")
        void createClassRoom_success() throws Exception {
            ClassRoomDTO input = new ClassRoomDTO();
            input.setName("Class A");
            input.setDescription("First class");
            input.setCourseId(10L);
            input.setSemester("2024-2025-1");
            input.setMaxStudents(50);

            ClassRoomDTO output = new ClassRoomDTO();
            output.setId(1L);
            output.setName("Class A");
            output.setDescription("First class");
            output.setCourseId(10L);
            output.setSemester("2024-2025-1");
            output.setMaxStudents(50);
            output.setCurrentStudents(0);
            output.setCreatedAt(LocalDateTime.now());

            when(enrollService.createClassRoom(any(ClassRoomDTO.class))).thenReturn(output);

            mockMvc.perform(post("/api/enroll/classroom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name", is("Class A")))
                    .andExpect(jsonPath("$.data.maxStudents", is(50)));
        }
    }

    @Nested
    @DisplayName("GET /api/enroll/course/{courseId}/classrooms")
    class GetClassRooms {

        @Test
        @DisplayName("should return classrooms for a course")
        void getClassRooms_success() throws Exception {
            ClassRoomDTO cr1 = new ClassRoomDTO();
            cr1.setId(1L);
            cr1.setName("Class A");
            cr1.setCourseId(10L);

            ClassRoomDTO cr2 = new ClassRoomDTO();
            cr2.setId(2L);
            cr2.setName("Class B");
            cr2.setCourseId(10L);

            when(enrollService.getClassRoomsByCourse(10L)).thenReturn(List.of(cr1, cr2));

            mockMvc.perform(get("/api/enroll/course/10/classrooms"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name", is("Class A")))
                    .andExpect(jsonPath("$.data[1].name", is("Class B")));
        }
    }
}
