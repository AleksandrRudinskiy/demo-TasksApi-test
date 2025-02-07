package com.example.demo.task;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/admin")
@RequiredArgsConstructor
public class TaskAdminController {
    private final TaskService taskService;

    @PatchMapping("/tasks/{taskId}")
    @Operation(summary = "Доступен только авторизованным пользователям с ролью ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskDto patchCompilation(@RequestBody TaskDto taskDto,
                                    @PathVariable Long taskId) {
        log.info("PATCH by admin with taskId = {} and body = {}", taskId, taskDto);
        return taskService.patchTaskByAdmin(taskDto, taskId);
    }
}
