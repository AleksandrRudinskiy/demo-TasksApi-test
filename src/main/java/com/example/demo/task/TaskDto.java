package com.example.demo.task;

import com.example.demo.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private UserDto author;
    private UserDto performer;
    private List<Long> commentsIds;
}
