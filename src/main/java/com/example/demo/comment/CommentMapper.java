package com.example.demo.comment;

import com.example.demo.task.Task;
import com.example.demo.task.TaskMapper;
import com.example.demo.user.User;
import com.example.demo.user.UserMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommentMapper {
    public static Comment convertToComment(CommentDto commentDto,
                                           Task task,
                                           User commenter) {
        return new Comment(commentDto.getId(),
                commentDto.getBody(),
                task,
                commenter,
                commentDto.getCreated());
    }

    public static CommentDto convertToCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getBody(),
                TaskMapper.convertToTaskForCommentsDto(comment.getTask()),
                UserMapper.convertToUserShortDto(comment.getCommenter()),
                comment.getCreated()
        );
    }
}
