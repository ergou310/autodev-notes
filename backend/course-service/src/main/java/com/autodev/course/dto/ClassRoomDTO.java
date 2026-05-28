package com.autodev.course.dto;

import com.autodev.course.entity.ClassRoom;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassRoomDTO {
    private Long id;
    private String name;
    private String description;
    private Long courseId;
    private String semester;
    private Integer maxStudents;
    private Integer currentStudents;
    private LocalDateTime createdAt;

    public static ClassRoomDTO fromEntity(ClassRoom c) {
        ClassRoomDTO dto = new ClassRoomDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setCourseId(c.getCourseId());
        dto.setSemester(c.getSemester());
        dto.setMaxStudents(c.getMaxStudents());
        dto.setCurrentStudents(c.getCurrentStudents());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
