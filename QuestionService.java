package com.demo.service;

import com.demo.model.Question;
import com.demo.model.User;
import com.demo.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<Question> getAllQuestions() {
        return questionRepository.findAllWithUser();
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("问题不存在"));
    }

    public Question getQuestionWithReplies(Long id) {
        Question question = getQuestionById(id);
        // 触发懒加载，获取回复
        question.getReplies().size();
        return question;
    }

    @Transactional
    public Question createQuestion(String title, String content, User user) {
        Question question = new Question();
        question.setTitle(title);
        question.setContent(content);
        question.setUser(user);

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long questionId, User user) {
        Question question = getQuestionById(questionId);
        if (!question.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此问题");
        }
        questionRepository.delete(question);
    }
}