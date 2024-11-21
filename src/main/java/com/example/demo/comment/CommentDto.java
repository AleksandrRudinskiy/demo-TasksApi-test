package com.example.demo.comment;

import com.example.demo.task.TaskShortDto;
import com.example.demo.user.UserShortDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private long id;
    private String body;
    private TaskShortDto task;
    private UserShortDto commenter;
    private LocalDateTime created;
}
