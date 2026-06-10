package com.kevin.agents.support;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class PromptManager {

    private final ResourceLoader resourceLoader;
    private final Map<String, String> promptCache;

    public PromptManager(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.promptCache = new java.util.concurrent.ConcurrentHashMap<>();
    }

    public String getPrompt(String resourcePath, String defaultPrompt) {
        return promptCache.computeIfAbsent(resourcePath, k -> {
            try {
                Resource resource = resourceLoader.getResource("classpath:prompts/" + resourcePath + ".txt");
                if (resource.exists()) {
                    return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                // ignore and use default
            }
            return defaultPrompt;
        });
    }

    public String renderPrompt(String template, Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(template);
        return promptTemplate.render(variables);
    }
}
