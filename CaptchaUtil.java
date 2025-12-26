// src/main/java/com/demo/util/CaptchaUtil.java
package com.demo.util;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

@Component
public class CaptchaUtil {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 45;
    private static final int CODE_COUNT = 4;
    private static final Random random = new Random();

    // 验证码字符集（排除易混淆字符）
    private static final String CHAR_SET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    @Data
    public static class CaptchaData {
        private final String captchaId;
        private final String code;
        private final String base64Image;
        private final long expireTime;

        public CaptchaData(String captchaId, String code, String base64Image) {
            this.captchaId = captchaId;
            this.code = code;
            this.base64Image = base64Image; // 已经是完整的 data:image/png;base64,xxxx
            this.expireTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5分钟过期
        }
    }

    public CaptchaData generateCaptcha() {
        String code = generateRandomCode();
        BufferedImage image = createCaptchaImage(code);
        String base64Image = convertToBase64(image);
        String captchaId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        System.out.println("生成验证码: ID=" + captchaId + ", Code=" + code);

        return new CaptchaData(captchaId, code, base64Image);
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_COUNT; i++) {
            code.append(CHAR_SET.charAt(random.nextInt(CHAR_SET.length())));
        }
        return code.toString();
    }

    private BufferedImage createCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 设置白色背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制渐变色背景
        GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 240, 245),
                WIDTH, HEIGHT, new Color(220, 220, 225));
        g.setPaint(gradient);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制干扰线
        drawInterferenceLines(g);

        // 绘制验证码
        drawCode(g, code);

        // 绘制边框
        g.setColor(new Color(200, 200, 210));
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        g.dispose();
        return image;
    }

    private void drawInterferenceLines(Graphics2D g) {
        // 绘制浅色干扰线
        g.setColor(new Color(180, 180, 200, 30));
        for (int i = 0; i < 8; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(x1, y1, x2, y2);
        }

        // 添加噪点
        g.setColor(new Color(150, 150, 170, 40));
        for (int i = 0; i < 80; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.fillOval(x, y, 1, 1);
        }
    }

    private void drawCode(Graphics2D g, String code) {
        g.setFont(new Font("Arial", Font.BOLD, 28));

        for (int i = 0; i < code.length(); i++) {
            // 随机生成深色，避免太浅看不清
            int r = random.nextInt(100);
            int gr = random.nextInt(100);
            int b = random.nextInt(100);
            g.setColor(new Color(r, gr, b));

            // 字符位置计算
            int x = 25 * i + 12;
            int y = 30;

            // 轻微旋转字符（-15度到15度之间）
            double rotation = (random.nextDouble() - 0.5) * 0.5;
            g.rotate(rotation, x + 10, y);

            // 绘制字符
            g.drawString(String.valueOf(code.charAt(i)), x, y);

            // 恢复旋转
            g.rotate(-rotation, x + 10, y);

            // 添加字符阴影效果
            g.setColor(new Color(r + 30, gr + 30, b + 30, 50));
            g.drawString(String.valueOf(code.charAt(i)), x + 1, y + 1);
        }
    }

    private String convertToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            // 返回完整的 data:image/png;base64,xxxx 格式
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("验证码生成失败", e);
        }
    }
}