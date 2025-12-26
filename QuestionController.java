package com.demo.controller;

import com.demo.model.User;
import com.demo.service.QuestionService;
import com.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;

    @GetMapping("/ask")
    public String askPage() {
        return "question/ask";
    }

    @PostMapping("/ask")
    public String askQuestion(
            @RequestParam String title,
            @RequestParam String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);

            questionService.createQuestion(title, content, user);

            redirectAttributes.addFlashAttribute("success", "问题发布成功！");
            return "redirect:/questions";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "发布失败: " + e.getMessage());
            return "redirect:/questions/ask";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteQuestion(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);

            questionService.deleteQuestion(id, user);

            redirectAttributes.addFlashAttribute("success", "问题删除成功！");
            return "redirect:/questions";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
            return "redirect:/questions/" + id;
        }
    }
}