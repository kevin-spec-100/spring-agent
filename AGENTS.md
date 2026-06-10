# AGENTS.md

## Project Overview

Spring Boot 3.3 + Spring AI 1.0 multi-agent interaction system with vision-based face recognition.

## Quick Commands

```bash
# Build
mvn clean install

# Run (requires OPENAI_API_KEY env var)
mvn spring-boot:run

# Windows quick start
start.bat

# Run tests
mvn test
```

## Environment Setup

- **JDK 21** required (project targets Java 21)
- **Maven 3.8+**
- Set `OPENAI_API_KEY` environment variable (uses Aliyun Qwen model by default)
- Optional: `OPENAI_BASE_URL`, `OPENAI_MODEL` to override defaults

## Architecture

### Agent System (Template Method Pattern)

All agents extend `BaseAgent`. To create a new agent, implement 4 methods:
- `name()` - agent name
- `model()` - LLM model to use
- `temperature()` - creativity level (0.0-1.0)
- `systemPrompt()` - system prompt text

### Key Packages

```
com.kevin.agents
├── agent/          # Agent implementations (BaseAgent, single, blacksmith, orchestrator)
├── api/            # REST controllers for agent chat
├── vision/         # Face detection and image annotation
├── config/         # Spring AI configuration properties
├── dto/            # Request/response DTOs
├── exception/      # Global exception handling
└── support/        # PromptManager for loading prompt files
```

### Multi-Agent Orchestration

Blacksmith scenario uses 4 agents:
- **MasterAgent** (temp: 0.7) - decision maker
- **ApprenticeAgent** (temp: 0.5) - executor  
- **CoordinatorAgent** (temp: 0.1) - decides who speaks next
- **TerminatorAgent** (temp: 0.0) - evaluates completion

Double termination guard: Coordinator does preliminary check, Terminator does deep evaluation.

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/agent/chat` | POST | Synchronous chat |
| `/api/agent/single/stream` | POST | SSE streaming chat |
| `/api/vision/recognize` | POST | Image upload for face detection |

## Key Technical Details

### Spring AI Configuration

Uses Spring AI milestone repository (not GA). Config in `application.yml`:
```yaml
spring.ai.openai:
  base-url: ${OPENAI_BASE_URL:https://dashscope.aliyuncs.com/compatible-mode/v1}
  api-key: ${OPENAI_API_KEY:your-api-key-here}
  model: ${OPENAI_MODEL:aliyun/qwen3.6-plus}
```

### Vision Model Quirks

- Qwen3-VL returns coordinates as `[0, 1000]` normalized values, not pixel coordinates
- LLM returns `bbox_2d` field, not `x/y/width/height` - align DTOs to actual LLM output
- Label normalization needed: LLM returns "男/女", drawing expects "男生/女生"
- Always print raw LLM response before parsing - never assume format

### Multimodal API Pattern

```java
// Use framework-native Media class for image uploads
Media media = new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(imageBytes));
UserMessage userMessage = new UserMessage("prompt", List.of(media));
```

## File Locations

- Prompts: `src/main/resources/prompts/` (externalized, cached by PromptManager)
- Config: `src/main/resources/application.yml`
- Entry point: `SpringAgentApplication.java`

## Common Pitfalls

1. **Missing API key**: App won't start without `OPENAI_API_KEY`
2. **Coordinate system**: Vision model uses normalized coordinates, not pixels
3. **DTO field names**: Must match exact LLM JSON output (e.g., `bbox_2d`)
4. **Spring AI version**: Uses milestone (1.0.0-M6), not stable release
5. **File upload limits**: Max 10MB configured in application.yml

## Testing

Minimal test coverage - only `SpringAgentApplicationTests` exists (contextLoads). No unit tests for agents or vision services.

## Dependencies

Key: spring-boot-starter-web, spring-ai-openai-spring-boot-starter, lombok, jackson-databind, spring-boot-starter-validation
