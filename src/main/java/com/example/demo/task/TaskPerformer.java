package com.example.demo.task;

import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tasks_performers", schema = "public")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskPerformer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @ManyToOne
    @JoinColumn(name = "performer_id", nullable = false)
    @ToString.Exclude
    private User performer;
}
