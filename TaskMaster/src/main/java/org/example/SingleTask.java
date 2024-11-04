package org.example;

import java.time.LocalDate;

public class SingleTask extends Task {
    public SingleTask(String name, String description, TaskType type, int priority, LocalDate deadline) {
        super(name, description, type, priority, deadline);
    }

}