package com.kevin.agents.agent.blacksmith;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class BlacksmithOrchestrator {

    private static final int MAX_ROUNDS = 20;

    private final MasterAgent master;
    private final ApprenticeAgent apprentice;
    private final CoordinatorAgent coordinator;
    private final TerminatorAgent terminator;

    public BlacksmithOrchestrator(
            MasterAgent master,
            ApprenticeAgent apprentice,
            CoordinatorAgent coordinator,
            TerminatorAgent terminator) {
        this.master = master;
        this.apprentice = apprentice;
        this.coordinator = coordinator;
        this.terminator = terminator;
    }

    private void printDivider() {
        System.out.println("───────────────────────────────────────");
    }

    private void printMater(String content) {
        System.out.println("【欧师傅】 > " + content);
        System.out.println();
    }

    private void printApprentice(String content) {
        System.out.println("【小  铁】 > " + content);
        System.out.println();
    }

    public String startBlacksmith(Scanner scanner) {
        StringBuilder result = new StringBuilder();
        List<String> history = new ArrayList<>();

        printLine(result, "═══════════════════════════════════════");
        printLine(result, "        欢迎来到青云镇兵器铺");
        printLine(result, "═══════════════════════════════════════");

        // step1: 师傅以 NPC 身份迎接来客
        String greet = master.speak("你是青云铁匠铺的欧师傅，一位客官走进你的铺子。请以仙侠世界 NPC 的身份热情的迎接他", history);
        printMater(greet);
        history.add("欧师傅 > " + greet);

        // step2: 客官选择要打造的兵器
        System.out.println("【兵器谱】");
        System.out.println("1. 金箍棒");
        System.out.println("2. 芭蕉扇");
        System.out.println();
        System.out.print("请选择要打造的兵器：");
        String choice = scanner.nextLine().trim();

        String userNeed = switch (choice) {
            case "1" -> "帮我打造一个金箍棒";
            case "2" -> "帮我打造一个芭蕉扇";
            default -> "帮我打造一个金箍棒";
        };

        System.out.println("【客  官】 > " + userNeed);
        System.out.println();
        history.add("客官 > " + userNeed);


        // step3: 师傅听到需求，开始锻造
        String reply = master.speak("客官说：" + userNeed + "\n请分析他的需求，说明材料和价格，然后开始锻造工序的第一步。", history);
        printMater(reply);
        history.add("欧师傅 > " + reply);

        // step4: 师徒对话循环
        int round = 1;

        while (round <= MAX_ROUNDS) {
            printDivider();

            // 协调 Agent 判断下一轮谁发言
            String nextSpeaker = coordinator.decide(history);
            String decision = nextSpeaker.trim().toLowerCase();

            // 协调 Agent 认为该终止了，交由终止 Agent 评估
            if (decision.contains("terminate")) {
                String judgeResult = terminator.judge(userNeed, history);
                if (judgeResult.trim().toLowerCase().contains("completed")) {
                    printLine(result, "═══════════════════════════════════════");
                    printLine(result, "             兵器已成");
                    printLine(result, "═══════════════════════════════════════");
                    break;
                }
                // 终止 Agent 认为还没完成，继续
                decision = "master";
            }

            if (decision.contains("apprentice")) {
                reply = apprentice.speak("师傅刚才说：" + reply + "\n请根据师傅的指令，恭敬的执行并描述过程", history);
                printApprentice(reply);
                history.add("小铁 > " + reply);
            } else {
                reply = master.speak("徒弟已经执行了你上一轮的指令，他的回应是：\n" + reply +"\n请继续下一步工序，如果兵器已打造完成并交付客官。", history);
                printMater(reply);
                history.add("欧师傅 > " + reply);
            }
            round++;
        }

        return result.toString();
    }

    private void printLine(StringBuilder sb, String line) {
        sb.append(line).append("\n");
    }
}
