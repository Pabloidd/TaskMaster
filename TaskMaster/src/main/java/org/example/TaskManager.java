
        package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.io.*;

public class TaskManager {

    private static final int MAIN_FRAME_WIDTH = 1000;
    private static final int MAIN_FRAME_HEIGHT = 800;
    private static final int TASK_PANEL_WIDTH = 250;
    private static final int TASK_PANEL_HEIGHT = 70;
    private static final int CREATE_TASK_DIALOG_WIDTH = 400;
    private static final int CREATE_TASK_DIALOG_HEIGHT = 300;
    private static final int EDIT_TASK_DIALOG_WIDTH = 400;
    private static final int EDIT_TASK_DIALOG_HEIGHT = 300;
    private static final int INFO_DIALOG_WIDTH = 400;
    private static final int INFO_DIALOG_HEIGHT = 300;

    private List<Task> tasks = new ArrayList<>();
    private JFrame mainFrame;
    private JPanel taskListPanel;
    private DefaultListModel<String> taskListModel; // Use DefaultListModel to hold data for the JList
    private String dataFile = "tasks.csv"; // файл для сохранения задач
    private JList<String> taskList; // Declare the JList

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
        mainFrame.setSize(MAIN_FRAME_WIDTH, MAIN_FRAME_HEIGHT);

        JPanel contentPane = new JPanel(new BorderLayout());

        // Панель инструментов
        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createTaskButton = new JButton(" Создать задачу ");
        createTaskButton.addActionListener(e -> createTaskDialog());
        toolBarPanel.add(createTaskButton);

        // Кнопка "Сохранить и выйти"

        JButton saveAndExitButton = new JButton(" Сохранить и выйти ");
        saveAndExitButton.addActionListener(e -> {
            saveTasksToFile();
            mainFrame.dispose();
        });
        toolBarPanel.add(saveAndExitButton);

