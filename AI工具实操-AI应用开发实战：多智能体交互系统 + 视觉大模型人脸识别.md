# AI工具实操-AI应用开发实战：多智能体交互系统 + 视觉大模型人脸识别

> 基于 `spring-agent` 项目（Spring Boot 3.3.x + Spring AI 1.0.0-M6 + JDK 21）
>
> **本课程是上一次 Python 多轮对话课程的延续**。上次我们用 Python 直接调用 OpenAI 接口实现了多轮对话、函数调用等核心能力。这次咱们升级到 Java 体系，用 Spring AI 框架搭建一个智能体系统。

## 前言

上次课程中，咱们用 Python 实现了：多轮对话、上下文窗口管理、流式/非流式传输、函数调用等核心能力。这次咱们换个思路——**用 Spring AI 框架，从零搭建一个智能体系统**。

上次是用 Python 脚本直接调 API，这次是用企业级 Java 框架，通过面向对象的方式把每个 Agent 封装成独立的组件，让它们各司其职、协调工作。

咱们会从一个兵器铺的 NPC 场景出发，让师傅和徒弟两个角色通过多轮对话，完成一件兵器的锻造全过程。在这个过程中，你会看到：单个智能体怎么创建、多个智能体怎么协作、协调者怎么判断下一轮该谁说话、终止者怎么评估任务是否完成。

---

## 目录

| 章节 | 标题 | 状态 |
|------|------|------|
| 第一章 | Agent 框架概述与基本概念 | 完成 |
| 第二章 | 创建单一智能体 | 完成 |
| 第三章 | 兵器铺 NPC 多智能体交互 | 完成 |
| 第四章 | 使用视觉大模型实现人脸识别 | 完成 |

---

## 第一章 Agent 框架概述与基本概念

### 1.1 什么是 Agent（智能体）？

简单说，Agent 就是一个能**理解你的意图、自主完成任务**的智能程序。它不是简单地执行你写的死代码，而是能理解自然语言，自己决定怎么做。

用"去餐厅吃饭"来理解：

**普通程序** 就像自动售货机——你按哪个按钮，它就掉哪个东西，不会多也不会少。你投币选 A3，它就掉出一瓶可乐。你如果说"给我来瓶冰的"，它听不懂。

**Agent** 就像餐厅里的服务员——你坐下说一句"今天有点冷，想吃点暖和的"，服务员会理解你的意思，给你推荐热汤、火锅，甚至建议你"今天有新上的羊肉煲"。

```
你走进餐厅说："今天有点冷，想吃点暖和的"
        ↓
服务员（Agent）听懂了你的意图 —— 想吃热乎的
        ↓
翻看菜单和今日推荐（System Prompt），知道店里有什么
        ↓
想起上次你来吃的是牛肉面，这次推荐不同的（History）
        ↓
给你推荐："今天有羊肉煲，滋补暖胃，要不要试试？"
```

**关键区别**：普通程序只能执行预设好的指令，Agent 能理解模糊的意图并自主决策。

### 1.2 核心概念对照表

| 现实中的概念 | 在 Agent 框架中叫什么 | 通俗解释 |
|-------------|----------------------|----------|
| 员工工牌名 | `name()` | 比如 "翻译助手"、"翻译专家" |
| 工作手册 | `systemPrompt()` | 告诉 Agent "你是谁、你要做什么、你不能做什么" |
| 业务水平 | `model()` | 用哪个大模型，就像选哪个学历的员工 |
| 灵活程度 | `temperature()` | 0.0 = 死板但准确，1.0 = 创意丰富但可能跑偏 |
| 服务记忆 | `history` | 记住之前的对话，不会"失忆" |
| 顾客点单 | `chat(message)` | 给 Agent 发一句话，它返回一句话 |

### 1.3 单智能体 vs 多智能体

**单智能体**：就像一家小面馆，老板一个人包揽所有活。

```
顾客（用户） → 全能老板（Agent） → 端面给你
```

优点：简单、快、好沟通。缺点：遇到复杂需求（比如办一桌酒席），一个人干不了。

