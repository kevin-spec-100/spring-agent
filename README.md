# Spring Agent - Multi-Agent Interaction System

> 基于 Spring AI 的多智能体交互系统实战教学项目
>
> A hands-on Spring AI tutorial project for building multi-agent systems

---

## 项目简介 | About

基于 **Spring Boot 3.3 + Spring AI 1.0** 的多智能体交互系统与视觉大模型人脸识别项目。本项目从零搭建一个智能体系统，涵盖单智能体创建、多智能体协作交互、以及基于视觉大模型的人脸/目标检测三大核心模块。

A multi-agent interaction system and vision-based face recognition project built with **Spring Boot 3.3 + Spring AI 1.0**. This project builds an agent system from scratch, covering three core modules: single agent creation, multi-agent collaboration, and vision-based face/target detection.

---

## 技术栈 | Tech Stack

| 技术 / Technology | 版本 / Version |
|-------------------|----------------|
| Spring Boot | 3.3.5 |
| Spring AI | 1.0.0-M6 |
| JDK | 21 |
| 大模型 / LLM | 通义千问 qwen3.6-plus |
| 构建工具 / Build Tool | Maven |
| Lombok | 简化实体类 / Simplify entity classes |

---

## 项目结构 | Project Structure

```
src/main/java/com/kevin/agents/
├── SpringAgentApplication.java          # 启动入口 / Application entry point
├── agent/
│   ├── BaseAgent.java                   # 智能体基类 / Agent base class (Template Method)
│   ├── single/
│   │   └── SimpleSingleAgent.java       # 单智能体示例 / Single agent example
│   ├── blacksmith/
│   │   ├── MasterAgent.java             # 兵器铺师傅 / Master NPC (decision maker)
│   │   ├── ApprenticeAgent.java         # 兵器铺徒弟 / Apprentice NPC (executor)
│   │   ├── CoordinatorAgent.java        # 协调者 / Coordinator (turn dispatcher)
│   │   └── TerminatorAgent.java         # 终结者 / Terminator (completion evaluator)
│   ├── orchestrator/
│   │   └── BlacksmithOrchestrator.java  # 多智能体编排器 / Multi-agent orchestrator
│   └── service/
│       └── SingleAgentService.java      # 单智能体服务 / Single agent service
├── api/
│   └── AgentController.java             # REST API 接口 / REST API controller
├── vision/
│   ├── controller/
│   │   └── FaceController.java          # 人脸识别 API / Face detection API
│   ├── service/
│   │   ├── TargetAnnotationService.java # 目标检测服务 / Target detection service
│   │   └── TargetInfo.java              # 目标信息 DTO / Target info DTO
│   └── util/
│       └── TargetImageUtil.java         # 图片标注绘图 / Image annotation utility
├── config/
│   └── SpringAiProperties.java          # AI 配置属性 / AI configuration properties
├── dto/
│   ├── ChatRequest.java                 # 聊天请求 DTO / Chat request DTO
│   └── ChatResponse.java               # 聊天响应 DTO / Chat response DTO
├── exception/
│   ├── ChatException.java              # 自定义异常 / Custom exception
│   └── GlobalExceptionHandler.java     # 全局异常处理 / Global exception handler
└── support/
    └── PromptManager.java              # Prompt 文件管理器 / Prompt file manager

src/main/resources/
├── application.yml                      # 应用配置 / Application configuration
└── prompts/                             # 外部化系统提示 / Externalized system prompts
    ├── single/system_prompt.txt
    └── blacksmith/
        ├── master.txt
        └── apprentice.txt
```

---

## 核心模块详解 | Core Modules

### 一、单智能体 | Single Agent

所有智能体继承自 `BaseAgent` 抽象类，新增一个智能体只需实现 4 个方法：

All agents extend the `BaseAgent` abstract class. Creating a new agent requires implementing only 4 methods:

| 方法 / Method | 说明 / Description |
|---------------|-------------------|
| `name()` | 智能体名称 / Agent name |
| `model()` | 使用的模型 / Model to use |
| `temperature()` | 创意度，范围 0.0-1.0 / Creativity level (0.0-1.0) |
| `systemPrompt()` | 系统提示词 / System prompt |

**设计模式 / Design Pattern:** 模板方法模式 (Template Method) — 父类定义调用流程，子类只负责具体实现。

The Template Method pattern — the parent defines the invocation flow, subclasses provide concrete implementations.

