package com.example.demo.task;

import com.example.demo.comment.CommentDto;
import com.example.demo.comment.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/tasks")
@RequiredArgsConstructor

public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TaskDto createTask(@RequestHeader(value = "Authorization") String authHeader,
                              @RequestBody TaskDto taskDto) {
        log.info("Заголовок авторизации {}", authHeader);
        log.info("POST-запрос на создание задачи");
        return taskService.createTask(taskDto, authHeader);
    }

    @GetMapping("/{taskId}")
    public TaskDto getTaskById(@RequestHeader(value = "Authorization") String authHeader,
                               @PathVariable long taskId) {
        log.info("Заголовок авторизации {}", authHeader);
        log.info("GET-запрос задачи по id = {}", taskId);
        return taskService.getTaskById(taskId);
    }

    @PatchMapping("/admin/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskDto patchCompilation(@RequestBody TaskDto taskDto,
                                    @PathVariable Long taskId) {
        log.info("PATCH by admin with taskId = {} and body = {}", taskId, taskDto);
        return taskService.patchTaskByAdmin(taskDto, taskId);
    }

    @PostMapping("/{taskId}/comment")
    public CommentDto addComment(@RequestHeader(value = "Authorization") String authHeader,
                                 @PathVariable Long taskId,
                                 @RequestBody CommentDto commentDto) {
        log.info("POST-запрос на добавления комментария к задаче {}", taskId);
        return CommentMapper.convertToCommentDto(
                taskService.addComment(authHeader, taskId, commentDto)
        );
    }
}
