package com.autodev.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Slf4j
@Service
public class AiServiceProxy {

    private final WebClient webClient;
    private final Map<String, String> serviceUrls;

    public AiServiceProxy(
            @Value("${ai.services.note-generator}") String noteGenUrl,
            @Value("${ai.services.rag-service}") String ragUrl,
            @Value("${ai.services.analysis-service}") String analysisUrl,
            @Value("${ai.services.grading-service}") String gradingUrl) {

        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        this.serviceUrls = Map.of(
                "note-generator", noteGenUrl,
                "rag-service", ragUrl,
                "analysis-service", analysisUrl,
                "grading-service", gradingUrl
        );
    }

    /**
     * 调用 Python AI 服务
     *
     * @param service 服务名称
     * @param path    接口路径
     * @param params  请求参数
     * @return 响应结果
     */
    public Object callService(String service, String path, Object params) {
        String baseUrl = serviceUrls.get(service);
        if (baseUrl == null) {
            throw new RuntimeException("未知的AI服务: " + service);
        }

        String url = baseUrl + path;
        log.info("调用AI服务: {} -> {}", service, url);

        try {
            Object result = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params != null ? params : Map.of())
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return result;
        } catch (WebClientResponseException e) {
            log.error("AI服务调用失败: {} {} -> {}", service, path, e.getResponseBodyAsString());
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI服务调用异常: {} {}", service, path, e);
            throw new RuntimeException("AI服务不可用: " + e.getMessage());
        }
    }

    /**
     * 同步调用（直接返回结果）
     */
    public Object callServiceSync(String service, String path, Object params) {
        return callService(service, path, params);
    }
}