**调用方式 / Invocation Modes:**

- **同步调用 / Synchronous:** `chat(message)` 返回完整响应
- **流式调用 / Streaming:** `stream(message)` 返回 `Flux<String>`，配合 SSE 实现打字机效果，推荐用于前端

---

### 二、多智能体交互 | Multi-Agent Collaboration

青云镇兵器铺场景：欧冶子（师傅）和小铁（徒弟）两位 NPC 通过多轮对话协作，完成一件兵器的锻造全过程。

Blacksmith shop scenario: Master Ouyangzi and apprentice Xiaotie collaborate through multi-turn dialogue to forge a weapon from start to finish.

**四位角色分工 | Role Distribution:**

| Agent | 角色 / Role | 职责 / Responsibility | Temperature |
|-------|------------|----------------------|-------------|
| **MasterAgent** (欧师傅) | 决策者 / Decision Maker | 分析需求、规划锻造工序 / Analyze requirements, plan forging process | 0.7 |
| **ApprenticeAgent** (小铁) | 执行者 / Executor | 执行师傅指令 / Execute master's instructions | 0.5 |
| **CoordinatorAgent** | 调度者 / Dispatcher | 根据历史判断下一轮谁发言 / Decide who speaks next based on history | 0.1 |
| **TerminatorAgent** | 裁判者 / Evaluator | 读取完整对话，评估是否真正完成 / Evaluate if truly completed based on full context | 0.0 |

> **温度分层原则 / Temperature Principle:** 创造性角色（师傅、徒弟）温度高，判断性角色（协调者、终结者）温度低。
>
> Creative roles (master, apprentice) get higher temperature; judgmental roles (coordinator, terminator) get lower temperature.

**编排流程 | Orchestration Flow:**

```
用户选兵器 / User selects weapon
        │
        ▼
  师傅分析需求 / Master analyzes requirements
        │
        ▼
┌───────────────────────────┐
│  Coordinator 判断谁发言    │  ← 读历史，决定下一轮
│  (Coordinator decides     │
│   who speaks next)         │
└──────┬────────────┬────────┘
       │            │
   apprentice    master
   (小铁执行)    (欧师傅继续)
       │            │
       └──────┬─────┘
              │
     循环 N 轮 / Loop N rounds
              │
              ▼
  Coordinator 返回 "terminate"
              │
              ▼
┌───────────────────────────┐
│  Terminator 深度评估       │  ← 读取完整对话 + 客官需求
│  (Terminator evaluates    │
│   full context)            │
└──────┬────────────┬────────┘
  completed       continue
       │            │
  进入支付环节    继续循环
  (Payment)      (Continue)
```

**双重终止保障 | Double Termination Guard:**

| 阶段 / Stage | Agent | 判断方式 / How |
|-------------|-------|----------------|
| 初步判断 / Preliminary | CoordinatorAgent | 根据"师傅说了完成"信号 / Based on master's "done" signal |
| 深度评估 / Deep Evaluation | TerminatorAgent | 读取完整对话 + 客官原始需求 / Reads full dialogue + user's original request |

**支付环节 | Payment Phase:**

兵器打造完成后，用户选择是否支付 (y/n)，NPC 根据选择触发不同行为：

After forging is complete, user chooses to pay (y/n), triggering different NPC behaviors:

- **y (支付):** 徒弟确认收款 + 师傅送客祝福 / Apprentice confirms payment + Master gives farewell blessing
- **n (不支付):** 徒弟委婉催款 + 师傅出面圆场 / Apprentice politely reminds + Master smooths things over

---

### 三、视觉大模型人脸识别 | Vision-Based Face Recognition

利用通义千问 qwen3.6-plus 的视觉能力实现图片目标检测：

Leverage Qwen qwen3.6-plus vision capabilities for image target detection:

- **人脸检测与定位** / Face detection and localization
- **性别识别（男/女）** / Gender recognition (male/female)
- **自动标注并返回标注图片** / Auto annotation with labeled image output

**核心模块 | Core Components:**

| 文件 / File | 职责 / Responsibility |
|-------------|----------------------|
| `TargetAnnotationService.java` | 目标检测服务：调用 LLM、解析结果 / Detection service: call LLM, parse results |
| `FaceController.java` | REST API：接收图片上传，返回标注图片 / REST API: receive upload, return annotated image |
| `TargetImageUtil.java` | 绘图工具：在原图上绘制框和标签 / Drawing utility: draw boxes and labels on image |

