package com.kevin.agents.agent.single;

import com.kevin.agents.agent.BaseAgent;
import com.kevin.agents.support.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class SimpleSingleAgent extends BaseAgent {

    private final PromptManager promptManager;

    public SimpleSingleAgent(ChatClient.Builder builder, PromptManager promptManager) {
        super(builder, promptManager);
        this.promptManager = promptManager;
    }

    @Override
    protected String name() {
        return "AI助手";
    }

    @Override
    protected String model() {
        return "mimo-v2.5-pro";
    }

    @Override
    protected double temperature() {
        return 0.7;
    }

    @Override
    protected String systemPrompt() {
        return promptManager.getPrompt("single/system_prompt", "你是一个专业的AI助手，请用中文回答用户的问题。回答要简洁明了，有条理。");
    }

    public String chat(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息不能为空");
        }
        return super.chat(message);
    }
}
