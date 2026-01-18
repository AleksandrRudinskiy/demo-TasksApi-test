package com.example.demo.task;

import com.example.demo.comment.Comment;
import com.example.demo.comment.CommentDto;

import java.util.List;

public interface TaskService {

    TaskDto createTask(TaskDto taskDto);

    TaskDto getTaskById(long id);

    TaskDto patchTaskByAdmin(TaskDto taskDto, long taskId);

    Comment addComment(long taskId, CommentDto commentDto);

    List<TaskDto> getPerformersTasks(Long performerId, int from, int size);

    List<CommentDto> getTaskComments(Long taskId);

    TaskDto deleteTaskById(Long taskId);
}
