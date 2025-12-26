package com.demo.repository;

import com.demo.model.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByQuestionIdOrderByCreatedAtAsc(Long questionId);

    @Query("SELECT r FROM Reply r JOIN FETCH r.user WHERE r.question.id = :questionId ORDER BY r.createdAt ASC")
    List<Reply> findByQuestionIdWithUser(Long questionId);
}