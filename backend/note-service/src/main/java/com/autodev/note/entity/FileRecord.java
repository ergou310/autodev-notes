package com.autodev.note.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_records")
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 原始文件名 */
    @Column(nullable = false, length = 255)
    private String originalName;

    /** MinIO 存储路径 */
    @Column(nullable = false, length = 500)
    private String storagePath;

    /** 文件类型 */
    @Column(length = 50)
    private String contentType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 所属用户ID */
    @Column(nullable = false)
    private Long userId;

    /** 关联课程ID */
    private Long courseId;

    /** 文件用途：RECORDING / PPT / VIDEO / IMAGE / DOCUMENT */
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum FileType { RECORDING, PPT, VIDEO, IMAGE, DOCUMENT }
}
