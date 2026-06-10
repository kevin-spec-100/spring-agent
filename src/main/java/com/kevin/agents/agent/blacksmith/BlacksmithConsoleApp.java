package com.kevin.agents.agent.blacksmith;

import com.kevin.agents.SpringAgentApplication;
import com.kevin.agents.support.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

/**
 * 兵器谱控制台应用入口 运行方式：确保已配置 OPENAI API_KEY 环境变量 2. 编译 mvn clean compile
 * 3、运行：直接运行此类的 main 方法 注意：此类通过自动 Spring Boot 上下文获取自动配置的 ChatClient.Builder，
 * 而非直接 new ChatClient.Builder()（Spring AI 1.0.0-M6 中该方法需要 ChatModel 参数）
 *
 * @author Kevin
 * @since 2026/6/3
 */
public class BlacksmithConsoleApp {

    public static void main(String[] args) {

        System.out.println("═══════════════════════════════════════");
        System.out.println("        欢迎来到青云镇兵器铺");
        System.out.println("═══════════════════════════════════════");

        SpringApplication app = new SpringApplication(SpringAgentApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext context = app.run(args);

        try {
            ChatClient.Builder builder = context.getBean(ChatClient.Builder.class);
            PromptManager promptManager = context.getBean(PromptManager.class);

            MasterAgent master = new MasterAgent(builder, promptManager);
            ApprenticeAgent apprentice = new ApprenticeAgent(builder, promptManager);
            CoordinatorAgent coordinator = new CoordinatorAgent(builder, promptManager);
            TerminatorAgent terminator = new TerminatorAgent(builder, promptManager);
            BlacksmithOrchestrator orchestrator = new BlacksmithOrchestrator(master, apprentice, coordinator, terminator);

            Scanner scanner = new Scanner(System.in);
            orchestrator.startBlacksmith(scanner);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}
