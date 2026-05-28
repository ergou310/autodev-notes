package com.autodev.note.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /** 笔记正文（Markdown） */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 思维导图 JSON */
    @Column(columnDefinition = "TEXT")
    private String mindMap;

    /** 复习题 JSON */
    @Column(columnDefinition = "TEXT")
    private String quizQuestions;

    /** 所属课程ID */
    private Long courseId;

    /** 创建者ID */
    @Column(nullable = false)
    private Long userId;

    /** 来源类型：MANUAL / AI_GENERATED / UPLOAD */
    @Enumerated(EnumType.STRING)
    private NoteSource source = NoteSource.MANUAL;

    /** 关联的源文件ID（录音/PPT等） */
    private Long sourceFileId;

    /** 是否公开分享 */
    private Boolean shared = false;

    /** 分享码 */
    @Column(length = 32)
    private String shareCode;

    /** 标签，逗号分隔 */
    @Column(length = 500)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteStatus status = NoteStatus.DRAFT;

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

    public enum NoteSource { MANUAL, AI_GENERATED, UPLOAD }
    public enum NoteStatus { DRAFT, PUBLISHED, ARCHIVED }
}
