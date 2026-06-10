package com.kevin.agents.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevin.agents.agent.single.SimpleSingleAgent;
import com.kevin.agents.agent.blacksmith.BlacksmithOrchestrator;
import com.kevin.agents.dto.StreamEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Slf4j
@Service
public class SingleAgentService {

    private final SimpleSingleAgent simpleSingleAgent;
    private final ObjectMapper objectMapper;
    private final BlacksmithOrchestrator blacksmithOrchestrator;

    public SingleAgentService(SimpleSingleAgent simpleSingleAgent, BlacksmithOrchestrator blacksmithOrchestrator) {
        this.simpleSingleAgent = simpleSingleAgent;
        this.objectMapper = new ObjectMapper();
        this.blacksmithOrchestrator = blacksmithOrchestrator;
    }

    public String chat(String message) {
        return simpleSingleAgent.chat(message);
    }

    /**
     * 流式聊天 - SSE 流式输出(LLM 返回一个 token 就推送一个)
     * <h3>防止中途断开的措施<h3/>
     * <ul>
     *     <li>超时设置为 5 分钟</li>
     *     <li>用 Schedules.boundedElastic() 确保在独立线程运行</li>
     *     <li>用 SseEmitter.event().data() 发送标准 SSE 格式</li>
     *     <li>每个事件发送后立即 flush</li>
     * </ul>
     */
    public SseEmitter streamChat(String message, String sessionId) {
        // 5 分钟超时，防止 LLM 响应慢导致断开
        SseEmitter emitter = new SseEmitter(300_000L);

        // 独立线程运行 Flux,避免阻塞 HTTP 线程
        Flux<String> tokenStream = simpleSingleAgent.stream(message)
                .subscribeOn(Schedulers.boundedElastic());

        StringBuilder fullResponse = new StringBuilder();


        tokenStream
                .doOnNext(token -> {
                    fullResponse.append(token);
                    sendEvent(emitter, StreamEvent.chunk(token), sessionId);
                })
                .doOnComplete(() -> {

                    sendEvent(emitter, StreamEvent.done(), sessionId);
                    emitter.complete();
                    log.info("Stream complete:{} chars for session:{}", fullResponse.length(), sessionId);

                })
                .doOnError(e -> {
                    log.error("Stream error for session:{}", sessionId, e);
                    sendEvent(emitter, StreamEvent.error(e.getMessage()), sessionId);
                    emitter.completeWithError(e);
                })
                .doOnCancel(() -> {
                    log.warn("Stream cancelled for session: {}, collected {} chars", sessionId, fullResponse.length());
                })
                .subscribe();

        // 生命周期回顾
        emitter.onCompletion(() ->
            log.debug("SSE connection completed for session:{}", sessionId));
        emitter.onTimeout(() ->
            log.warn("SSE connection timed out for session:{}, collected {} chars", sessionId, fullResponse.length()));
        emitter.onError(e ->
            log.error("SSE connection error for session:{}", sessionId, e));

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, StreamEvent event, String sessionId) {
        try {
            // 手动序列化 JSON， 避免 Spring 消息转换器问题
            String json = objectMapper.writeValueAsString(event);
            emitter.send(SseEmitter.event().id(sessionId).data(json));
        } catch (IOException e) {
            log.error("Failed to send SSE event for session:{}, event type: {}", sessionId, event.getType(), e);
        }
    }
}
