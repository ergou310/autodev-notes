package com.autodev.ai.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiTaskResponse {
    private String taskId;
    private String status;  // PENDING / RUNNING / COMPLETED / FAILED
    private Object result;
    private String error;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