**多智能体**：就像一家大酒楼，有完整的分工。

```
顾客（用户） → 领班（Orchestrator）
                    │
                    ├──► 大厨（Agent A）→ 出菜单、规划菜序
                    ├──► 炒锅（Agent B）→ 炒菜执行
                    └──► 试菜员（Agent C）→ 尝菜把关
                               │
                          不合格 → 打回去重做
                          合格   → 端给顾客
```

优点：分工明确、各司其职、适合复杂任务。缺点：流程长、响应慢。

> 本课程只讲两个实战示例：单一智能体 和 兵器铺NPC交互。

### 1.4 Spring AI 是什么？

Spring AI 就是一个"餐厅管理工具包"，帮你：

- **招聘员工**：一行代码接入不同的大模型（ChatGPT、通义千问等）
- **统一标准**：统一用 `ChatClient` 接口，不管后端是什么模型
- **管理手册**：管理 System Prompt（工作手册），可以放在文件里统一维护
- **实时上菜**：支持流式输出，用户不用等完整结果就能看到实时输出

用 Spring AI 之后，只需要：
```java
String response = chatClient.prompt()
    .system("你是一个翻译助手")
    .user("你好")
    .call()
    .content();
```

### 1.5 项目的技术栈

| 工具 | 作用 |
|------|------|
| Spring Boot | 应用框架 |
| JDK 21 | Java 运行环境 |
| Spring AI | AI 框架 |
| 通义千问 | 大语言模型 |
| Maven | 构建工具 |

### 1.6 项目目录结构（快速浏览）

```
com.kevin.agents/
│
├── api/                    ← 对外接口（REST API）
│   └── AgentController     ← 接电话的"前台"
│
├── agent/                  ← 智能体核心
│   ├── BaseAgent           ← 员工模板（所有智能体的基类）
│   ├── single/             ← 单智能体
│   └── blacksmith/         ← NPC 交互示例（铁匠铺）
│       ├── MasterAgent     ← 师傅
│       ├── ApprenticeAgent ← 徒弟
│       ├── CoordinatorAgent← 协调者
│       └── TerminatorAgent ← 终止者
│
├── support/
│   └── PromptManager       ← 工作手册管理员
└── exception/
    └── ChatException       ← 异常处理
```

**记住一个核心**：所有智能体都继承 `BaseAgent`，就像所有员工都按同一个模板入职。

---

## 第二章 创建单一智能体

### 2.1 核心抽象：BaseAgent

所有智能体都继承自 `BaseAgent` 抽象类。新增一个智能体只需实现 4 个方法：

```java
// 文件：agent/BaseAgent.java

public abstract class BaseAgent {

    private final ChatClient chatClient;

    protected BaseAgent(ChatClient.Builder builder, PromptManager promptManager) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model())           // 子类定义：模型名称
                .temperature(temperature()) // 子类定义：创意度
                .build();
        this.chatClient = builder.defaultOptions(options).build();
    }

    // ===== 4 个必须实现的抽象方法 =====
    protected abstract String name();           // 智能体名称
    protected abstract String model();          // 模型名称
    protected abstract double temperature();    // 创意度 (0.0-1.0)
    protected abstract String systemPrompt();   // 系统提示词

    // ===== 工具方法 =====
    protected String chat(String userMessage) { ... }          // 同步调用
    public Flux<String> stream(String userMessage) { ... }      // 流式调用
}
```

**设计模式**：模板方法模式（Template Method）。父类定义了调用流程，子类只需实现 4 个抽象方法来定制行为。

### 2.2 实战：创建 SimpleSingleAgent

```java
// 文件：agent/single/SimpleSingleAgent.java

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
        return "aliyun/qwen3.6-plus";
    }

    @Override
    protected double temperature() {
        return 0.7;
    }

    @Override
    protected String systemPrompt() {
        // 方式一：从外部文件加载（推荐，方便修改和版本化）
        return promptManager.getPrompt("single/system_prompt", "你是一个专业的AI助手");
        // 方式二：内联定义
        // return "你是一个专业的AI助手，请用中文回答用户的问题。回答要简洁明了，有条理。";
    }

    public String chat(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息不能为空");
        }
        return chat(message);  // 调用父类的 chat 方法
    }
}
```

