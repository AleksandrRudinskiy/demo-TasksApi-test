package com.example.demo.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface TaskPerformerRepository extends JpaRepository<TaskPerformer, Long> {

    TaskPerformer getTaskPerformerByTaskId(long taskId);

    @Query(value = "select task_id from tasks_performers where performer_id = ?1", nativeQuery = true)
    List<Long> getTaskIdsByPerformerId(Long performerId);

}
