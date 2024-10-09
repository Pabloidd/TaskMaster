package org.example;
// добавить кнопку для удаления задачи. с доп подтверждением.
// добавить кнопку *сохранить и выйти*. Данные будут сохраняться в файлик а пролога закрываться. При нажатии на крестик и т.д. должны предупреждать что данные не сохраняться
// добавить кнопку развернуть ( имеется ввиду задачу )
// при неправильном вводе даты прога ничего не делает. Нужно всплывающее окно с сообщением об ошибке
// добавить кнопки сортировки и фильтров
//сделать интерфейс краше ( + шанс выбора одного из вариантов )
// сделать ограничение по количеству вводимых символов
// по умолчанию пусть выводятся те, у кого горят дедлайны
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
//test
public class TaskManager {

    private List<Task> tasks = new ArrayList<>();
    private JFrame mainFrame;
    private JPanel taskListPanel;
    private JList<String> taskList;
    private DefaultListModel<String> taskListModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaskManager().createAndShowGUI());
    }

    private void createAndShowGUI() {
        mainFrame = new JFrame("Task Manager");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(600, 400);

        JPanel contentPane = new JPanel(new BorderLayout());

        // Панель инструментов
        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createTaskButton = new JButton("Создать задачу");
        createTaskButton.addActionListener(e -> createTaskDialog());
        toolBarPanel.add(createTaskButton);

        // Панель задач
        taskListPanel = new JPanel(new BorderLayout());
        taskListModel = new DefaultListModel<>();
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
        dialog.setLayout(new GridLayout(6, 2));

        // Поля ввода
        JLabel nameLabel = new JLabel("Название:");
        JTextField nameField = new JTextField();
        JLabel descriptionLabel = new JLabel("Описание:");
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JLabel typeLabel = new JLabel("Тип:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Разовая", "Ежедневная"});
        JLabel priorityLabel = new JLabel("Приоритет:");
        JComboBox<String> priorityComboBox = new JComboBox<>(new String[]{"Высокий", "Средний", "Низкий"});
        JLabel deadlineLabel = new JLabel("Срок выполнения (гггг-мм-дд):");
        JTextField deadlineField = new JTextField();

        // Кнопки
        JButton createButton = new JButton("Создать");
        createButton.addActionListener(e -> {
            String name = nameField.getText();
            String description = descriptionArea.getText();
            TaskType type = typeComboBox.getSelectedItem().equals("Разовая") ? TaskType.SINGLE : TaskType.DAILY;
            int priority = priorityComboBox.getSelectedIndex() + 1; // 1 - высокий, 2 - средний, 3 - низкий
            LocalDate deadline = LocalDate.parse(deadlineField.getText());
            Task task = new Task(name, description, type, priority, deadline);
            tasks.add(task);
            updateTaskList();
            dialog.dispose();
        });
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        // Добавляем элементы в диалоговое окно
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
        dialog.add(createButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void editTaskDialog() {
        if (taskList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для редактирования.");
            return;
        }
        int selectedIndex = taskList.getSelectedIndex();
        Task task = tasks.get(selectedIndex);

        JFrame dialog = new JFrame("Редактировать задачу");
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(6, 2));

        // Поля ввода
        JLabel nameLabel = new JLabel("Название:");
        JTextField nameField = new JTextField(task.getName());
        JLabel descriptionLabel = new JLabel("Описание:");
        JTextArea descriptionArea = new JTextArea(task.getDescription());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JLabel typeLabel = new JLabel("Тип:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Разовая", "Ежедневная"});
        typeComboBox.setSelectedItem(task.getType() == TaskType.SINGLE ? "Разовая" : "Ежедневная");
        JLabel priorityLabel = new JLabel("Приоритет:");
        JComboBox<String> priorityComboBox = new JComboBox<>(new String[]{"Высокий", "Средний", "Низкий"});
        priorityComboBox.setSelectedIndex(task.getPriority() - 1); // 1 - высокий, 2 - средний, 3 - низкий
        JLabel deadlineLabel = new JLabel("Срок выполнения (гггг-мм-дд):");
        JTextField deadlineField = new JTextField(task.getDeadline().toString());

        // Кнопки
        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> {
            task.setName(nameField.getText());
            task.setDescription(descriptionArea.getText());
            task.setType(typeComboBox.getSelectedItem().equals("Разовая") ? TaskType.SINGLE : TaskType.DAILY);
            task.setPriority(priorityComboBox.getSelectedIndex() + 1); // 1 - высокий, 2 - средний, 3 - низкий
            task.setDeadline(LocalDate.parse(deadlineField.getText()));
            updateTaskList();
            dialog.dispose();
        });
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        // Добавляем элементы в диалоговое окно
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
        dialog.add(saveButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void completeTask() {
        if (taskList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для завершения.");
            return;
        }

        int selectedIndex = taskList.getSelectedIndex();
        tasks.get(selectedIndex).setStatus(TaskStatus.COMPLETED);
        updateTaskList();
    }

    private void updateTaskList() {
        taskListModel.clear();
//        for (int i = 0; i < tasks.size(); i++) {
//            Task task = tasks.get(i);
//            taskListModel.addElement((i + 1) + ". " + task.getName() + " (" + task.getStatus() + ")");
//        }
        for (Task task : tasks){
            taskListModel.addElement((task.getName() + " (" + task.getStatus() + ")"));
        }
    }
}