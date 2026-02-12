package com.example.demo.task;

import com.example.demo.comment.Comment;
import com.example.demo.comment.CommentDto;
import com.example.demo.comment.CommentMapper;
import com.example.demo.comment.CommentRepository;
import com.example.demo.exception.NotFoundException;
import com.example.demo.user.*;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskPerformerRepository taskPerformerRepository;
    private final CommentRepository commentRepository;
    private final UserServiceImpl userService;

    /**
     * Создание задачи (исполнитель назначается администратором)
     *
     * @return созданная задача
     */
    @Override
    public TaskDto createTask(TaskDto taskDto) {
        User author = userService.getCurrentUser();
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
        List<Long> commentsIds = commentRepository.getCommentsIdByTaskId(id);
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            if (taskPerformerRepository.getTaskPerformerByTaskId(id) != null) {
                User performer = taskPerformerRepository.getTaskPerformerByTaskId(id).getPerformer();
                return TaskMapper.convertToTaskDto(
                        taskOptional.get(), commentsIds,
                        UserMapper.convertToUserDto(performer));
            } else {
                return TaskMapper.convertToTaskDto(taskOptional.get(), commentsIds, null);
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

   /*
   Предусмотреть в логике, что если назначается исполнитель,
   то статус задачи меняется на IN_PROGRESS
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
            if (taskDto.getPriority() != null) {
                Priority priority = taskDto.getPriority();
                pachedTask.setPriority(priority);
            }
            TaskDto taskDto1 = TaskMapper.convertToTaskDto(taskRepository.save(pachedTask), new ArrayList<>(), null);
            if (taskDto.getPerformer() != null) {
                User performer = userRepository.findById(taskDto.getPerformer().getId()).get();
                if (taskPerformerRepository.getTaskPerformerByTaskId(taskId) != null) {
                    TaskPerformer taskPerformer = taskPerformerRepository.getTaskPerformerByTaskId(taskId);
                    taskPerformer.setTask(pachedTask);
                    taskPerformer.setPerformer(performer);
                    taskPerformerRepository.save(taskPerformer);
                    taskDto1.setPerformer(UserMapper.convertToUserDto(userRepository.findById(taskPerformer.getPerformer().getId()).get()));
                    taskDto1.setStatus(Status.IN_PROGRESS);
                } else {
                    TaskPerformer taskPerformer = TaskMapper.convertTaskPerformerFromTask(pachedTask, performer);
                    taskPerformerRepository.save(taskPerformer);
                    taskDto1.setPerformer(UserMapper.convertToUserDto(userRepository.findById(taskPerformer.getPerformer().getId()).get()));
                    taskDto1.setStatus(Status.IN_PROGRESS);
                }
                taskDto1.setCommentsIds(commentRepository.getCommentsIdByTaskId(taskId));
                taskRepository.save(TaskMapper.convertToTask(
                        taskDto1,
                       taskRepository.findById(taskDto1.getId()).get().getAuthor()));

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
    public Comment addComment(long taskId, CommentDto commentDto) {
        User commenter = userService.getCurrentUser();
        Task task;
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
    public List<TaskDto> getPerformersTasks(Long performerId, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        if (performerId == null) {
            return taskRepository.findAll().stream()
                    .map(task -> TaskMapper.convertToTaskDto(task, null, null))
                    .collect(Collectors.toList());
        } else {
            if (!userRepository.existsById(performerId)) {
                throw new NotFoundException("Исполнитель с id " + performerId + " не найден!");
            }
            return taskPerformerRepository.getTaskIdsByPerformerId(performerId, page).stream()
                    .map(taskId -> taskRepository.findById(taskId).isPresent() ? taskRepository.findById(taskId).get() : null)
                    .map(task -> TaskMapper.convertToTaskDto(task,
                            commentRepository.getCommentsIdByTaskId(task.getId()),
                            UserMapper.convertToUserDto(taskPerformerRepository.getTaskPerformerByTaskId(task.getId()).getPerformer())))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Получение списка всех комментариев к задаче по taskId
     *
     * @return список комментариев
     */
    @Override
    public List<CommentDto> getTaskComments(Long taskId) {
        int commentsCount = commentRepository.findByTaskId(taskId).size();
        log.info("Comments count of task id = {} is: {}", taskId, commentsCount);
        return commentRepository.findByTaskId(taskId).stream()
                .map(CommentMapper::convertToCommentDto)
                .collect(Collectors.toList());
    }

    /**
     * Удаление задачи по id
     *
     * @return удаленная задача
     */
    @Override
    public TaskDto deleteTaskById(Long taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        List<Long> commentsIds = commentRepository.getCommentsIdByTaskId(taskId);
        User performer = null;
        TaskDto taskDto;
        if (taskOptional.isEmpty()) {
            throw new NotFoundException("Task not present id = " + taskId);
        } else {
            taskDto = TaskMapper.convertToTaskDto(taskOptional.get(), commentsIds, null);
            if (taskPerformerRepository.getTaskPerformerByTaskId(taskId) != null) {
                performer = taskPerformerRepository.getTaskPerformerByTaskId(taskId).getPerformer();
                taskDto = TaskMapper.convertToTaskDto(taskOptional.get(), commentsIds, UserMapper.convertToUserDto(performer));
            }
            if (!commentsIds.isEmpty()) {
                commentsIds.forEach(commentRepository::deleteById);
            }
            if (performer != null) {
                TaskPerformer taskPerformer = taskPerformerRepository.getTaskPerformerByTaskId(taskId);
                taskPerformerRepository.deleteById(taskPerformer.getId());
            }
            taskRepository.deleteById(taskId);
            return taskDto;
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
