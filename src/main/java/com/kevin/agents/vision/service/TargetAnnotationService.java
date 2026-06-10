package com.kevin.agents.vision.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevin.agents.vision.util.TargetImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TargetAnnotationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public TargetAnnotationService(ChatClient.Builder builder, ObjectMapper objectMapper) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("mimo-v2.5")
                .temperature(0.0)
                .build();
        this.chatClient = builder.defaultOptions(options).build();
        this.objectMapper = objectMapper;
    }

    /**
     * 对图片进行目标检测和标注
     * @param imageBytes 原始图片字节
     * @return  标注后图片字节
     */
    public byte[] annotate(byte[] imageBytes) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new RuntimeException("图片数据不能为空");
        }

        // 1.读取原图尺寸
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new RuntimeException("无法读取图片");
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 2.调用 LLM 检测目标(传入尺寸)
        String response = callLLM(imageBytes, width, height);
        log.debug("LLM 响应：{}", response);

        // 3. 解析结果
        List<TargetInfo> targetInfos = parseResponse(response, width, height);
        log.info("检测到 {} 个目标", targetInfos.size());

        if (targetInfos.isEmpty()) {
            log.info("未检测到目标返回原图");
            return imageBytes;
        }

        // 打印坐标日志
        for (int i = 0; i < targetInfos.size(); i++) {
            TargetInfo targetInfo = targetInfos.get(i);
            log.info("  目标 {}：x1={}, y1={}, x2={}, y2={}, label:{}", i + 1, targetInfo.getX1(), targetInfo.getY1(), targetInfo.getX2(), targetInfo.getY2(), targetInfo.getLabel());
        }

        // 4.绘制标注
        return TargetImageUtil.drawAnnotations(imageBytes, targetInfos);
    }

    private String callLLM(byte[] imageBytes, int width, int height) {
        String systemPrompt = buildSystemPrompt(width, height);

        // 使用 Media 包装图片，UserMessage 支持文字 + 图片多模态输入
        Media media = new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(imageBytes));
        UserMessage userMessage = new UserMessage("请检测这张图片中所有的人脸位置（仅面部，不包括身体），以 JSON 格式输出坐标，并标注性别（男/女）。",
                List.of(media));

        // 将SystemMessage 和 UserMessage 组合为 Prompt 发送
        Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), userMessage));

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    private String buildSystemPrompt(int width, int height) {
        return String.format("""
        你是一个专业的目标检测系统。你的任务是检测图片中所有人物的头部/面部。
        
        图片实际尺寸：%d 像素宽 × %d 像素高。
        
        你必须基于这个实际尺寸，返回精确的像素坐标。
        
        输出格式（纯 JSON 数组，不要任何 markdown 或解释）：
        [
            {"bbox_2d": [x1, y1, x2, y2], "label": "男"},
            {"bbox_2d": [x1, y1, x2, y2], "label": "女"},
        ]
        
        - x1, y1: 左上角坐标（像素，从 0 开始）
        - x2, y2: 右下角坐标（像素，从 0 开始）
        - label: 性别标签，只能是 "男" 或 "女"
        - 框要紧贴面部轮廓，不要包含身体、肩膀或背景
        """, width, height);
    }

    private List<TargetInfo> parseResponse(String response, int width, int height) {
        try {
            String cleaned = response.trim()
                    .replaceAll("^```(?:json)?\\s*", "")
                    .replaceAll("\\s*```\\s*$", "");

            int jsonStart = cleaned.indexOf("[");
            int jsonEnd = cleaned.lastIndexOf("]");
            if (jsonStart == -1 || jsonEnd == -1) {
                return new ArrayList<>();
            }

            String json = cleaned.substring(jsonStart, jsonEnd + 1);
            List<Map<String, Object>> dtos = objectMapper.readValue(json, new TypeReference<>() {});

            List<TargetInfo> targets = new ArrayList<>();
            for (Map<String, Object> dto : dtos) {
                @SuppressWarnings("unchecked")
                List<Integer> bbox = (List<Integer>) dto.get("bbox_2d");
                String label = (String) dto.get("label");

                if (bbox != null && bbox.size() == 4) {
                    int x1 = (int) (bbox.get(0) / 1000.0 * width);
                    int y1 = (int) (bbox.get(1) / 1000.0 * height);
                    int x2 = (int) (bbox.get(2) / 1000.0 * width);
                    int y2 = (int) (bbox.get(3) / 1000.0 * height);

                    targets.add(new TargetInfo(x1, y1, x2, y2, normalizeLabel(label)));
                }
            }
            return targets;
        } catch (Exception e) {
            throw new RuntimeException("解析响应失败：" + e.getMessage(), e);
        }
    }

    private String normalizeLabel(String label) {
        if (label == null) {
            return "未知";
        }
        if (label.contains("男") || label.toLowerCase().contains("male")) {
            return "男生";
        } else if (label.contains("女") || label.toLowerCase().contains("female")) {
            return "女生";
        }
        return label;
    }
}
