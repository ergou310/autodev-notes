package com.autodev.note.dto;

import com.autodev.note.entity.Note;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoteDTO {
    private Long id;
    private String title;
    private String content;
    private String mindMap;
    private String quizQuestions;
    private Long courseId;
    private Long userId;
    private String source;
    private Long sourceFileId;
    private Boolean shared;
    private String shareCode;
    private String tags;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoteDTO fromEntity(Note n) {
        NoteDTO dto = new NoteDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setContent(n.getContent());
        dto.setMindMap(n.getMindMap());
        dto.setQuizQuestions(n.getQuizQuestions());
        dto.setCourseId(n.getCourseId());
        dto.setUserId(n.getUserId());
        dto.setSource(n.getSource().name());
        dto.setSourceFileId(n.getSourceFileId());
        dto.setShared(n.getShared());
        dto.setShareCode(n.getShareCode());
        dto.setTags(n.getTags());
        dto.setStatus(n.getStatus().name());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setUpdatedAt(n.getUpdatedAt());
        return dto;
    }
}
