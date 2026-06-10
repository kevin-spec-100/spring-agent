package com.kevin.agents.agent.blacksmith;

import com.kevin.agents.agent.BaseAgent;
import com.kevin.agents.support.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApprenticeAgent extends BaseAgent {

    public ApprenticeAgent(ChatClient.Builder builder, PromptManager promptManager) {
        super(builder, promptManager);
    }

    @Override
    protected String name() {
        return "小铁";
    }

    @Override
    protected String model() {
        return "mimo-v2.5-pro";
    }

    @Override
    protected double temperature() {
        return 0.5;
    }

    @Override
    protected String systemPrompt() {
        return promptManager.getPrompt("blacksmith/apprentice", """
            你是青云镇兵器铺的小铁，是欧冶子师傅的徒弟。
            你年轻勤快，说话带点俏皮，但尊敬师傅。
            你的职责是执行师傅的指令，完成锻造过程中的各种任务。
            执行完毕后要向师傅汇报。
            """);
    }

    public String speak(String instruction, List<String> history) {
        return chatWithHistory(instruction, history);
    }

    public String execute(String masterCommand) {
        return chat("师傅说：" + masterCommand + "。请执行师傅的指令，完成任务后汇报。");
    }
}