### 2.3 两种 System Prompt 加载方式

| 方式 | 优点 | 适用场景 |
|------|------|----------|
| **内联**（代码中直接写字符串） | 简单直接，IDE 友好 | 短提示词、快速原型 |
| **外部文件**（`resources/prompts/`） | 修改方便、支持版本管理 | 长提示词、生产环境 |

### 2.4 流式调用（推荐使用）

上次课程咱们已经对比过流式和非流式的区别。在 Spring AI 项目中，**推荐使用流式调用**，用户体验更好。

```java
// 流式调用（逐个 token 输出，打字机效果）
SseEmitter emitter = new SseEmitter(60000L); // 60 秒超时

agent.stream("你好")
    .doOnNext(token -> emitter.send(StreamEvent.chunk(token)))   // 每收到一个 token，立即推送
    .doOnComplete(() -> {
        emitter.send(StreamEvent.done());                         // 完成信号
        emitter.complete();
    })
    .doOnError(e -> {
        emitter.send(StreamEvent.error(e.getMessage()));          // 错误信号
        emitter.completeWithError(e);
    })
    .subscribe();

return emitter;
```

**前端接收 SSE 的效果**：用户看到文字一个字一个字往外蹦，不用等模型全部生成完。

### 2.5 REST API 集成（全流式）

```java
// 文件：api/AgentController.java

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private SingleAgentService singleAgentService;

    // 流式聊天
    @PostMapping("/single/stream")
    public SseEmitter stream(@RequestBody ChatRequest request) {
        return singleAgentService.streamChat(request.getMessage(), request.getSessionId());
    }
}
```

使用 `apifox` 测试

---

## 第三章 兵器铺 NPC 多智能体交互

### 3.1 场景设计

青云镇兵器铺，欧冶子（师傅）和小铁（徒弟）两位 NPC 角色。客官走进兵器铺，选择要打造的兵器，师徒开始对话、锻造兵器，最后客官决定是否支付。

```
═══════════════════════════════════════
        欢迎来到青云镇兵器铺
═══════════════════════════════════════

【欧师傅】 > 欢迎少侠光临青云铁匠铺！

【兵器谱】
  1 - 金箍棒
  2 - 芭蕉扇

【客  官】 > 请选一件: 1
【客  官】 > 帮我打造一根金箍棒，要能大能小，重一万三千五百斤！

───────────────────────────────────────
【欧师傅】 > （分析需求、报价、开始规划第一步...）
───────────────────────────────────────
【小  铁】 > （执行师傅指令：去库房取玄铁...）
───────────────────────────────────────
【欧师傅】 > （继续下一步：开炉锻造...）
...
═══════════════════════════════════════
             兵器已成
═══════════════════════════════════════

【小  铁】 > 少侠，兵器已为您打造完成，请支付银两。
              是否立即支付？(y/n): y

═══════════════════════════════════════
             客官慢走
═══════════════════════════════════════
```

### 3.2 四位角色一览

兵器铺有四位"员工"，各司其职：

| Agent | 角色标签 | 职责 | Temperature | 为什么不用替代方案 |
|-------|---------|------|-------------|-------------------|
| **欧师傅（MasterAgent）** | 决策者 | 技术专家，负责规划锻造工序 | 0.7（需要创意） | 编排器不写对话，只协调 |
| **小铁（ApprenticeAgent）** | 执行者 | 徒弟，执行师傅的指令 | 0.5（中规中矩） | 增加交互趣味性 |
| **协调者（CoordinatorAgent）** | 调度者 | 判断下一轮该谁发言 | 0.1（稳定可靠） | LLM 更智能，适应不同对话长度 |
| **终结者（TerminatorAgent）** | 裁判者 | 评估兵器是否真正完成 | 0.0（纯判断） | LLM 理解语义，比字符串匹配更准确 |

