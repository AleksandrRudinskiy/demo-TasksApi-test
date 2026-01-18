package com.example.demo.task;

import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserMapper;
import lombok.experimental.UtilityClass;

import java.util.List;


@UtilityClass
public class TaskMapper {
    public static Task convertToTask(TaskDto taskDto, User author) {
        return new Task(
                taskDto.getId(),
                taskDto.getTitle(),
                taskDto.getDescription(),
                taskDto.getStatus(),
                taskDto.getPriority(),
                author
        );
    }

    public static TaskDto convertToTaskDto(Task task, List<Long> commentsIds, UserDto performer) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                UserMapper.convertToUserDto(task.getAuthor()),
                performer,
                commentsIds
        );
    }

    public static TaskPerformer convertTaskPerformerFromTask(Task task, User performer) {
        return new TaskPerformer(
                0,
                task,
                performer
        );
    }

    public TaskShortDto convertToTaskForCommentsDto(Task task) {
        return new TaskShortDto(task.getId());
    }
}
