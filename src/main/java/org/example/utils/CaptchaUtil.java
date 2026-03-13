package org.example.utils;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码生成工具类
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
public class CaptchaUtil {

    private static final String CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final Random RANDOM = new Random();

    /**
     * 生成验证码
     *
     * @return 验证码信息和Base64图片
     */
    public static JSONObject generateCaptcha() {
        // 创建BufferedImage对象
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        // 获取Graphics2D对象
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 填充背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 生成随机验证码
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        // 绘制验证码
        for (int i = 0; i < CODE_LENGTH; i++) {
            // 设置随机颜色
            g.setColor(getRandomColor());
            // 设置随机字体
            g.setFont(new Font("Arial", Font.BOLD, 28));
            // 绘制字符
            g.drawString(String.valueOf(code.charAt(i)), 20 * i + 20, 30);
        }

        // 添加干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(getRandomColor());
            g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT),
                    RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
        }

        // 添加干扰点
        for (int i = 0; i < 50; i++) {
            g.setColor(getRandomColor());
            g.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
        }

        g.dispose();

        // 转换为Base64
        String base64Image = imageToBase64(image);

        JSONObject result = new JSONObject();
        result.put("code", code.toString());
        result.put("image", base64Image);

        return result;
    }

    /**
     * 获取随机颜色
     */
    private static Color getRandomColor() {
        return new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    /**
     * 将BufferedImage转换为Base64字符串
     */
    private static String imageToBase64(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            log.error("验证码图片转换失败", e);
        }
        byte[] imageBytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
}
