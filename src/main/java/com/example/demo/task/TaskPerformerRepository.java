package com.example.demo.task;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TaskPerformerRepository extends JpaRepository<TaskPerformer, Long> {
    TaskPerformer getTaskPerformerByTaskId(long taskId);

}
