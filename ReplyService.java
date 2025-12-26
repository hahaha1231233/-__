package com.demo.service;

import com.demo.model.Question;
import com.demo.model.Reply;
import com.demo.model.User;
import com.demo.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final QuestionService questionService;

    @Transactional
    public Reply addReply(Long questionId, String content, User user) {
        Question question = questionService.getQuestionById(questionId);

        Reply reply = new Reply();
        reply.setContent(content);
        reply.setUser(user);
        reply.setQuestion(question);

        return replyRepository.save(reply);
    }

    @Transactional
    public void deleteReply(Long replyId, User user) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("回复不存在"));

        if (!reply.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此回复");
        }

        replyRepository.delete(reply);
    }
}