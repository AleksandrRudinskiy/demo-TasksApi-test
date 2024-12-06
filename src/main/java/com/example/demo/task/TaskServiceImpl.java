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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    public static final String BEARER_PREFIX = "Bearer ";
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskPerformerRepository taskPerformerRepository;
    private final CommentRepository commentRepository;


    /**
     * Создание задачи
     *
     * @return созданная задача
     */
    @Override
    public TaskDto createTask(TaskDto taskDto, String authHeader) {
        String token = authHeader.substring(BEARER_PREFIX.length());
        long authorId = getAuthorIdFromToken(token);
        User author = null;
        if (userRepository.findById(authorId).isPresent()) {
            author = userRepository.findById(authorId).get();
        } else {
            throw new NotFoundException("Пользователя с id " + authorId + " не существует!");
        }
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

    /**
     * Получение задачи по id
     *
     * @return задача
     */
    @Override
    public TaskDto getTaskById(long id) {
        User performer;
        if (taskRepository.findById(id).isPresent()) {
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
        } else {
            throw new NotFoundException("Задача с id " + id + " не найдена");
        }
    }

    /**
     * Обновление полей задачи по id
     *
     * @return обновленная задача
     */
    @Override
    public TaskDto patchTaskByAdmin(TaskDto taskDto, long taskId) {
        if (taskRepository.findById(taskId).isPresent()) {
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
        } else {
            throw new NotFoundException("Задание с id = " + taskId + " не найдено.");
        }
    }


    /**
     * Создание комментария к задаче по taskId
     *
     * @return созданные комментарий
     */
    @Override
    public Comment addComment(String authHeader, long taskId, CommentDto commentDto) {
        String token = authHeader.substring(BEARER_PREFIX.length());
        long authorId = getAuthorIdFromToken(token);
        User commenter = null;
        if (userRepository.findById(authorId).isPresent()) {
            commenter = userRepository.findById(authorId).get();
        }
        Task task = null;
        if (taskRepository.findById(taskId).isPresent()) {
            task = taskRepository.findById(taskId).get();
        } else {
            throw new NotFoundException("Задача с id = " + taskId + " не найдена");
        }
        Comment comment = CommentMapper.convertToComment(commentDto, task, commenter);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }


    /**
     * Получение списка задач пользователя, в которых он является исполнителем
     *
     * @return список задач исполнителя
     */
    @Override
    public List<TaskDto> getPerformersTasks(Long performerId) {
        if (performerId == null) {
            return taskRepository.findAll().stream()
                    .map(task -> TaskMapper.convertToTaskDto(task, null, null))
                    .collect(Collectors.toList());
        } else {
            if (!userRepository.existsById(performerId)) {
                throw new NotFoundException("Исполнитель с id " + performerId + " не найден!");
            }
            return taskPerformerRepository.getTaskIdsByPerformerId(performerId).stream()
                    .map(taskId -> taskRepository.findById(taskId).isPresent() ? taskRepository.findById(taskId).get() : null)
                    .map(task -> TaskMapper.convertToTaskDto(task,
                            commentRepository.getCommentsIdByTaskId(task.getId()),
                            UserMapper.convertToUserDto(taskPerformerRepository.getTaskPerformerByTaskId(task.getId()).getPerformer())))
                    .collect(Collectors.toList());
        }

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
