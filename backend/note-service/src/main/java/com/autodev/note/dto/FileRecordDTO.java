package com.autodev.note.dto;

import com.autodev.note.entity.FileRecord;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileRecordDTO {
    private Long id;
    private String originalName;
    private String storagePath;
    private String contentType;
    private Long fileSize;
    private Long userId;
    private Long courseId;
    private String fileType;
    private LocalDateTime createdAt;
    private String downloadUrl;

    public static FileRecordDTO fromEntity(FileRecord f) {
        FileRecordDTO dto = new FileRecordDTO();
        dto.setId(f.getId());
        dto.setOriginalName(f.getOriginalName());
        dto.setStoragePath(f.getStoragePath());
        dto.setContentType(f.getContentType());
        dto.setFileSize(f.getFileSize());
        dto.setUserId(f.getUserId());
        dto.setCourseId(f.getCourseId());
        dto.setFileType(f.getFileType().name());
        dto.setCreatedAt(f.getCreatedAt());
        return dto;
    }
}
