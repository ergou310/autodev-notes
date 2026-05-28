package com.autodev.ai.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AiTaskRequest {
    /** 服务名称：note-generator / rag-service / analysis-service / grading-service */
    private String service;
    /** 接口路径，如 /generate-note */
    private String path;
    /** 请求参数 */
    private Map<String, Object> params;
}