        // Панель задач
        taskListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Используем FlowLayout
        taskListModel = new DefaultListModel<>();
        updateTaskList(); // Обновляем список задач при запуске
        // Initialize taskList
        taskList = new JList<>(taskListModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Enable single selection
        taskList.addListSelectionListener(e -> { // Обработка выбора задачи
            if (!e.getValueIsAdjusting()) { // Проверяем, что выбор не меняется
                expandTask();
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskList); // Add taskList to a JScrollPane
        taskListPanel.add(scrollPane, BorderLayout.CENTER); // Add the scrollpane to taskListPanel
        // Убираем фразу "Список задач"
        // taskListPanel.add(new JLabel("Список задач:")); // Добавляем заголовок

        // Панель действий
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editTaskButton = new JButton(" Редактировать ");
        editTaskButton.addActionListener(e -> editTaskDialog());
        actionPanel.add(editTaskButton);

        JButton completeTaskButton = new JButton(" Завершить ");
        completeTaskButton.addActionListener(e -> completeTask());
        actionPanel.add(completeTaskButton);

        JButton deleteTaskButton = new JButton(" Удалить ");
        deleteTaskButton.addActionListener(e -> deleteTask());
        actionPanel.add(deleteTaskButton);

        JButton sortByDeadlineButton = new JButton(" Сортировать по сроку ");
        sortByDeadlineButton.addActionListener(e -> sortByDeadline());
        actionPanel.add(sortByDeadlineButton);

        // Кнопка развернуть (убираем)
        //JButton expandTaskButton = new JButton(" Развернуть ");
        //expandTaskButton.addActionListener(e -> expandTask());
        //actionPanel.add(expandTaskButton);

        // Кнопка справки
        JButton helpButton = new JButton("?");
        helpButton.addActionListener(e -> showHelpDialog());
        actionPanel.add(helpButton);

        // Собираем все элементы
        contentPane.add(toolBarPanel, BorderLayout.NORTH);
        contentPane.add(taskListPanel, BorderLayout.CENTER); // Используем FlowLayout
        contentPane.add(actionPanel, BorderLayout.SOUTH);

        mainFrame.setContentPane(contentPane);
        mainFrame.setVisible(true);
    }

    private void expandTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для просмотра.");
            return;
        }

        Task selectedTask = tasks.get(selectedIndex);

        JFrame dialog = new JFrame("Информация о задаче");
        dialog.setSize(INFO_DIALOG_WIDTH, INFO_DIALOG_HEIGHT);
        dialog.setLayout(new GridLayout(6, 2));

        JLabel nameLabel = new JLabel("Название задачи:");
        JLabel nameValue = new JLabel(selectedTask.getName());
        JLabel descriptionLabel = new JLabel("Описание:");
        JLabel descriptionValue = new JLabel(selectedTask.getDescription());
        JLabel typeLabel = new JLabel("Тип задачи:");
        JLabel typeValue = new JLabel(selectedTask.getType().toString());
        JLabel priorityLabel = new JLabel("Приоритет:");
        JLabel priorityValue = new JLabel("Приоритет: " + selectedTask.getPriority());
        JLabel deadlineLabel = new JLabel("Срок выполнения:");
        JLabel deadlineValue = new JLabel(selectedTask.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        JLabel statusLabel = new JLabel("Статус:");
        JLabel statusValue = new JLabel(selectedTask.getStatus().toString());

        dialog.add(nameLabel);
        dialog.add(nameValue);
        dialog.add(descriptionLabel);
        dialog.add(descriptionValue);
        dialog.add(typeLabel);
        dialog.add(typeValue);
        dialog.add(priorityLabel);
        dialog.add(priorityValue);
        dialog.add(deadlineLabel);
        dialog.add(deadlineValue);
        dialog.add(statusLabel);
        dialog.add(statusValue);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    private void createTaskDialog() {
        JFrame dialog = new JFrame(" Создать задачу ");
        dialog.setSize(CREATE_TASK_DIALOG_WIDTH, CREATE_TASK_DIALOG_HEIGHT);
        dialog.setLayout(new GridLayout(8, 2));

        JLabel nameLabel = new JLabel("Название задачи:");
        JTextField nameField = new JTextField();
        JLabel descriptionLabel = new JLabel("Описание:");
        JTextArea descriptionField = new JTextArea();
        descriptionField.setLineWrap(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionField);

        JLabel typeLabel = new JLabel("Тип задачи:");
        JComboBox<TaskType> typeComboBox = new JComboBox<>(TaskType.values());

        JLabel priorityLabel = new JLabel("Приоритет:");
        JComboBox<Integer> priorityComboBox = new JComboBox<>(new Integer[]{1, 2, 3});

        JLabel deadlineLabel = new JLabel("Срок выполнения:");
        JTextField deadlineField = new JTextField();

        JButton createButton = new JButton("Создать");
        JButton cancelButton = new JButton("Отмена");

        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(descriptionLabel);
        dialog.add(descriptionScrollPane);
        dialog.add(typeLabel);
        dialog.add(typeComboBox);
        dialog.add(priorityLabel);
        dialog.add(priorityComboBox);
        dialog.add(deadlineLabel);
        dialog.add(deadlineField);
        dialog.add(createButton);
        dialog.add(cancelButton);

        createButton.addActionListener(e -> {
            String name = nameField.getText();
            String description = descriptionField.getText();
            TaskType type = (TaskType) typeComboBox.getSelectedItem();
            int priority = (Integer) priorityComboBox.getSelectedItem();
            LocalDate deadline = null;
            try {
                deadline = LocalDate.parse(deadlineField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Неверный формат даты. Используйте формат YYYY-MM-DD.");
                return;
            }

            if (name.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, заполните все поля.");
                return;
            }

            Task newTask = new Task(name, description, type, priority, deadline);
            tasks.add(newTask);
            updateTaskList();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    private void editTaskDialog() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для редактирования.");
            return;
        }

        Task selectedTask = tasks.get(selectedIndex);

        JFrame dialog = new JFrame(" Редактировать задачу ");
        dialog.setSize(EDIT_TASK_DIALOG_WIDTH, EDIT_TASK_DIALOG_HEIGHT);
        dialog.setLayout(new GridLayout(8, 2));

        JLabel nameLabel = new JLabel("Название задачи:");
        JTextField nameField = new JTextField(selectedTask.getName());
        JLabel descriptionLabel = new JLabel("Описание:");
        JTextArea descriptionField = new JTextArea(selectedTask.getDescription());
        descriptionField.setLineWrap(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionField);

        JLabel typeLabel = new JLabel("Тип задачи:");
        JComboBox<TaskType> typeComboBox = new JComboBox<>(TaskType.values());
        typeComboBox.setSelectedItem(selectedTask.getType());

        JLabel priorityLabel = new JLabel("Приоритет:");
        JComboBox<Integer> priorityComboBox = new JComboBox<>(new Integer[]{1, 2, 3});
        priorityComboBox.setSelectedItem(selectedTask.getPriority());

        JLabel deadlineLabel = new JLabel("Срок выполнения:");
        JTextField deadlineField = new JTextField(selectedTask.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");

        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(descriptionLabel);
        dialog.add(descriptionScrollPane);
        dialog.add(typeLabel);
        dialog.add(typeComboBox);
        dialog.add(priorityLabel);
        dialog.add(priorityComboBox);
        dialog.add(deadlineLabel);
        dialog.add(deadlineField);
        dialog.add(saveButton);
        dialog.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String description = descriptionField.getText();
            TaskType type = (TaskType) typeComboBox.getSelectedItem();
            int priority = (Integer) priorityComboBox.getSelectedItem();
            LocalDate deadline = null;
            try {
                deadline = LocalDate.parse(deadlineField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Неверный формат даты. Используйте формат YYYY-MM-DD.");
                return;
            }

            if (name.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, заполните все поля.");
                return;
            }

            selectedTask.setName(name);
            selectedTask.setDescription(description);
            selectedTask.setType(type);
            selectedTask.setPriority(priority);
            selectedTask.setDeadline(deadline);
            updateTaskList();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    private void completeTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для завершения.");
            return;
        }

        Task selectedTask = tasks.get(selectedIndex);
        if (selectedTask.getStatus() == TaskStatus.COMPLETED) {
            JOptionPane.showMessageDialog(mainFrame, "Задача уже завершена.");
            return;
        }

        selectedTask.setStatus(TaskStatus.COMPLETED);
        updateTaskList();
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите задачу для удаления.");
            return;
        }

        if (confirmDelete()) {
            tasks.remove(selectedIndex);
            updateTaskList();
        }
    }

    private void sortByDeadline() {
        tasks.sort(Comparator.comparing(Task::getDeadline));
        updateTaskList();
    }

    private void updateTaskList() {
        taskListPanel.removeAll(); // Очищаем панель задач
        // Убираем фразу "Список задач"
        // taskListPanel.add(new JLabel("Список задач:")); // Добавляем заголовок

        for (Task task : tasks) {
            JPanel taskPanel = new JPanel(new BorderLayout());
            taskPanel.setPreferredSize(new Dimension(TASK_PANEL_WIDTH, TASK_PANEL_HEIGHT)); // Уменьшаем размер прямоугольника

            // Цвет прямоугольника в зависимости от приоритета (более блед ный)
            Color priorityColor = switch (task.getPriority()) {
                case 1 -> new Color(255, 150, 150); // Более бледный красный
                case 2 -> new Color(255, 200, 150); // Более бледный оранжевый
                case 3 -> new Color(150, 255, 150); // Более бледный зеленый
                default -> Color.LIGHT_GRAY;
            };
            taskPanel.setBackground(priorityColor);

            // Рамка в зависимости от типа задачи
            if (task.getType() == TaskType.DAILY) {
                taskPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            } else if (task.getType() == TaskType.SINGLE) {
                taskPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
            }

            // Текст задачи (только название и срок)
            JLabel taskLabel = new JLabel("<html><center>" + task.getName() + "<br>" + task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "</center></html>");
            taskLabel.setHorizontalAlignment(SwingConstants.CENTER); // Центрирование текста
            taskPanel.add(taskLabel, BorderLayout.CENTER);

            // Добавляем обработчик двойного клика
            taskPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) { // Двойной клик
                        expandTask();
                    }
                }
            });

            // Добавляем прямоугольник на панель задач
            taskListPanel.add(taskPanel);
        }

        taskListPanel.revalidate(); // Обновляем компоновку панели
        taskListPanel.repaint(); // Перерисовываем панель
    }