> **温度分层原则**：创造性角色（师傅、徒弟）温度高，判断性角色（协调者、终结者）温度低。

### 3.3 核心循环：一图看懂交互流程

```
用户选兵器 → 师傅分析需求
                    │
            ┌───────▼────────┐
            │  Coordinator   │ ← 读取历史，决定谁发言
            │  "下一轮该谁？" │
            └──┬─────────┬───┘
               │         │
        apprentice    master
               │         │
          ┌────▼───┐ ┌───▼────┐
          │  小铁   │ │  欧师傅 │
          │ 执行指令│ │ 继续工序│
          └────┬───┘ └───┬────┘
               │         │
               └────┬────┘
                    │
            （循环 N 轮）
                    │
          Coordinator 返回 terminate
                    │
            ┌───────▼────────┐
            │  Terminator    │ ← 评估：是否真正完成？
            │  "完成了吗？"   │
            └───┬────────┬───┘
         completed   continue
               │         │
          进入支付环节  继续循环
```

**编排器设计哲学**：编排器 **不决定流程走向**，它只是执行各 Agent 的判断结果。
- CoordinatorAgent 决定 **"谁该发言"**
- TerminatorAgent 决定 **"是否完成"**
- MasterAgent / ApprenticeAgent 负责 **"说什么话"**
- 各司其职，解耦清晰

### 3.4 真实对话时间线（打造金箍棒示例）

| 轮次 | 谁 | 输入 Context | 输出 |
|------|-----|-------------|------|
| 1 | 客官 | - | 选 1，要打造金箍棒 |
| 2 | 欧师傅 | "客官说：帮我打造金箍棒...请分析需求" | "需要玄铁、赤铜...小铁，去搬玄铁！" |
| 3 | Coordinator | 历史最后一条是欧师傅发的 | → apprentice |
| 4 | 小铁 | "师傅说...请根据指令执行" | "好的师傅！我去搬玄铁...材料备好了" |
| 5 | Coordinator | 历史最后一条是小铁发的 | → master |
| 6 | 欧师傅 | "徒弟已执行...请继续下一步" | "好！开始熔炉加热。小铁，用三昧真火煅烧！" |
| ... | ... | ... | ... |
| N | Coordinator | 师傅宣布"完成" | → terminate |
| N+1 | Terminator | 完整对话 + 客官需求 | → completed → 进入支付 |

### 3.5 CoordinatorAgent 决策逻辑

协调者是整个对话的"交通指挥员"，通过读取对话历史来决定下一轮该谁发言。

**决策规则**：

| 最后一条消息来源 | 返回结果 | 原因 |
|-----------------|---------|------|
| 欧师傅 | `apprentice` | 徒弟该执行指令了 |
| 小铁 | `master` | 师傅该给下一步指令了 |
| 欧师傅（含"完成"） | `terminate` | 可能该终止了，交由终结者评估 |

```java
// CoordinatorAgent.java
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
    return chat("对话历史：\n" + String.join("\n", history) + "\n\n下一轮该谁发言？");
}
```

> **关键点**：CoordinatorAgent 的 temperature = 0.1，接近确定性输出，保证调度逻辑稳定可靠。它只返回一个词，不做多余发挥。

### 3.6 TerminatorAgent 评估流程

终结者是"质量检查员"。当协调者认为该结束时，终结者会读取 **完整对话 + 客官原始需求**，做最终判断。

```
Coordinator 返回 terminate
        ↓
Terminator.judge(userNeed, history)
        ↓
分析：客官需求 vs 实际交付
        ↓
   ┌───┴────┐
   │        │
completed  continue
   │        │
退出循环  继续master发言
```

**为什么需要两个终止判断？**

CoordinatorAgent 只是根据"师傅说了完成"这个**信号**来初步判断；TerminatorAgent 才是真正读取全局上下文做**深度评估**。双重保险，防止师傅过早说"完成"导致流程中断。TerminatorAgent 的 temperature = 0.0，纯确定性输出，保证评估结果稳定、不随机。

