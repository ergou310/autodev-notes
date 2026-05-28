package com.autodev.course.service;

import com.autodev.course.dto.CourseDTO;
import java.util.List;

public interface CourseService {

    CourseDTO createCourse(CourseDTO dto, Long teacherId, String teacherName);

    CourseDTO updateCourse(Long id, CourseDTO dto);

    void deleteCourse(Long id);

    CourseDTO getCourseById(Long id);

    List<CourseDTO> getAllCourses();

    List<CourseDTO> getCoursesByTeacher(Long teacherId);

    List<CourseDTO> searchCourses(String keyword);

    CourseDTO publishCourse(Long id);

    CourseDTO closeCourse(Long id);
}
