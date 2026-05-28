package com.autodev.ai.controller;

import com.autodev.ai.common.Result;
import com.autodev.ai.dto.AiTaskRequest;
import com.autodev.ai.dto.AiTaskResponse;
import com.autodev.ai.service.AiServiceProxy;
import com.autodev.ai.service.AiTaskManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiGatewayController {

    private final AiServiceProxy aiServiceProxy;
    private final AiTaskManager aiTaskManager;

    /**
     * 同步调用AI服务（适合快速响应的接口，如简单LLM调用）
     * POST /api/ai/call
     */
    @PostMapping("/call")
    public Result<Object> callAiService(@RequestBody AiTaskRequest request) {
        Object result = aiServiceProxy.callServiceSync(
                request.getService(), request.getPath(), request.getParams());
        return Result.success(result);
    }

    /**
     * 异步提交AI任务（适合耗时接口，如语音转文字、笔记生成）
     * POST /api/ai/task
     */
    @PostMapping("/task")
    public Result<AiTaskResponse> submitTask(@RequestBody AiTaskRequest request) {
        AiTaskResponse response = aiTaskManager.submitTask(
                request.getService(), request.getPath(), request.getParams());
        return Result.success(response);
    }

    /**
     * 查询异步任务状态
     * GET /api/ai/task/{taskId}
     */
    @GetMapping("/task/{taskId}")
    public Result<AiTaskResponse> getTaskStatus(@PathVariable String taskId) {
        return Result.success(aiTaskManager.getTaskStatus(taskId));
    }

    // ── 快捷接口：笔记生成 ──

    /** 语音转文字 */
    @PostMapping("/note/transcribe")
    public Result<Object> transcribe(@RequestBody Map<String, Object> params) {
        return Result.success(aiServiceProxy.callServiceSync("note-generator", "/transcribe", params));
    }

    /** 生成笔记 */
    @PostMapping("/note/generate")
    public Result<Object> generateNote(@RequestBody Map<String, Object> params) {
        return Result.success(aiServiceProxy.callServiceSync("note-generator", "/generate", params));
    }

    /** 生成思维导图 */
    @PostMapping("/note/mindmap")
    public Result<Object> generateMindMap(@RequestBody Map<String, Object> params) {
        return Result.success(aiServiceProxy.callServiceSync("note-generator", "/mindmap", params));
    }

    /** 生成复习题 */
    @PostMapping("/note/quiz")
    public Result<Object> generateQuiz(@RequestBody Map<String, Object> params) {
        return Result.success(aiServiceProxy.callServiceSync("note-generator", "/quiz", params));
    }

    // ── 快捷接口：RAG ──

    /** 文档向量化 */
    @PostMapping("/rag/index")
    public Result<Object> indexDocument(@RequestBody Map<String, Object> params) {
        return Result.success(aiServiceProxy.callServiceSync("rag-service", "/index", params));
    }

    /** 智能问答 */
    @PostMapping("/rag/ask")
    public Result<Object> askQuestion(@RequestBody Map<String, Object> params) {
        return Result.success(aiServiceProxy.callServiceSync("rag-service", "/ask", params));
    }

    // ── 快捷接口：学情分析 ──

    /** 课堂分析 */
    @PostMapping("/analysis/classroom")
    public Result<Object> analyzeClassroom(@RequestBody Map<String, Object> params) {
        return Result.success(aiServiceProxy.callServiceSync("analysis-service", "/analyze", params));
    }

    // ── 快捷接口：智能阅卷 ──

    /** 自动批改 */
    @PostMapping("/grading/grade")
    public Result<Object> gradeAnswer(@RequestBody Map<String, Object> params) {
        return Result.success(aiServiceProxy.callServiceSync("grading-service", "/grade", params));
    }
}