```java
// TerminatorAgent.java
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
    return chat("客官需求：" + userNeed + "\n\n对话历史：\n"
        + String.join("\n", history) + "\n\n兵器是否已打造完成？返回 completed 或 continue。");
}
```

### 3.7 编排器核心循环

```java
// BlacksmithOrchestrator.java 主循环
int round = 1;
while (round <= MAX_ROUNDS) {
    // 1. 协调 Agent 判断下一轮该谁发言
    String nextSpeaker = coordinator.decide(history);
    String decision = nextSpeaker.trim().toLowerCase();

    // 2. 协调 Agent 认为该终止 → 交由终止 Agent 评估
    if (decision.contains("terminate")) {
        String result = terminator.judge(userNeed, history);
        if (result.trim().toLowerCase().contains("completed")) {
            System.out.println("═════ 兵器已成 ═════");
            break;
        }
        decision = "master";  // 还没完成，继续
    }

    // 3. 根据决定，调用对应 Agent
    if (decision.contains("apprentice")) {
        reply = apprentice.speak("师傅刚才说：" + reply + "...", history);
        printApprentice(reply);
        history.add("小铁 > " + reply);
    } else {
        reply = master.speak("徒弟已经执行了...请继续下一步。", history);
        printMaster(reply);
        history.add("欧师傅 > " + reply);
    }
    round++;
}
```

### 3.8 模拟付款环节（NPC与用户交互）

兵器打造完成后，客官需要选择是否支付。这是 **NPC 角色与用户直接交互** 的核心环节。

```java
// 编排器代码：支付环节
System.out.print("【小  铁】 > 少侠，兵器已为您打造完成，请支付银两。是否立即支付？(y/n): ");
String payChoice = scanner.nextLine().trim().toLowerCase();

if (payChoice.equals("y") || payChoice.equals("yes")) {
    // 客官同意支付 → 让徒弟确认收款 + 师傅送客
    String payConfirm = apprentice.speak(
        "客官同意支付了。请恭敬地确认收款，双手将兵器交付给客官，并叮嘱保养方法。",
        history);
    System.out.println("【小  铁】 > " + payConfirm);

    String farewell = master.speak(
        "兵器已交付，请说几句送客的话，表达江湖祝福。",
        history);
    printMaster(farewell);

} else {
    // 客官选择不支付 → 徒弟委婉提醒 + 师傅出面圆场
    System.out.println("【客  官】 > 暂时不付了，改日再来。");

    String reminder = apprentice.speak(
        "客官说暂时不付银两了。请委婉地提醒客官，兵器已打造完成，按规矩需要付清银两。",
        history);
    System.out.println("【小  铁】 > " + reminder);

    String masterWord = master.speak(
        "小铁提醒过客官了，但客官暂时不付。请出面说几句江湖话，表达理解和祝福。",
        history);
    printMaster(masterWord);
}
```

**这段代码的关键点**：
- 用户选择 **y**：触发师徒两个 Agent 的正常交付流程
- 用户选择 **n**：触发师徒两个 Agent 的"催款+圆场"流程
- 同一个 NPC，根据用户的不同选择，表现不同的行为，这就是 **多角色与用户交互** 的核心

### 3.9 本章小结

1. **4 个 Agent 各司其职**：师傅决策、徒弟执行、协调调度、终结评判
2. **编排器是导演**：不抢戏，只协调各角色按剧本出场
3. **消息传递链**：Coordinator → 判断谁发言 → 编排器调用对应 Agent → Agent 回复 → 打印 → 加入历史 → 下一轮
4. **双重终止保障**：Coordinator 初步判断 + Terminator 深度评估，防止误判
5. **温度分层**：创造性角色温度高，判断性角色温度低

---

## 第四章 使用视觉大模型实现人脸识别

### 4.1 目标

利用视觉大模型（通义千问 qwen3.6-plus）实现图片目标检测功能，包括：
- 人脸检测与定位
- 性别识别（男/女）
- 自动标注并返回标注图片

