package com.autodev.course.repository;

import com.autodev.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByStatus(Course.CourseStatus status);

    List<Course> findByNameContaining(String name);
}
