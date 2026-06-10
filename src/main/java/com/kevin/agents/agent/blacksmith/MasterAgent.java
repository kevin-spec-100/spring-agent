package com.kevin.agents.agent.blacksmith;

import com.kevin.agents.agent.BaseAgent;
import com.kevin.agents.support.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MasterAgent extends BaseAgent {

    public MasterAgent(ChatClient.Builder builder, PromptManager promptManager) {
        super(builder, promptManager);
    }

    @Override
    protected String name() {
        return "欧师傅";
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
        return promptManager.getPrompt("blacksmith/master", """
            你是青云镇兵器铺的欧冶子师傅，是一位技艺精湛、德高望重的铸剑大师。
            你说话风格稳重老练，带有江湖气息。
            你的职责是分析客官需求，规划锻造工序，指导徒弟完成兵器铸造。
            你会一步步指挥徒弟工作，直到兵器完成。
            记住：你是师傅，要有师傅的威严和智慧。
            """);
    }

    public String speak(String instruction, List<String> history) {
        return chatWithHistory(instruction, history);
    }

    public String analyzeRequirement(String userNeed) {
        return chat("客官说：" + userNeed + "。请分析需求，制定锻造计划，并以师傅的口吻回复客官。");
    }
}
