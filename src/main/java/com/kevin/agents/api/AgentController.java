package com.kevin.agents.api;

import com.kevin.agents.agent.service.SingleAgentService;
import com.kevin.agents.dto.ChatRequest;
import com.kevin.agents.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    public static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final SingleAgentService singleAgentService;

    @PostMapping("/chat")
    public String simpleChat(@RequestBody ChatRequest request) {
        long startTime = System.currentTimeMillis();
        return singleAgentService.chat(request.getMessage());
    }

    @PostMapping(value = "/single/stream")
    public SseEmitter stream(@RequestBody ChatRequest request) {
        log.info("SSE stream request: message:{}, sessionId:{}", request.getMessage(), request.getSessionId());
        return singleAgentService.streamChat(request.getMessage(), request.getSessionId());
    }
}
