package org.example;

import java.time.LocalDate;

class Task {
    private String name;
    private String description;
    private TaskType type;
    private int priority; // 1 - высокий, 2 - средний, 3 - низкий
    private LocalDate deadline;
    private TaskStatus status;
    private String[] tags;

    public Task(String name, String description, TaskType type, int priority, LocalDate deadline) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.priority = priority;
        this.deadline = deadline;
        this.status = TaskStatus.ACTIVE;
        this.tags = new String[0];
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Задача: " + name + "\n" +
                "Описание: " + description + "\n" +
                "Тип: " + type + "\n" +
                "Приоритет: " + priority + "\n" +
                "Срок выполнения: " + deadline + "\n" +
                "Статус: " + status + "\n" +
                "Теги: " + (tags.length == 0 ? "Нет" : String.join(", ", tags));
    }

    public void setType(TaskType type) {
        this.type = type;
    }
}