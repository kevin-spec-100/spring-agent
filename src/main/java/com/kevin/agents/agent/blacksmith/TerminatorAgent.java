package com.kevin.agents.agent.blacksmith;

import com.kevin.agents.agent.BaseAgent;
import com.kevin.agents.support.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TerminatorAgent extends BaseAgent {

    public TerminatorAgent(ChatClient.Builder builder, PromptManager promptManager) {
        super(builder, promptManager);
    }

    @Override
    protected String name() {
        return "终结者";
    }

    @Override
    protected String model() {
        return "mimo-v2.5-pro";
    }

    @Override
    protected double temperature() {
        return 0.0;
    }

    @Override
    protected String systemPrompt() {
        return """
            你是锻造流程终结者。根据对话历史判断兵器是否已真正打造完成并交付给客官。
            - 如果兵器已锻造完成并交付 → 返回 completed
            - 如果还在锻造中 → 返回 continue
            只返回一个词：completed 或 continue，不要说其他内容。
            """;
    }

    public String judge(String userNeed, List<String> history) {
        String historyText = String.join("\n", history);
        return chat("客官需求：" + userNeed + "\n\n对话历史：\n" + historyText + "\n\n兵器是否已打造完成？返回 completed 或 continue。");
    }
}
