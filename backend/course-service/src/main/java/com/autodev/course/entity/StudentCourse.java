package com.autodev.course.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "student_courses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
public class StudentCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long classroomId;

    /** 选课状态：ENROLLED / DROPPED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollStatus status = EnrollStatus.ENROLLED;

    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;

    @PrePersist
    public void prePersist() {
        this.enrolledAt = LocalDateTime.now();
    }

    public enum EnrollStatus {
        ENROLLED, DROPPED
    }
}
