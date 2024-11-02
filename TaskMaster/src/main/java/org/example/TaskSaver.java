package org.example;

import javax.swing.*;
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
         try (FileWriter writer = new FileWriter(dataFile)) {
             for (Task task : tasks) {
                 writer.write(task.getName() + "," + task.getDescription() + "," + task.getType() + "," + task.getPriority() + "," + (task.getDeadline() == null ? "" : task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) + "\n");
             }
         } catch (IOException e) {
             JOptionPane.showMessageDialog(null, "Ошибка при сохранении данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
         }
     }

     public List<Task> loadTasks() {
         List<Task> loadedTasks = new ArrayList<>();
         try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
             String line;
             while ((line = reader.readLine()) != null) {
                 String[] parts = line.split(",");
                 if (parts.length == 5) {
                     String name = parts[0];
                     String description = parts[1];
                     TaskType type = TaskType.valueOf(parts[2]);
                     int priority = Integer.parseInt(parts[3]);
                     LocalDate deadline = parts[4].isEmpty() ? null : LocalDate.parse(parts[4], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                     Task task = new Task(name, description, type, priority, deadline);
                     loadedTasks.add(task);
                 }
             }
         } catch (FileNotFoundException e) {
             // Файл не найден - это нормально, если приложение запускается впервые
         } catch (IOException e) {
             JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных.");
         }
         return loadedTasks;
     }
 }
