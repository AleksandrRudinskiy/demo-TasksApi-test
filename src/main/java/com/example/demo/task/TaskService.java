package com.example.demo.task;

import com.example.demo.comment.Comment;
import com.example.demo.comment.CommentDto;

public interface TaskService {

    TaskDto createTask(TaskDto taskDto, String authHeader);

    TaskDto getTaskById(long id);

    TaskDto patchTaskByAdmin(TaskDto taskDto, long taskId);

    Comment addComment(String authHeader, long taskId, CommentDto commentDto);
}