### 4.2 技术选型

我们选择的是 **通义千问 qwen3.6-plus**，它本身就具备视觉能力（Vision），不需要额外接入其他模型。配置只需在 `application.yml` 中一行搞定：

```yaml
spring:
  ai:
    openai:
      model: ${OPENAI_MODEL:aliyun/qwen3.6-plus}
```

### 4.3 提示词设计策略

在视觉大模型场景中，我们需要**两个提示词**协同工作：

**System Prompt（系统提示词）**：定义角色、规则、输出格式

```java
"你是一个专业的目标检测系统。你的任务是检测图片中所有人物的头部/面部。"
"图片实际尺寸：1920 像素宽 × 1080 像素高。"
"你必须基于这个实际尺寸，返回精确的像素坐标。"
"输出格式（纯 JSON 数组，不要任何 markdown 或解释）："
"[{\"bbox_2d\": [x1, y1, x2, y2], \"label\": \"男\"}, ...]"
"- x1, y1: 左上角坐标（像素，从 0 开始）"
"- x2, y2: 右下角坐标（像素，从 0 开始）"
"- label: 性别标签，只能是 \"男\" 或 \"女\""
"- 框要紧贴面部轮廓，不要包含身体、肩膀或背景"
```

**User Message（用户消息）**：具体任务指令 + 图片

```java
"请检测这张图片中所有的人脸位置（仅面部，不包括身体），" +
"以 JSON 格式输出坐标，并标注性别（男/女）。"
```

**为什么分两层？**
- System Prompt 是"工作手册"——定义规则和格式，告诉 LLM "你是谁、你要怎么做"
- User Message 是"具体任务"——告诉 LLM "现在要做这件事"
- 图片作为多模态内容附加在 User Message 中

### 4.4 核心模块

| 文件 | 职责 |
|------|------|
| `TargetAnnotationService.java` | 目标检测服务：调用 LLM，解析结果 |
| `FaceController.java` | REST API：接收图片上传，返回标注图片 |
| `TargetImageUtil.java` | 绘图工具：在原图上绘制框和标签 |

### 4.5 第一次实现：手动 Base64 拼接（失败）

最开始，我们像调用普通 API 一样，手动把图片 Base64 编码后拼到消息里：

```java
// ❌ 不推荐：手动编码
String base64Image = Base64.getEncoder().encodeToString(imageBytes);
String dataUrl = "data:image/jpeg;base64," + base64Image;
String userMessage = "请检测这张图片中的所有头部：" + dataUrl;
```

这能用，但代码丑，而且不符合 Spring AI 的最佳实践。

**第一次改进**：改用 Spring AI 框架自带的多模态 API：

```java
// ✓ 推荐：使用框架原生 Media + UserMessage
Media media = new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(imageBytes));
UserMessage userMessage = new UserMessage("请检测这张图片中的所有头部", List.of(media));

Prompt prompt = new Prompt(List.of(
        new SystemMessage(systemPrompt),
        userMessage
));

return chatClient.prompt(prompt).call().content();
```

**核心变化**：
- `Media` 是 Spring AI 的多模态内容类，位于 `org.springframework.ai.model.Media`
- 需要用 `ByteArrayResource` 包装字节数组
- `UserMessage` 的第二个参数接受 `List<Media>`
- 框架自动处理图片的编码和传输，开发者不需要关心底层细节

### 4.6 第二次踩坑：LLM 返回的坐标格式不对

LLM 返回的 JSON 长这样：

```json
[
    {"bbox_2d": [25, 244, 92, 364], "label": "女"},
    {"bbox_2d": [162, 272, 228, 396], "label": "男"}
]
```

但我们的 DTO 预期的是 `x, y, width, height` 格式。解析直接失败。

**解决方案**：让 DTO 完全匹配 LLM 的实际返回格式，不做任何转换假设。

