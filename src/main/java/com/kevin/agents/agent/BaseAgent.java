package com.kevin.agents.agent;

import com.kevin.agents.support.PromptManager;
import reactor.core.publisher.Flux;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.List;

public abstract class BaseAgent {

    private final ChatClient chatClient;
    protected final PromptManager promptManager;

    protected BaseAgent(ChatClient.Builder builder, PromptManager promptManager) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model())
                .temperature(temperature())
                .build();
        this.chatClient = builder.defaultOptions(options).build();
        this.promptManager = promptManager;
    }

    protected abstract String name();

    protected abstract String model();

    protected abstract double temperature();

    protected abstract String systemPrompt();

    protected String chat(String userMessage) {
        return chatClient.prompt()
                .system(systemPrompt())
                .user(userMessage)
                .call()
                .content();
    }

    protected String chatWithHistory(String userMessage, List<String> history) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            context.append(history.get(i));
            if (i < history.size() - 1) {
                context.append("\n");
            }
        }

        return chatClient.prompt()
                .system(systemPrompt())
                .user("对话历史：\n" + context + "\n\n当前消息：" + userMessage)
                .call()
                .content();
    }

    public Flux<String> stream(String userMessage) {
        return chatClient.prompt()
                .system(systemPrompt())
                .user(userMessage)
                .stream()
                .content()
                .filter(content -> content != null && !content.isEmpty());
    }

    public String getName() {
        return name();
    }
}
