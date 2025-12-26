// src/main/java/com/demo/controller/MainController.java
package com.demo.controller;

import com.demo.dto.RegisterRequest;
import com.demo.model.Question;
import com.demo.service.CaptchaService;
import com.demo.service.QuestionService;
import com.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final QuestionService questionService;
    private final UserService userService;
    private final CaptchaService captchaService;

    @GetMapping("/")
    public String home() {
        return "redirect:/questions";
    }

    @GetMapping("/questions")
    public String questionList(Model model) {
        model.addAttribute("questions", questionService.getAllQuestions());
        return "question/list";
    }

    @GetMapping("/questions/{id}")
    public String questionDetail(@PathVariable Long id, Model model) {
        Question question = questionService.getQuestionWithReplies(id);
        model.addAttribute("question", question);
        return "question/detail";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model, HttpSession session) {

        // 清空之前的消息
        model.asMap().remove("error");
        model.asMap().remove("message");

        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
            log.info("登录页面显示错误: 用户名或密码错误");
        }
        if (logout != null) {
            model.addAttribute("message", "您已成功退出");
            log.info("登录页面显示消息: 您已成功退出");
        }

        // 生成验证码
        try {
            var captchaData = captchaService.generateCaptcha();
            session.setAttribute("captchaId", captchaData.getCaptchaId());

            model.addAttribute("captchaId", captchaData.getCaptchaId());
            model.addAttribute("captchaImage", captchaData.getBase64Image());

            log.info("登录页面生成验证码: {}", captchaData.getCaptchaId());
        } catch (Exception e) {
            log.error("生成验证码失败: {}", e.getMessage());
            model.addAttribute("error", "系统错误，请刷新页面");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        model.addAttribute("registerRequest", new RegisterRequest());

        // 生成验证码
        try {
            var captchaData = captchaService.generateCaptcha();
            session.setAttribute("captchaId", captchaData.getCaptchaId());

            model.addAttribute("captchaId", captchaData.getCaptchaId());
            model.addAttribute("captchaImage", captchaData.getBase64Image());

            log.info("注册页面生成验证码: {}", captchaData.getCaptchaId());
        } catch (Exception e) {
            log.error("生成验证码失败: {}", e.getMessage());
            model.addAttribute("error", "系统错误，请刷新页面");
        }

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        log.info("开始注册 - 用户名: {}, 邮箱: {}",
                registerRequest.getUsername(),
                registerRequest.getEmail());

        // 1. 验证验证码
        String sessionCaptchaId = (String) session.getAttribute("captchaId");
        log.info("验证码验证 - Session ID: {}, 请求 ID: {}",
                sessionCaptchaId,
                registerRequest.getCaptchaId());

        boolean isValidCaptcha = false;
        String captchaErrorMessage = "验证码错误或已过期";

        if (sessionCaptchaId != null &&
                sessionCaptchaId.equals(registerRequest.getCaptchaId()) &&
                registerRequest.getCaptcha() != null &&
                !registerRequest.getCaptcha().trim().isEmpty()) {

            isValidCaptcha = captchaService.validateCaptcha(
                    sessionCaptchaId,
                    registerRequest.getCaptcha().trim()
            );

            log.info("验证码验证结果: {}", isValidCaptcha);
        } else {
            if (sessionCaptchaId == null) {
                captchaErrorMessage = "验证码已过期，请刷新页面重试";
            } else if (registerRequest.getCaptcha() == null || registerRequest.getCaptcha().trim().isEmpty()) {
                captchaErrorMessage = "请输入验证码";
            } else {
                captchaErrorMessage = "验证码会话不一致，请刷新页面重试";
            }
            log.warn("验证码验证失败: {}", captchaErrorMessage);
        }

        if (!isValidCaptcha) {
            bindingResult.rejectValue("captcha", "error.captcha", captchaErrorMessage);
        }

        // 2. 验证密码确认
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "两次输入的密码不一致");
            log.warn("密码不匹配");
        }

        // 3. 如果有错误，重新生成验证码并返回
        if (bindingResult.hasErrors()) {
            log.info("表单验证失败，错误数量: {}", bindingResult.getErrorCount());

            // 重新生成验证码
            try {
                var newCaptchaData = captchaService.generateCaptcha();
                session.setAttribute("captchaId", newCaptchaData.getCaptchaId());

                model.addAttribute("captchaId", newCaptchaData.getCaptchaId());
                model.addAttribute("captchaImage", newCaptchaData.getBase64Image());
                model.addAttribute("registerRequest", registerRequest);
            } catch (Exception e) {
                log.error("重新生成验证码失败: {}", e.getMessage());
                model.addAttribute("error", "系统错误，请刷新页面");
            }

            return "auth/register";
        }

        // 4. 注册用户
        try {
            userService.registerUser(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getEmail()
            );

            // 清除Session中的验证码ID
            session.removeAttribute("captchaId");

            // 可选：标记验证码为已使用（虽然验证时已经标记了）
            if (sessionCaptchaId != null) {
                captchaService.removeCaptcha(sessionCaptchaId);
            }

            log.info("用户注册成功: {}", registerRequest.getUsername());
            redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
            return "redirect:/login";

        } catch (RuntimeException e) {
            log.error("注册失败: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());

            // 重新生成验证码
            try {
                var newCaptchaData = captchaService.generateCaptcha();
                session.setAttribute("captchaId", newCaptchaData.getCaptchaId());

                model.addAttribute("captchaId", newCaptchaData.getCaptchaId());
                model.addAttribute("captchaImage", newCaptchaData.getBase64Image());
                model.addAttribute("registerRequest", registerRequest);
            } catch (Exception ex) {
                log.error("重新生成验证码失败: {}", ex.getMessage());
                model.addAttribute("error", "系统错误，请刷新页面");
            }

            return "auth/register";
        }
    }

    @GetMapping("/captcha/refresh")
    @ResponseBody
    public Map<String, String> refreshCaptcha(HttpSession session) {
        log.info("收到验证码刷新请求");

        Map<String, String> result = new HashMap<>();

        try {
            // 清除旧的验证码
            String oldCaptchaId = (String) session.getAttribute("captchaId");
            if (oldCaptchaId != null) {
                captchaService.removeCaptcha(oldCaptchaId);
                log.info("移除旧验证码: {}", oldCaptchaId);
            }

            // 生成新的验证码
            var captchaData = captchaService.generateCaptcha();
            session.setAttribute("captchaId", captchaData.getCaptchaId());

            log.info("生成新验证码: {}", captchaData.getCaptchaId());

            result.put("captchaId", captchaData.getCaptchaId());
            result.put("captchaImage", captchaData.getBase64Image());
            result.put("success", "true");
            result.put("message", "验证码刷新成功");

        } catch (Exception e) {
            log.error("刷新验证码失败: {}", e.getMessage());
            result.put("success", "false");
            result.put("message", "验证码刷新失败: " + e.getMessage());
        }

        return result;
    }

    @GetMapping("/captcha/debug")
    @ResponseBody
    public Map<String, Object> debugCaptcha(HttpSession session) {
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("sessionId", session.getId());
        debugInfo.put("sessionCaptchaId", session.getAttribute("captchaId"));
        debugInfo.put("timestamp", System.currentTimeMillis());
        debugInfo.put("status", "ok");
        return debugInfo;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        try {
            model.addAttribute("user", userService.getUserByUsername(username));
            return "auth/profile";
        } catch (RuntimeException e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return "redirect:/login";
        }
    }

    // src/main/java/com/demo/controller/MainController.java
// 在类的末尾添加登录POST方法（在现有方法之后）
    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String captcha,
                              @RequestParam String captchaId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        log.info("登录尝试 - 用户: {}, 验证码ID: {}", username, captchaId);

        // 验证验证码
        String sessionCaptchaId = (String) session.getAttribute("captchaId");
        if (sessionCaptchaId == null || !sessionCaptchaId.equals(captchaId)) {
            redirectAttributes.addFlashAttribute("error", "验证码会话已过期");
            return "redirect:/login";
        }

        boolean isValidCaptcha = captchaService.validateCaptcha(captchaId, captcha);
        if (!isValidCaptcha) {
            redirectAttributes.addFlashAttribute("error", "验证码错误");
            return "redirect:/login";
        }

        // 验证码验证成功，清除session中的验证码
        session.removeAttribute("captchaId");

        // 注意：实际的用户名密码验证由Spring Security处理
        // 这里只需要重定向到Spring Security的登录处理
        return "redirect:/login?username=" + username;
    }
}
// 注意：这里没有多余的 }，保持类的正确结束