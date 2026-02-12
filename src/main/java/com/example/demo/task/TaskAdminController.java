package com.example.demo.task;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin")
@RequiredArgsConstructor
public class TaskAdminController {
    private final TaskService taskService;

    @PatchMapping("tasks/{taskId}")
    @Operation(summary = "Доступен только авторизованным пользователям с ролью ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskDto patchCompilation(@RequestBody TaskDto taskDto,
                                    @PathVariable Long taskId) {
        log.info("PATCH by admin with taskId = {} and body = {}", taskId, taskDto);
        return taskService.patchTaskByAdmin(taskDto, taskId);
    }

    /* написать тест в Postman на назначение исполнителя не администратором,
    предусмотрев генерацию специального исключения
     */

    /* написать метод на получение всех задач (доступ - только админ),
     в которых не назначен исполнитель
     */

    @GetMapping("admin/tasks")
    @Operation(summary = "Доступен только авторизованным пользователям с ролью ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TaskDto> getTasks(@RequestParam String taskStatus) {
        return null;
    }


}
