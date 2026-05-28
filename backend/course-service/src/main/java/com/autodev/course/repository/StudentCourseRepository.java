package com.autodev.course.repository;

import com.autodev.course.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByStudentIdAndStatus(Long studentId, StudentCourse.EnrollStatus status);

    List<StudentCourse> findByCourseIdAndStatus(Long courseId, StudentCourse.EnrollStatus status);

    Optional<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, StudentCourse.EnrollStatus status);
}
