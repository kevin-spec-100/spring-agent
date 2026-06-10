package com.kevin.agents.agent.blacksmith;

import com.kevin.agents.agent.BaseAgent;
import com.kevin.agents.support.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoordinatorAgent extends BaseAgent {

    public CoordinatorAgent(ChatClient.Builder builder, PromptManager promptManager) {
        super(builder, promptManager);
    }

    @Override
    protected String name() {
        return "协调者";
    }

    @Override
    protected String model() {
        return "mimo-v2.5-pro";
    }

    @Override
    protected double temperature() {
        return 0.1;
    }

    @Override
    protected String systemPrompt() {
        return """
            你是对话协调者。根据以下规则判断下一轮该谁发言：
            - 如果最后一条消息是师傅发的 → 徒弟该回应 → 返回 apprentice
            - 如果最后一条消息是徒弟发的 → 师傅该继续 → 返回 master
            只返回一个词：master、apprentice 或 terminate，不要说其他内容。
            如果师傅的发言中明确宣布了兵器已"完成"，则返回 terminate。
            """;
    }

    public String decide(List<String> history) {
        String historyText = String.join("\n", history);
        return chat("对话历史：\n" + historyText + "\n\n下一轮该谁发言？只返回一个词：master、apprentice 或 terminate。");
    }
}
