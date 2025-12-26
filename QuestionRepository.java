package com.demo.repository;

import com.demo.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByOrderByCreatedAtDesc();

    @Query("SELECT q FROM Question q JOIN FETCH q.user ORDER BY q.createdAt DESC")
    List<Question> findAllWithUser();
}