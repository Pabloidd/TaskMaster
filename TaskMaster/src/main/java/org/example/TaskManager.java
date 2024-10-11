
        package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.io.*;

public class TaskManager {

    private List<Task> tasks = new ArrayList<>();
    private JFrame mainFrame;
    private JPanel taskListPanel;
    private JList<String> taskList;
    private DefaultListModel<String> taskListModel;
    private String dataFile = "tasks.txt"; // файл для сохранения задач

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaskManager().createAndShowGUI());
    }

    private void createAndShowGUI() {
        loadTasksFromFile(); // Загружаем задачи из файла при запуске

        mainFrame = new JFrame("Task Manager");
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Предупреждение о несохраненных данных
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (confirmSave()) {
                    saveTasksToFile();
                    mainFrame.dispose();
                }
            }
        });
        mainFrame.setSize(600, 400);

        JPanel contentPane = new JPanel(new BorderLayout());

        // Панель инструментов
        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createTaskButton = new JButton("Создать задачу");
        createTaskButton.addActionListener(e -> createTaskDialog());
        toolBarPanel.add(createTaskButton);

        // Кнопка "Сохранить и выйти"
        JButton saveAndExitButton = new JButton("Сохранить и выйти");
        saveAndExitButton.addActionListener(e -> {
            saveTasksToFile();
            mainFrame.dispose();
        });
        toolBarPanel.add(saveAndExitButton);

        // Панель задач
        taskListPanel = new JPanel(new BorderLayout());
        taskListModel = new DefaultListModel<>();
        updateTaskList(); // Обновляем список задач при запуске
        taskList = new JList<>(taskListModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(taskList);
        taskListPanel.add(scrollPane, BorderLayout.CENTER);


        // Панель действий
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editTaskButton = new JButton("Редактировать");
        editTaskButton.addActionListener(e -> editTaskDialog());
        actionPanel.add(editTaskButton);

        JButton completeTaskButton = new JButton("Завершить");
        completeTaskButton.addActionListener(e -> completeTask());
        actionPanel.add(completeTaskButton);

        JButton deleteTaskButton = new JButton("Удалить");
        deleteTaskButton.addActionListener(e -> deleteTask());
        actionPanel.add(deleteTaskButton);

        // Кнопки сортировки и фильтрации
        JButton sortByDeadlineButton = new JButton("Сортировать по сроку");
        sortByDeadlineButton.addActionListener(e -> sortByDeadline());
        actionPanel.add(sortByDeadlineButton);

        JButton filterByDeadlineButton = new JButton("Фильтр по сроку");
        filterByDeadlineButton.addActionListener(e -> filterByDeadline());
        actionPanel.add(filterByDeadlineButton);

        // Собираем все элементы
        contentPane.add(toolBarPanel, BorderLayout.NORTH);
        contentPane.add(taskListPanel, BorderLayout.CENTER);
        contentPane.add(actionPanel, BorderLayout.SOUTH);

        mainFrame.setContentPane(contentPane);
        mainFrame.setVisible(true);
    }

    private void createTaskDialog() {
        JFrame dialog = new JFrame("Создать задачу");
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(8, 2));

        // Поля ввода
        JLabel nameLabel = new JLabel("Название:");
        JTextField nameField = new JTextField();
        JLabel descriptionLabel = new JLabel("Описание:");
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JLabel typeLabel = new JLabel("Тип:");
        JComboBox<TaskType> typeComboBox = new JComboBox<>(TaskType.values());
        JLabel priorityLabel = new JLabel("Приоритет:");
        JComboBox<Integer> priorityComboBox = new JComboBox<>(new Integer[]{1, 2, 3}); // 1 - высокий, 2 - средний, 3 - низкий
        JLabel deadlineLabel = new JLabel("Срок выполнения (гггг-мм-дд):");
        JTextField deadlineField = new JTextField();
        JLabel tagsLabel = new JLabel("Теги (через запятую):");
        JTextField tagsField = new JTextField();

        // Кнопки
        JButton createButton = new JButton("Создать");
        createButton.addActionListener(e -> {
            String name = nameField.getText();
            String description = descriptionArea.getText();
            TaskType type = (TaskType) typeComboBox.getSelectedItem();
            int priority = (Integer) priorityComboBox.getSelectedItem();
            LocalDate deadline = null;
            try {
                deadline = LocalDate.parse(deadlineField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Неверный формат даты. Используйте гггг-мм-дд.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введите название задачи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] tags = tagsField.getText().trim().split(",");
            for (int i = 0; i < tags.length; i++) {
                tags[i] = tags[i].trim();
            }

            tasks.add(new Task(name, description, type, priority, deadline));
            tasks.get(tasks.size() - 1).setTags(tags); // Установка тегов
            updateTaskList();
            dialog.dispose();
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        // Добавляем элементы на диалоговое окно
        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(descriptionLabel);
        dialog.add(descriptionArea);
        dialog.add(typeLabel);
        dialog.add(typeComboBox);
        dialog.add(priorityLabel);
        dialog.add(priorityComboBox);
        dialog.add(deadlineLabel);
        dialog.add(deadlineField);
        dialog.add(tagsLabel);
        dialog.add(tagsField);
        dialog.add(createButton);
        dialog.add(cancelButton);

        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    private void editTaskDialog() {
        if (taskList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для редактирования.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedIndex = taskList.getSelectedIndex();
        Task selectedTask = tasks.get(selectedIndex);

        JFrame dialog = new JFrame("Редактировать задачу");
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(8, 2));

        // Поля ввода
        JLabel nameLabel = new JLabel("Название:");
        JTextField nameField = new JTextField(selectedTask.getName());
        JLabel descriptionLabel = new JLabel("Описание:");
        JTextArea descriptionArea = new JTextArea(selectedTask.getDescription());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JLabel typeLabel = new JLabel("Тип:");
        JComboBox<TaskType> typeComboBox = new JComboBox<>(TaskType.values());
        typeComboBox.setSelectedItem(selectedTask.getType());
        JLabel priorityLabel = new JLabel("Приоритет:");
        JComboBox<Integer> priorityComboBox = new JComboBox<>(new Integer[]{1, 2, 3}); // 1 - высокий, 2 - средний, 3 - низкий
        priorityComboBox.setSelectedItem(selectedTask.getPriority());
        JLabel deadlineLabel = new JLabel("Срок выполнения (гггг-мм-дд):");
        JTextField deadlineField = new JTextField(selectedTask.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        JLabel tagsLabel = new JLabel("Теги (через запятую):");
        JTextField tagsField = new JTextField(String.join(", ", selectedTask.getTags()));

        // Кнопки
        JButton updateButton = new JButton("Обновить");
        updateButton.addActionListener(e -> {
            String name = nameField.getText();
            String description = descriptionArea.getText();
            TaskType type = (TaskType) typeComboBox.getSelectedItem();
            int priority = (Integer) priorityComboBox.getSelectedItem();
            LocalDate deadline = null;
            try {
                deadline = LocalDate.parse(deadlineField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Неверный формат даты. Используйте гггг-мм-дд.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введите название задачи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] tags = tagsField.getText().trim().split(",");
            for (int i = 0; i < tags.length; i++) {
                tags[i] = tags[i].trim();
            }

            tasks.set(selectedIndex, new Task(name, description, type, priority, deadline));
            tasks.get(selectedIndex).setTags(tags);
            updateTaskList();
            dialog.dispose();
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        // Добавляем элементы на диалоговое окно
        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(descriptionLabel);
        dialog.add(descriptionArea);
        dialog.add(typeLabel);
        dialog.add(typeComboBox);
        dialog.add(priorityLabel);
        dialog.add(priorityComboBox);
        dialog.add(deadlineLabel);
        dialog.add(deadlineField);
        dialog.add(tagsLabel);
        dialog.add(tagsField);
        dialog.add(updateButton);
        dialog.add(cancelButton);

        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    private void completeTask() {
        if (taskList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для завершения.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedIndex = taskList.getSelectedIndex();
        tasks.get(selectedIndex).setStatus(TaskStatus.COMPLETED);
        updateTaskList();
    }

    private void deleteTask() {
        if (taskList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для удаления.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (confirmDelete()) {
            int selectedIndex = taskList.getSelectedIndex();
            tasks.remove(selectedIndex);
            updateTaskList();
        }
    }

    private boolean confirmDelete() {
        return JOptionPane.showConfirmDialog(mainFrame, "Вы уверены, что хотите удалить задачу?", "Подтверждение", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void updateTaskList() {
        taskListModel.clear();
        for (Task task : tasks) {
            taskListModel.addElement(task.toString());
        }
    }

    private void sortByDeadline() {
        tasks.sort(Comparator.comparing(Task::getDeadline));
        updateTaskList();
    }

    private void filterByDeadline() {
        // Реализация фильтрации по сроку
    }

    // Методы для сохранения и загрузки задач из файла
    private void saveTasksToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при сохранении данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasksFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            tasks = (List<Task>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Если файла нет, то ничего не делаем
        }
    }

    private boolean confirmSave() {
        return JOptionPane.showConfirmDialog(mainFrame, "Сохранить изменения?", "Подтверждение", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
