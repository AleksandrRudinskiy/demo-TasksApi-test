package com.example.demo.task;

import com.example.demo.comment.CommentDto;
import com.example.demo.comment.CommentMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/tasks")
@RequiredArgsConstructor

public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Доступен только авторизованным пользователям")
    public TaskDto createTask(@RequestBody TaskDto taskDto) {
        log.info("POST-запрос на создание задачи");
        return taskService.createTask(taskDto);
    }

    @GetMapping
    @Operation(summary = "Доступен только авторизованным пользователям")
    public List<TaskDto> getPerformersTasks(@RequestParam(required = false) final Long performerId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET-запрос на получение задач пользователя с id {} в которых он является исполнителем", performerId);
        return taskService.getPerformersTasks(performerId, from, size);
    }

    @GetMapping("/{taskId}")
    public TaskDto getTaskById(@PathVariable long taskId) {
        log.info("GET-запрос задачи по id = {}", taskId);
        return taskService.getTaskById(taskId);
    }

    @PostMapping("/{taskId}/comment")
    @Operation(summary = "Доступен только авторизованным пользователям")
    public CommentDto addComment(@PathVariable Long taskId,
                                 @RequestBody CommentDto commentDto) {
        log.info("POST-запрос на добавления комментария {} к задаче id = {}", commentDto, taskId);
        return CommentMapper.convertToCommentDto(
                taskService.addComment(taskId, commentDto)
        );
    }

    @GetMapping("/{taskId}/comment")
    public List<CommentDto> getAllTaskComments(@PathVariable Long taskId) {
        log.info("GET-запрос на получение всех комментариев задачи с id {}", taskId);
        return taskService.getTaskComments(taskId);
    }

    //данную функцию должен выполнять только админ!!!
    /* написать тест на проверку авторизации пользователя как админа,
    в противном случае выдать исключение
    */
    @DeleteMapping("/{taskId}")
    public TaskDto deleteTaskById(@PathVariable Long taskId) {
        log.info("DELETE -запрос на удаление задачи по id {}", taskId);
        return taskService.deleteTaskById(taskId);
    }
}
