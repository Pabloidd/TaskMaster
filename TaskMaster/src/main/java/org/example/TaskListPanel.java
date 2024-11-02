package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TaskListPanel {

    private JPanel taskListPanel;
    private JPanel taskListContainer;
    private TaskManager taskManager;

    public TaskListPanel(TaskManager taskManager) {
        this.taskManager = taskManager;
        taskListPanel = new JPanel(new BorderLayout()); // Используем BorderLayout
        taskListContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Используем FlowLayout для расположения в ряд с отступами
        taskListPanel.add(taskListContainer, BorderLayout.CENTER); // Добавляем контейнер на панель
    }

    public JPanel getPanel() {
        return taskListPanel;
    }

    public JPanel getTaskListContainer() {
        return taskListContainer;
    }

    public void updateTaskList(TaskType filterType, int filterPriority) {
        taskListContainer.removeAll(); // Очищаем контейнер

        // Фильтруем задачи по типу и приоритету
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : taskManager.tasks) {
            if ((filterType == null || task.getType() == filterType) &&
                    (filterPriority == 0 || task.getPriority() == filterPriority)) {
                filteredTasks.add(task);
            }
        }

        for (int i = 0; i < filteredTasks.size(); i++) {
            JPanel taskPanel = taskManager.createTaskPanel(filteredTasks.get(i)); // Создаем блок задачи
            taskListContainer.add(taskPanel); // Добавляем блок в контейнер
        }

        taskListContainer.revalidate(); // Обновляем компоновку контейнера
        taskListContainer.repaint(); // Перерисовываем контейнер
    }
}
