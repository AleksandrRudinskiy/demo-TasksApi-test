package com.example.demo;

import com.example.demo.task.*;
import com.example.demo.user.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TaskServiceTest {
    private final EntityManager em;
    private final TaskServiceImpl taskService;
    private final UserServiceImpl userService;
    private final String authHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9VU0VSIiwiaWQiOjEsImVtYWlsIjoiZW1haWxAeWFuZGV4LnJ1Iiwic3ViIjoidXNlcjEiLCJpYXQiOjE3Mzg3Nzg0MzksImV4cCI6MTczODkyMjQzOX0.BV_lQItjnUGApnoShr9O5meGs1A1SZp8C_63ZSObDwE";

    @Test
    void createTaskTest() {
        User newuser = makeUser(1L, "username", "password", "email@mail.ru", Role.ROLE_USER);
        userService.save(newuser);
        TaskDto newTaskDto = makeTaskDto(
                1L, "task", "some task", Status.PENDING, Priority.LOW,
                UserMapper.convertToUserDto(newuser), null, null);
        taskService.createTask(newTaskDto);
        Query query = em.createNativeQuery("Select * from tasks where title = :title", Task.class);
        Task task = (Task) query.setParameter("title", newTaskDto.getTitle()).getSingleResult();
        assertThat(task.getDescription(), equalTo(newTaskDto.getDescription()));
    }

    private User makeUser(
            Long id, String username, String password, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private TaskDto makeTaskDto(
            Long id, String title, String description, Status status, Priority priority, UserDto author,
            UserDto performer, List<Long> commentsIds
    ) {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(id);
        taskDto.setTitle(title);
        taskDto.setDescription(description);
        taskDto.setStatus(status);
        taskDto.setPriority(priority);
        taskDto.setAuthor(author);
        taskDto.setPerformer(performer);
        taskDto.setCommentsIds(commentsIds);
        return taskDto;
    }

}
