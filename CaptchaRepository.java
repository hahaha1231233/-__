// src/main/java/com/demo/repository/CaptchaRepository.java
package com.demo.repository;

import com.demo.model.Captcha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CaptchaRepository extends JpaRepository<Captcha, String> {

    // 查找未使用的验证码
    Optional<Captcha> findByCaptchaIdAndUsedFalse(String captchaId);

    // 标记验证码为已使用
    @Modifying
    @Transactional
    @Query("UPDATE Captcha c SET c.used = true WHERE c.captchaId = :captchaId")
    int markAsUsed(@Param("captchaId") String captchaId);
}