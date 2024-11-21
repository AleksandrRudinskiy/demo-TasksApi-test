package com.example.demo.task;

import com.example.demo.comment.Comment;
import com.example.demo.comment.CommentDto;
import com.example.demo.comment.CommentMapper;
import com.example.demo.comment.CommentRepository;
import com.example.demo.exception.NotFoundException;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserMapper;
import com.example.demo.user.UserRepository;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

@Service
@Slf4j
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    public static final String BEARER_PREFIX = "Bearer ";
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskPerformerRepository taskPerformerRepository;
    private final CommentRepository commentRepository;

    @Override
    public TaskDto createTask(TaskDto taskDto, String authHeader) {
        String token = authHeader.substring(BEARER_PREFIX.length());
        long authorId = getAuthorIdFromToken(token);
        User author = userRepository.findById(authorId).get();
        UserDto authorDto = UserMapper.convertToUserDto(
                author);
        taskDto.setAuthor(authorDto);
        if (taskDto.getStatus() == null) {
            taskDto.setStatus(Status.PENDING);
        }
        if (taskDto.getPriority() == null) {
            taskDto.setPriority(Priority.LOW);
        }
        return TaskMapper.convertToTaskDto(
                taskRepository.save(TaskMapper.convertToTask(taskDto, author)),
                new ArrayList<>(),
                null
        );
    }

    @Override
    public TaskDto getTaskById(long id) {
        User performer = null;
        if (taskPerformerRepository.getTaskPerformerByTaskId(id) != null) {
            performer = taskPerformerRepository.getTaskPerformerByTaskId(id).getPerformer();
            return TaskMapper.convertToTaskDto(
                    taskRepository.findById(id).get(), commentRepository.getCommentsIdByTaskId(id),
                    UserMapper.convertToUserDto(performer));
        } else {
            return TaskMapper.convertToTaskDto(
                    taskRepository.findById(id).get(), commentRepository.getCommentsIdByTaskId(id),
                    null
            );
        }
    }

    @Override
    public TaskDto patchTaskByAdmin(TaskDto taskDto, long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Задание с id = " + taskId + " не найдено.");
        }
        Task pachedTask = taskRepository.findById(taskId).get();
        if (taskDto.getStatus() != null) {
            Status status = taskDto.getStatus();
            pachedTask.setStatus(status);
        }
        if (taskDto.getStatus() != null) {
            Priority priority = taskDto.getPriority();
            pachedTask.setPriority(priority);
        }
        if (taskDto.getTitle() != null) {
            String title = taskDto.getTitle();
            pachedTask.setTitle(title);
        }
        if (taskDto.getDescription() != null) {
            String description = taskDto.getDescription();
            pachedTask.setDescription(description);
        }
        TaskDto taskDto1 = TaskMapper.convertToTaskDto(taskRepository.save(pachedTask), new ArrayList<>(), null);
        if (taskDto.getPerformer() != null) {
            User performer = userRepository.findById(taskDto.getPerformer().getId()).get();
            log.info("performer = {}", taskDto.getPerformer());
            if (taskPerformerRepository.getTaskPerformerByTaskId(taskId) != null) {
                TaskPerformer taskPerformer = taskPerformerRepository.getTaskPerformerByTaskId(taskId);
                taskPerformer.setTask(pachedTask);
                taskPerformer.setPerformer(performer);
                taskPerformerRepository.save(taskPerformer);
                taskDto1.setPerformer(UserMapper.convertToUserDto(userRepository.findById(taskPerformer.getPerformer().getId()).get()));
            } else {
                TaskPerformer taskPerformer = TaskMapper.convertTaskPerformerFromTask(pachedTask, performer);
                taskPerformerRepository.save(taskPerformer);
                taskDto1.setPerformer(UserMapper.convertToUserDto(userRepository.findById(taskPerformer.getPerformer().getId()).get()));
            }
taskDto1.setCommentsIds(commentRepository.getCommentsIdByTaskId(taskId));
        }
        return taskDto1;
    }

    @Override
    public Comment addComment(String authHeader, long taskId, CommentDto commentDto) {
        String token = authHeader.substring(BEARER_PREFIX.length());
        long authorId = getAuthorIdFromToken(token);
        User commenter = userRepository.findById(authorId).get();
        Task task = taskRepository.findById(taskId).get();
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Задача с id = " + taskId + " не найдена");
        }
        Comment comment = CommentMapper.convertToComment(commentDto, task, commenter);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    private long getAuthorIdFromToken(String token) {
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        log.info("header {}", header);
        log.info("payload {}", payload);
        User performer = new Gson().fromJson(payload, User.class);
        log.info("текущий пользователь {}", performer);
        return performer.getId();
    }
}
