package com.demo.controller;

import com.demo.model.User;
import com.demo.service.ReplyService;
import com.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/questions/{questionId}/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final UserService userService;

    @PostMapping
    public String addReply(
            @PathVariable Long questionId,
            @RequestParam String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);

            replyService.addReply(questionId, content, user);

            redirectAttributes.addFlashAttribute("success", "回复成功！");
            return "redirect:/questions/" + questionId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "回复失败: " + e.getMessage());
            return "redirect:/questions/" + questionId;
        }
    }

    @PostMapping("/{replyId}/delete")
    public String deleteReply(
            @PathVariable Long questionId,
            @PathVariable Long replyId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);

            replyService.deleteReply(replyId, user);

            redirectAttributes.addFlashAttribute("success", "回复删除成功！");
            return "redirect:/questions/" + questionId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
            return "redirect:/questions/" + questionId;
        }
    }
}