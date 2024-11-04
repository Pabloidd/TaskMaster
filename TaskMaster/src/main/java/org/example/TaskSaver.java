package org.example;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskSaver {
    private String dataFile;

    public TaskSaver(String dataFile) {
        this.dataFile = dataFile;
    }

    public void saveTasks(List<Task> tasks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile))) {
            for (Task task : tasks) {
                String taskString = task.getName() + "," + task.getDescription() + "," + task.getType() + "," + task.getPriority();
                if (task.getDeadline() != null) {
                    taskString += "," + task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                writer.println(taskString);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Обработка ошибки: например, вывести сообщение пользователю
        }
    }

    public List<Task> loadTasks() {
        List<Task> loadedTasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0];
                String description = parts[1];
                TaskType type = TaskType.valueOf(parts[2]);
                int priority = Integer.parseInt(parts[3]);
                LocalDate deadline = null;
                if (parts.length > 4) {
                    deadline = LocalDate.parse(parts[4], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                if (type == TaskType.DAILY) {
                    loadedTasks.add(new DailyTask(name, description, type, priority));
                } else if (type == TaskType.WEEKLY) {
                    loadedTasks.add(new WeeklyTask(name, description, type, priority));
                } else if (type == TaskType.MONTHLY) {
                    loadedTasks.add(new MonthlyTask(name, description, type, priority));
                } else { // type == TaskType.SINGLE
                    loadedTasks.add(new SingleTask(name, description, type, priority, deadline));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Обработка ошибки: например, вывести сообщение пользователю
        }
        return loadedTasks;
    }
}
