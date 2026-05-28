package com.autodev.ai.service;

import com.autodev.ai.dto.AiTaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskManager {

    private final StringRedisTemplate redisTemplate;
    private final AiServiceProxy aiServiceProxy;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final ConcurrentHashMap<String, AiTaskResponse> localCache = new ConcurrentHashMap<>();

    /**
     * 提交异步AI任务
     */
    public AiTaskResponse submitTask(String service, String path, Object params) {
        String taskId = UUID.randomUUID().toString().replace("-", "");

        AiTaskResponse response = new AiTaskResponse();
        response.setTaskId(taskId);
        response.setStatus("PENDING");
        response.setCreatedAt(LocalDateTime.now());
        localCache.put(taskId, response);

        // 异步执行
        CompletableFuture.runAsync(() -> {
            AiTaskResponse running = localCache.get(taskId);
            running.setStatus("RUNNING");

            try {
                Object result = aiServiceProxy.callService(service, path, params);
                running.setStatus("COMPLETED");
                running.setResult(result);
                running.setCompletedAt(LocalDateTime.now());
            } catch (Exception e) {
                log.error("AI任务执行失败: taskId={}", taskId, e);
                running.setStatus("FAILED");
                running.setError(e.getMessage());
                running.setCompletedAt(LocalDateTime.now());
            }
        }, executor);

        return response;
    }

    /**
     * 查询任务状态
     */
    public AiTaskResponse getTaskStatus(String taskId) {
        AiTaskResponse response = localCache.get(taskId);
        if (response == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        return response;
    }
}
