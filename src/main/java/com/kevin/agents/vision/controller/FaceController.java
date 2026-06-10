package com.kevin.agents.vision.controller;

import ch.qos.logback.classic.Logger;
import com.kevin.agents.vision.service.TargetAnnotationService;
import com.kevin.agents.vision.service.TargetInfo;
import com.kevin.agents.vision.util.TargetImageUtil;
import jdk.jfr.TransitionTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vision")
public class FaceController {

    /**
     * 支持的文件格式
     */
    public static final String[] ALLOWED_CONTENT_TYPES = {
      "image/jpeg", "image/jpg", "image/png", "image/webp"
    };

    /**
     * 最大文件大小：10M
     */
    public static final long MAX_FILE_SIZE = 1024 * 1024 * 10;

    private final TargetAnnotationService targetAnnotationService;

    public FaceController(TargetAnnotationService targetAnnotationService) {
        this.targetAnnotationService = targetAnnotationService;
    }

    @PostMapping("/recognize")
    public ResponseEntity<?> recognize(@RequestParam("image") MultipartFile image) {
        log.info("收到识别请求：filename={}， size={},contentType={}", image.getOriginalFilename(), image.getSize(), image.getContentType());

        try {
            // 1.参数校验
            String validationError = validateImage(image);
            if (validationError != null) {
                return ResponseEntity.badRequest().body(validationError);
            }

            // 2.调用标注服务
            byte[] imageBytes = image.getBytes();
            byte[] annotatedImageBytes = targetAnnotationService.annotate(imageBytes);

            log.info("标注完成");

            String outputFilename = image.getOriginalFilename();
            if (outputFilename != null && outputFilename.contains(".")) {
                outputFilename = "annotated_" + outputFilename.substring(0, outputFilename.lastIndexOf(".")) + ".png";
            } else {
                outputFilename = "annotated_image.png";
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFilename + "\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(annotatedImageBytes);
        } catch (Exception e) {
            log.error("识别失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }



    /**
     * 校验图片文件
     * @param image
     * @return
     */
    private String validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return "请上传图片文件";
        }

        if (image.getSize() > MAX_FILE_SIZE) {
            return "图片文件大小不能超过10M";
        }

        String contentType = image.getContentType();
        if (contentType == null) {
            return "无法识别图片格式";
        }

        boolean allowed = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            return "不支持的图片格式:" + contentType + "，仅支持 JPG、PNG、Webp";
        }

        return null;
    }
}
