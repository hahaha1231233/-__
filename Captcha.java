// src/main/java/com/demo/model/Captcha.java
package com.demo.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "captchas")
public class Captcha {

    @Id
    @Column(name = "captcha_id", length = 50, nullable = false)
    private String captchaId; // 验证码ID

    @Column(name = "captcha_code", length = 10, nullable = false)
    private String captchaCode; // 验证码内容

    @Column(name = "base64_image", columnDefinition = "TEXT")
    private String base64Image; // 验证码图片（Base64格式）

    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime; // 过期时间

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_used", nullable = false)
    private Boolean used = false; // 是否已使用

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}