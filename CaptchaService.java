// src/main/java/com/demo/service/CaptchaService.java
package com.demo.service;

import com.demo.model.Captcha;
import com.demo.repository.CaptchaRepository;
import com.demo.util.CaptchaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final CaptchaUtil captchaUtil;
    private final CaptchaRepository captchaRepository;

    /**
     * 生成验证码并保存到 H2 数据库
     */
    @Transactional
    public CaptchaUtil.CaptchaData generateCaptcha() {
        CaptchaUtil.CaptchaData captchaData = captchaUtil.generateCaptcha();

        // 保存到 H2 数据库
        Captcha captcha = new Captcha();
        captcha.setCaptchaId(captchaData.getCaptchaId());
        captcha.setCaptchaCode(captchaData.getCode());
        captcha.setBase64Image(captchaData.getBase64Image());
        captcha.setExpireTime(LocalDateTime.now().plusMinutes(5)); // 5分钟后过期
        captcha.setUsed(false);

        captchaRepository.save(captcha);

        System.out.println("✅ 验证码保存到H2数据库: " + captchaData.getCaptchaId() + ", 代码: " + captchaData.getCode());
        return captchaData;
    }

    /**
     * 验证验证码 - 从 H2 数据库查询
     */
    @Transactional
    public boolean validateCaptcha(String captchaId, String userInput) {
        System.out.println("🔍 验证验证码 - ID: " + captchaId + ", 用户输入: " + userInput);

        if (captchaId == null || userInput == null) {
            return false;
        }

        // 从 H2 数据库查询
        Captcha captcha = captchaRepository.findByCaptchaIdAndUsedFalse(captchaId)
                .orElse(null);

        if (captcha == null) {
            System.out.println("❌ 验证码不存在或已使用");
            return false;
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(captcha.getExpireTime())) {
            System.out.println("❌ 验证码已过期");
            captcha.setUsed(true);
            captchaRepository.save(captcha);
            return false;
        }

        // 比较验证码（忽略大小写）
        boolean isValid = captcha.getCaptchaCode().equalsIgnoreCase(userInput.trim());
        System.out.println("📊 验证结果: " + captcha.getCaptchaCode() + " vs " + userInput + " = " + isValid);

        if (isValid) {
            // 标记为已使用
            captcha.setUsed(true);
            captchaRepository.save(captcha);
            System.out.println("✅ 验证成功");
        }

        return isValid;
    }

    /**
     * 删除验证码
     */
    @Transactional
    public void removeCaptcha(String captchaId) {
        if (captchaId != null) {
            captchaRepository.markAsUsed(captchaId);
        }
    }
}