```java
// DTO 字段必须和 LLM 返回的 JSON key 一致
private static class TargetDTO {
    public List<Integer> bbox_2d;  // [x1, y1, x2, y2]
    public String label;            // "男" 或 "女"

    public TargetInfo toTargetInfo(int imgWidth, int imgHeight) {
        // 转换逻辑见下一节
    }
}
```

**教训**：不要预设 LLM 会返回什么格式，先用日志打印出来看，再适配代码。

### 4.7 第三次踩坑：标注框偏移（核心问题）

标注画出来的框偏移严重，框的位置和实际人脸对不上。这是本次最关键的坑。

**根因分析**：通义千问 qwen3.6-plus（基于 Qwen-VL 系列）返回的坐标是 **归一化的 `[0, 1000]` 范围**，不是真实的像素值。

比如原图是 1920×1080，LLM 返回 `[250, 300, 400, 500]`，这不是像素坐标，而是：
- x1 = 250/1000 × 1920 = 480
- y1 = 300/1000 × 1080 = 324
- x2 = 400/1000 × 1920 = 768
- y2 = 500/1000 × 1080 = 540

之前直接用这些值画图，框自然就偏了。

**解决方案**：解析时按原图尺寸做比例转换：

```java
public TargetInfo toTargetInfo(int imgWidth, int imgHeight) {
    // Qwen3-VL 返回的是 [0, 1000] 归一化坐标，需转换为真实像素
    int x1 = (int) (bbox_2d.get(0) / 1000.0 * imgWidth);
    int y1 = (int) (bbox_2d.get(1) / 1000.0 * imgHeight);
    int x2 = (int) (bbox_2d.get(2) / 1000.0 * imgWidth);
    int y2 = (int) (bbox_2d.get(3) / 1000.0 * imgHeight);
    return new TargetInfo(x1, y1, x2, y2, normalizeLabel(label));
}
```

**完整的解析方法**：

