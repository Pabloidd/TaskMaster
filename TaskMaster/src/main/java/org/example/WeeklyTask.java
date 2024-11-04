package org.example;

import java.time.LocalDate;

public class WeeklyTask extends Task {
    public WeeklyTask(String name, String description, TaskType type, int priority) {
        super(name, description, type, priority);
    }

    @Override
    public LocalDate getDeadline() {
        return null; // Возвращаем null, так как у ежемесячных задач нет дедлайна
    }
}