**核心踩坑经验 | Key Lessons Learned:**

| 问题 / Problem | 原因 / Root Cause | 解决 / Solution |
|----------------|-------------------|-----------------|
| Base64 手动拼接麻烦 / Manual Base64 is messy | 没用框架 API / Didn't use framework API | 改用 `Media` + `UserMessage` / Use Spring AI's `Media` + `UserMessage` |
| DTO 解析失败 / DTO parse failure | LLM 返回 `bbox_2d` 而非 `x/y/width/height` / LLM returned `bbox_2d` not `x/y/width/height` | 字段名对齐 LLM 返回格式 / Align DTO fields to LLM's actual JSON keys |
| 标注框偏移 / Annotation box offset | Qwen3-VL 坐标是 `[0, 1000]` 归一化值 / Coordinates are `[0, 1000]` normalized values | 按原图尺寸比例转换 / Scale to actual image dimensions |
| 标签颜色不匹配 / Label color mismatch | LLM 返回"男/女"，绘图期望"男生/女生" / LLM returns "男/女", drawing expects "男生/女生" | DTO 中做标签归一化 / Normalize labels in DTO |

**多模态 API 最佳实践 / Multimodal API Best Practice:**

```java
// ✓ 推荐 / Recommended: 使用框架原生 Media 类 / Use framework-native Media class
Media media = new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(imageBytes));
UserMessage userMessage = new UserMessage("请检测人脸位置", List.of(media));
```

**核心经验 / Core Takeaway:** 永远先打印 LLM 返回的原始数据再解析，不要假设格式。让代码适配 LLM，不要让 LLM 适配代码。

Always print the LLM's raw response before parsing — never assume the format. Let code adapt to LLM, not the other way around.

---

## 快速开始 | Quick Start

### 环境要求 | Prerequisites

- JDK 21（推荐 LibericaJDK / Recommended: LibericaJDK）
- Maven 3.8+
- 通义千问 API Key / Qwen API Key

### 启动 | Run

```bash
# Windows 用户直接运行 / For Windows users
start.bat

# 或手动执行 / Or run manually
set JAVA_HOME=D:\soft\BellSoft\LibericaJDK-21
mvn spring-boot:run
```

### API 测试 | API Testing

使用 Apifox 测试以下接口 / Test endpoints with Apifox:

| 接口 / Endpoint | 方法 / Method | 说明 / Description |
|-----------------|---------------|-------------------|
| `/api/agent/single/stream` | POST | 单智能体流式聊天 / Single agent streaming chat |
| `/api/face/detect` | POST | 上传图片进行人脸检测与标注 / Upload image for face detection & annotation |

**请求体示例 / Request Example:**

```json
{
  "message": "你好，请介绍一下你自己"
}
```

---

## 设计模式总结 | Design Patterns

| 模式 / Pattern | 应用位置 / Location | 说明 / Description |
|----------------|-------------------|-------------------|
| **Template Method** | `BaseAgent` | 父类定义流程，子类实现 4 个抽象方法 / Parent defines flow, subclasses implement 4 abstract methods |
| **Orchestrator** | `BlacksmithOrchestrator` | 集中编排多个 Agent，不抢戏只做协调 / Centralized coordination without overriding agent logic |
| **Coordinator** | `CoordinatorAgent` | 动态决定流程走向，只返回一个词 / Dynamically decides next step, returns a single word |
| **Circuit Breaker** | `MAX_ROUNDS` | 防止无限循环的安全机制 / Safety mechanism to prevent infinite loops |
| **Factory** | `StreamEvent` | `chunk()`, `done()`, `error()` 静态工厂方法 / Static factory methods for SSE events |
| **Caching** | `PromptManager` | Prompt 文件只加载一次，缓存复用 / Prompt files loaded once and cached for reuse |

---

## 依赖关系 | Dependencies

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

Support: PromptManager ←── All BaseAgent subclasses (所有 BaseAgent 子类)
```

---

## 配置说明 | Configuration

在 `application.yml` 中配置大模型 API：

Configure the LLM API in `application.yml`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your-api-key-here}
      base-url: ${OPENAI_BASE_URL:https://dashscope.aliyuncs.com/compatible-mode}
      model: ${OPENAI_MODEL:aliyun/qwen3.6-plus}
```

---

## License

MIT
