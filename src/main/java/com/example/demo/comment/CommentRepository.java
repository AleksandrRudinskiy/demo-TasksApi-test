package com.example.demo.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = "select id from comments where task_id = ?1", nativeQuery = true)
    List<Long> getCommentsIdByTaskId(long taskId);
}
