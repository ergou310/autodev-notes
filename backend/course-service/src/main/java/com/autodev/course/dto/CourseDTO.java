package com.autodev.course.dto;

import com.autodev.course.entity.Course;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Long teacherId;
    private String teacherName;
    private String coverImage;
    private Integer credit;
    private String status;
    private LocalDateTime createdAt;

    public static CourseDTO fromEntity(Course c) {
        CourseDTO dto = new CourseDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setCategory(c.getCategory());
        dto.setTeacherId(c.getTeacherId());
        dto.setTeacherName(c.getTeacherName());
        dto.setCoverImage(c.getCoverImage());
        dto.setCredit(c.getCredit());
        dto.setStatus(c.getStatus().name());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
