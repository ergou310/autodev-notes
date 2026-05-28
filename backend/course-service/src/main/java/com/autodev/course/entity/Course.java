package com.autodev.course.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String category;

    /** 所属教师ID */
    @Column(nullable = false)
    private Long teacherId;

    /** 教师姓名（冗余，方便查询） */
    @Column(length = 50)
    private String teacherName;

    /** 课程封面图URL */
    @Column(length = 500)
    private String coverImage;

    /** 学分 */
    private Integer credit;

    /** 状态：DRAFT / PUBLISHED / CLOSED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum CourseStatus {
        DRAFT, PUBLISHED, CLOSED
    }
}
