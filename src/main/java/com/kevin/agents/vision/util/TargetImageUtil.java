package com.kevin.agents.vision.util;

import com.kevin.agents.vision.service.TargetInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static javax.swing.text.html.CSS.Attribute.FONT_SIZE;

public class TargetImageUtil {

    private static final Color MALE_COLOR = new Color(33, 150, 243);
    private static final Color FEMALE_COLOR = new Color(233, 30, 99);

    /**
     * 在图片上绘制目标框和类别标签
     * @param imageBytes    原始图片字节
     * @param targets   目标信息列表
     * @return  标注后的图片字节
     * @throws IOException
     */
    public static byte[] drawAnnotations(byte[] imageBytes, List<TargetInfo> targets) throws IOException {
        if (targets == null || targets.isEmpty()) {
            return imageBytes;
        }

        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new RuntimeException("无法读取图片");
        }
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 动态调整字体大小和框线宽度(根据图片尺寸自适应)
        // 原理：图片越大，文字和框线也应越大，否则看不清
        // frontSize 取图片短边的 3%，最小16px
        // boxStroke 为字号的 18%，最小 2px
        float fontSize = Math.max(16f, Math.min(width, height) * 0.03f);
        int boxStroke = Math.max(2, (int) (fontSize * 0.18f));

        Graphics2D g2d = originalImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font = new Font("Microsoft YaHei", Font.BOLD, (int) fontSize);

        for (TargetInfo target : targets) {
            Color color = getColorByLabel(target.getLabel());

            // 绘制矩形框
            g2d.setStroke(new BasicStroke(boxStroke));
            g2d.setColor(color);
            g2d.drawRect(target.getX1(), target.getY1(),target.getX2() - target.getX1(), target.getY2() - target.getY1());

            // 绘制标签(放在框的左上角)
            String label = target.getLabel();
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(label);
            int textHeight = fontMetrics.getHeight();

            int labelX = target.getX1();
            int labelY = target.getY1() - textHeight - 4;

            // 如果标签超出图片顶部，则移到框的下方显示
            if (labelY < 0) {
                labelY = target.getY2() + 4;
            }

            // 标签背景
            g2d.setColor(color);
            g2d.fillRect(labelX, labelY, textWidth + 10, textHeight + 4);

            // 标签文字
            g2d.setColor(Color.WHITE);
            g2d.drawString(label, labelX + 5, labelY + textHeight - 2);
        }

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "png", baos);
        return baos.toByteArray();
    }

    private static Color getColorByLabel(String label) {
        if (label == null) {
            return MALE_COLOR;
        }
        if (label.contains("女") || label.contains("女生")) {
            return FEMALE_COLOR;
        }
        return MALE_COLOR;
    }
}