```java
private List<TargetInfo> parseResponse(String response, int width, int height) {
    // 1. 清理 markdown 代码块
    String cleaned = response.trim()
            .replaceAll("^```(?:json)?\\s*", "")
            .replaceAll("\\s*```\\s*$", "");

    // 2. 提取 JSON 数组
    int jsonStart = cleaned.indexOf("[");
    int jsonEnd = cleaned.lastIndexOf("]");
    String json = cleaned.substring(jsonStart, jsonEnd + 1);

    // 3. 反序列化 + 坐标转换
    List<TargetDTO> dtos = objectMapper.readValue(json, new TypeReference<>() {});
    List<TargetInfo> targets = new ArrayList<>();
    for (TargetDTO dto : dtos) {
        targets.add(dto.toTargetInfo(width, height));
    }
    return targets;
}
```

### 4.8 标注颜色映射

不同类别用不同颜色区分，一目了然：

| 标签 | 颜色 | RGB |
|------|------|-----|
| 男生 | 蓝色 | (33, 150, 243) |
| 女生 | 粉色 | (233, 30, 99) |
| 猫 | 橙色 | (255, 152, 0) |
| 狗 | 绿色 | (76, 175, 80) |
| 动物 | 紫色 | (156, 39, 176) |

### 4.9 API 调用方式

apifox 调用测试

### 4.10 本章踩坑总结

| 问题 | 原因 | 解决 |
|------|------|------|
| Base64 手动拼接麻烦 | 没用框架 API | 改用 `Media` + `UserMessage` |
| DTO 解析失败 | LLM 返回 `bbox_2d` 而非 `x/y/width/height` | 字段名对齐 LLM |
| 标注框偏移 | Qwen3-VL 坐标是 [0,1000] 归一化值 | 按原图尺寸比例转换 |
| 标签"男/女"不匹配颜色 | 绘图工具期望"男生/女生" | DTO 中做标签归一化 |

**核心经验**：

1. **永远先打印 LLM 返回的原始数据**，不要假设格式
2. **视觉模型的坐标格式因模型而异**，使用前查官方文档确认
3. **Spring AI 的 `Media` 类自动处理图片传输**，不需要手动 Base64
4. **让代码适配 LLM，不要让 LLM 适配代码**

### 4.11 讲解要点（给讲师的提示）

结合代码讲解时，建议按以下顺序展开：

1. **先演示效果**：上传一张多人照片，展示标注结果，让听众看到直观的输出
2. **讲架构**：3 个文件各司其职 — Controller（接请求）→ Service（调 LLM + 解析）→ Util（画图）
3. **讲多模态 API**：对比手动 Base64 拼接 vs 框架原生 Media 的差异，强调 Spring AI 的便利性
4. **讲提示词设计**：System Prompt 定规则，User Message 下任务，图片作为多模态输入
5. **讲三个踩坑**（重点）：
   - DTO 字段名要对齐 LLM 返回的 JSON key（`bbox_2d` 而非 `x/y/width/height`）
   - Qwen3-VL 的 `[0,1000]` 归一化坐标 → 必须做比例转换（核心坑）
   - 标签颜色匹配：LLM 返回"男/女"，绘图工具期望"男生/女生" → 需要归一化
6. **总结核心经验**：永远先打印 LLM 原始返回再解析、视觉模型的坐标格式因模型而异

> **代码注释说明**：所有关键踩坑点在代码中都有 `⚠️` 标记的注释，讲课时可以快速定位到这些位置，方便说明。

---

## 最终总结

今天咱们从基础到实操，完整走了一遍 Spring AI Agent 框架的核心内容，要点可以总结为 3 个层面：

### 1. 基础概念层

Agent 就是能理解意图、自主完成任务的智能程序。核心概念：name（名字）、systemPrompt（工作手册）、model（能力水平）、temperature（创意度）。单智能体适合简单任务，多智能体适合复杂任务。

### 2. 基础开发层

掌握 Spring AI 的核心抽象 `BaseAgent`，新增智能体只需实现 4 个方法（`name`、`model`、`temperature`、`systemPrompt`）。使用流式调用提升用户体验，Prompt 管理推荐外部文件方式。

### 3. 实战升级层

通过兵器铺 NPC 场景，学会了多角色协作的完整流程：编排器协调、协调者调度、终结者评估、双重终止保障、模拟付款环节。理解不同角色的温度设置策略，以及 **编排器不决定流程走向、只执行各角色判断结果** 的设计哲学。

后续大家可以在此基础上做更多扩展：比如接入真实数据库持久化对话历史、添加更多 NPC 场景（客栈、药铺等）、实现 Web 界面替代控制台、集成函数调用让 Agent 能执行外部操作。

最后，大家对今天的内容有什么问题，现在可以提问交流。

---

## 附录

### A. 快速开始

```bash
# 1. 设置 JDK
export JAVA_HOME="D:\soft\BellSoft\LibericaJDK-21"
export PATH="D:\soft\BellSoft\LibericaJDK-21/bin:$PATH"

# 2. 构建项目
mvn clean install

# 3. 启动 Web 服务
mvn spring-boot:run

# 4. 运行 NPC 交互演示
# 在 IDE 中运行 BlacksmithConsoleApp.main()
```

### B. 设计模式总结

| 模式 | 应用位置 | 说明 |
|------|----------|------|
| **Template Method** | BaseAgent | 父类定义流程，子类实现 4 个抽象方法 |
| **Orchestrator** | BlacksmithOrchestrator | 集中编排多个 Agent |
| **Coordinator** | CoordinatorAgent | 动态决定流程走向 |
| **Circuit Breaker** | MAX_ROUNDS | 防止无限循环 |
| **Factory** | StreamEvent 静态工厂方法 | chunk(), done(), error() |
| **Caching** | PromptManager | Prompt 文件只加载一次 |

### C. 依赖关系图

```
AgentController ──► SingleAgentService ──► SimpleSingleAgent ──► BaseAgent
                                                                        ▲
                                                                        │
                                              ┌─────────────────────────┤
                                              │           │
                                          Master    Apprentice
                                          Agent      Agent
                                              │
                                              │
                                       Coordinator Terminator
                                       Agent    Agent

Support: PromptManager ←── 所有 BaseAgent 子类
```