    // Методы для сохранения и загрузки задач из файла
    private void saveTasksToFile() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            for (Task task : tasks) {
                writer.write(task.getName() + "," + task.getDescription() + "," + task.getType() + "," + task.getPriority() + "," + task.getDeadline() + "," + task.getStatus() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при сохранении данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String name = parts[0];
                    String description = parts[1];
                    TaskType type = TaskType.valueOf(parts[2]);
                    int priority = Integer.parseInt(parts[3]);
                    LocalDate deadline = LocalDate.parse(parts[4], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    TaskStatus status = TaskStatus.valueOf(parts[5]);
                    Task task = new Task(name, description, type, priority, deadline);
                    task.setStatus(status);
                    tasks.add(task);
                }
            }
        } catch (FileNotFoundException e) {
            // Файл не найден - это нормально, если приложение запускается впервые
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных.");
        }
    }

    private boolean confirmSave() {
        return JOptionPane.showConfirmDialog(mainFrame, "Сохранить изменения?", "Подтверждение", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private boolean confirmDelete() {
        return JOptionPane.showConfirmDialog(mainFrame, "Вы уверены, что хотите удалить задачу?", "Удаление", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void showHelpDialog() {
        String helpText = "Добро пожаловать в Task Manager!\n\n" +
                "Это приложение поможет вам организовать ваши задачи.\n\n" +
                "**Создать задачу:**\n" +
                "1. Нажмите кнопку \"Создать задачу\" в панели инструментов.\n" +
                "2. Заполните поля \"Название\", \"Описание\", \"Тип\", \"Приоритет\" и \"Срок выполнения\".\n" +
                "3. Нажмите кнопку \"Создать\".\n\n" +
                "**Редактировать задачу:**\n" +
                "1. Выберите задачу из списка.\n" +
                "2. Нажмите кнопку \"Редактировать\" в панели действий.\n" +
                "3. Измените необходимые поля.\n" +
                "4. Нажмите кнопку \"Сохранить\".\n\n" +
                "**Завершить задачу:**\n" +
                "1. Выберите задачу из списка.\n" +
                "2. Нажмите кнопку \"Завершить\" в панели действий.\n\n" +
                "**Удалить задачу:**\n" +
                "1. Выберите задачу из списка.\n" +
                "2. Нажмите кнопку \"Удалить\" в панели действий.\n\n" +
                "**Сортировать по сроку:**\n" +
                "1. Нажмите кнопку \"Сортировать по сроку\" в панели действий.\n\n" +
                "**Просмотр задачи:**\n" +
                "1. Выберите задачу из списка.\n" +
                "2. Дважды щелкните по задаче, чтобы открыть ее полную информацию.\n\n" +
                "**Справка:**\n" +
                "1. Нажмите кнопку \"?\" в панели действий, чтобы открыть это окно справки.\n\n" +
                "Приятного использования!";

        JOptionPane.showMessageDialog(mainFrame, helpText, "Справка", JOptionPane.INFORMATION_MESSAGE);
    }
}