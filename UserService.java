// src/main/java/com/demo/service/UserService.java
package com.demo.service;

import com.demo.model.User;
import com.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("尝试加载用户: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });

        log.info("用户加载成功: {}", username);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Transactional
    public void registerUser(String username, String password, String email) {
        log.info("开始注册用户: {}, 邮箱: {}", username, email);

        if (username == null || username.trim().isEmpty()) {
            log.error("注册失败: 用户名为空");
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            log.error("注册失败: 密码为空");
            throw new RuntimeException("密码不能为空");
        }

        if (userRepository.existsByUsername(username.trim())) {
            log.error("注册失败: 用户名已存在 - {}", username);
            throw new RuntimeException("用户名已存在");
        }
        if (email != null && !email.trim().isEmpty() && userRepository.existsByEmail(email.trim())) {
            log.error("注册失败: 邮箱已被注册 - {}", email);
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password.trim()));
        user.setEmail(email != null ? email.trim() : null);

        userRepository.save(user);
        log.info("用户注册成功: {}", username);
    }

    public User getUserByUsername(String username) {
        log.info("获取用户信息: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", username);
                    return new RuntimeException("用户不存在");
                });
    }
}