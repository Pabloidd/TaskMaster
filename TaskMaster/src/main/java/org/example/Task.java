 package org.example;

import java.time.LocalDate;

public class Task {
    private String name;
    private String description;
    private TaskType type;
    private int priority; // 1 - высокий, 2 - средний, 3 - низкий
    private LocalDate deadline;

    public Task(String name, String description, TaskType type, int priority, LocalDate deadline) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.priority = priority;
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskType getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return "Задача: " + name  +
                "Описание: " + description  +
                "Тип: " + type  +
                "Приоритет: " + priority  +
                "Срок выполнения: " + deadline;
    }

    public void setType(TaskType type) {
        this.type = type;
    }
}



