 package org.example;

import javax.swing.*;
import javax.swing.border.Border;
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
    private static final int TASK_PANEL_WIDTH = 200; // Фиксированная ширина блока
    private static final int TASK_PANEL_HEIGHT = 50; // Фиксированная высота блока
    private static final int TASKS_PER_ROW = 3; // Количество блоков в ряду
    private static final int CREATE_TASK_DIALOG_WIDTH = 400;
    private static final int CREATE_TASK_DIALOG_HEIGHT = 300;
    private static final int EDIT_TASK_DIALOG_WIDTH = 400;
    private static final int EDIT_TASK_DIALOG_HEIGHT = 300;
    private static final int INFO_DIALOG_WIDTH = 400;
    private static final int INFO_DIALOG_HEIGHT = 300;

    private List<Task> tasks = new ArrayList<>();
    private JFrame mainFrame;
    private JPanel taskListPanel;
    private JPanel taskListContainer; // Контейнер для хранения блоков задач
    private String dataFile = "tasks.csv"; // файл для сохранения задач
    private JPanel selectedTaskPanel = null; // Панель выбранной задачи
    private TaskType filterType = null; // Фильтр по типу задачи
    private int filterPriority = 0; // Фильтр по приоритету
    private TaskSaver taskSaver = new TaskSaver(dataFile);

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

        // Кнопка справки
        JButton helpButton = new JButton("?");
        helpButton.addActionListener(e -> showHelpDialog());
        toolBarPanel.add(helpButton); // Добавляем кнопку "Справка" в панель инструментов

        // Фильтр по типу
        JLabel typeLabel = new JLabel("Фильтр по типу:");
        toolBarPanel.add(typeLabel);
        JComboBox<TaskType> typeComboBox = new JComboBox<>(TaskType.values());
        typeComboBox.addItem(null); // Добавляем опцию "Все"
        typeComboBox.setSelectedItem(null); // Выбираем "Все" по умолчанию
        typeComboBox.addActionListener(e -> {
            filterType = (TaskType) typeComboBox.getSelectedItem();
            updateTaskList();
        });
        toolBarPanel.add(typeComboBox);

        // Фильтр по приоритету
        JLabel priorityLabel = new JLabel("Фильтр по приоритету:");
        toolBarPanel.add(priorityLabel);
        JComboBox<Integer> priorityComboBox = new JComboBox<>(new Integer[]{0, 1, 2, 3}); // 0 - Все
        priorityComboBox.addItem(0); // Добавляем опцию "Все"
        priorityComboBox.setSelectedItem(0); // Выбираем "Все" по умолчанию
        priorityComboBox.addActionListener(e -> {
            filterPriority = (Integer) priorityComboBox.getSelectedItem();
            updateTaskList();
        });
        toolBarPanel.add(priorityComboBox);

        // Панель задач
        taskListPanel = new JPanel(new BorderLayout()); // Используем BorderLayout
        taskListContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Используем FlowLayout для расположения в ряд с отступами
        taskListPanel.add(taskListContainer, BorderLayout.CENTER); // Добавляем контейнер на панель
        updateTaskList(); // Обновляем список задач при запуске

        // Панель действий
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton expandTaskButton = new JButton("Развернуть"); // Кнопка Развернуть
        expandTaskButton.addActionListener(e -> {
            if (selectedTaskPanel != null) {
                expandTask(selectedTaskPanel);
            }
        });
        actionPanel.add(expandTaskButton);

        JButton editTaskButton = new JButton("Редактировать");
        editTaskButton.addActionListener(e -> {
            if (selectedTaskPanel != null) {
                editTask(selectedTaskPanel);
            }
        });
        actionPanel.add(editTaskButton);
        JButton deleteTaskButton = new JButton("Удалить");
        deleteTaskButton.addActionListener(e -> {
            if (selectedTaskPanel != null) {
                deleteTask(selectedTaskPanel);
            }
        });
        actionPanel.add(deleteTaskButton);

        JButton sortByDeadlineButton = new JButton("Сортировать по сроку");
        sortByDeadlineButton.addActionListener(e -> sortByDeadline());
        actionPanel.add(sortByDeadlineButton);

        JButton sortByImportanceButton = new JButton("Сортировать по важности");
        sortByImportanceButton.addActionListener(e -> sortByImportance());
        actionPanel.add(sortByImportanceButton);

        // Собираем все элементы
        contentPane.add(toolBarPanel, BorderLayout.NORTH);
        contentPane.add(taskListPanel, BorderLayout.CENTER);
        contentPane.add(actionPanel, BorderLayout.SOUTH);

        mainFrame.setContentPane(contentPane);
        mainFrame.setVisible(true);
    }

    private void updateTaskList() {
        taskListContainer.removeAll(); // Очищаем контейнер

        // Фильтруем задачи по типу и приоритету
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks) {
            if ((filterType == null || task.getType() == filterType) &&
                    (filterPriority == 0 || task.getPriority() == filterPriority)) {
                filteredTasks.add(task);
            }
        }

        for (int i = 0; i < filteredTasks.size(); i++) {
            JPanel taskPanel = createTaskPanel(filteredTasks.get(i)); // Создаем блок задачи
            taskListContainer.add(taskPanel); // Добавляем блок в контейнер
        }

        taskListContainer.revalidate(); // Обновляем компоновку контейнера
        taskListContainer.repaint(); // Перерисовываем контейнер
    }

    private JPanel createTaskPanel(Task task) {
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setPreferredSize(new Dimension(TASK_PANEL_WIDTH, TASK_PANEL_HEIGHT));

        // Create a JPanel for the gradient to be applied to
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Устанавливаем градиент в зависимости от статуса задачи
                if (task.isOverdue()) {
                    g2d.setPaint(new GradientPaint(0, 0, Color.LIGHT_GRAY, getWidth(), 0, Color.LIGHT_GRAY));
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, getStartColorForPriority(task.getPriority()), getWidth(), 0, getEndColorForPriority(task.getPriority())));
                }
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setPreferredSize(new Dimension(TASK_PANEL_WIDTH, TASK_PANEL_HEIGHT));
        // Set the background to transparent
        gradientPanel.setOpaque(false);

        // Текст задачи (только название и срок)
        String deadlineText = task.getType() == TaskType.DAILY ? "Ежедневная" : task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JLabel taskLabel = new JLabel("<html><center>" + task.getName() + "<br>" + deadlineText + "</center></html>");
        taskLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gradientPanel.add(taskLabel);

        // Обработка клика по блоку
        gradientPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Одиночный клик
                    // Сброс выделения предыдущей задачи
                    if (selectedTaskPanel != null) {
                        selectedTaskPanel.setBorder(null);
                        // Возвращаем цвет фона предыдущей задачи
                        JPanel previousGradientPanel = (JPanel) selectedTaskPanel.getComponent(0);
                        previousGradientPanel.repaint();
                    }
                    selectedTaskPanel = taskPanel; // Запоминаем выбранную панель
                    taskPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2)); // Выделение синей рамкой
                } else if (e.getClickCount() == 2) { // Двойной щелчок
                    expandTask(taskPanel); // Вызов функции развернуть
                }
            }
        });

        // Добавляем gradientPanel на taskPanel
        taskPanel.add(gradientPanel, BorderLayout.CENTER);

        return taskPanel;
    }

    private Color getStartColorForPriority(int priority) {
        return switch (priority) {
            case 1 -> new Color(255, 150, 150); // Красный (начальный)
            case 2 -> new Color(255, 200, 150); // Оранжевый (начальный)
            case 3 -> new Color(150, 255, 150); // Зеленый (начальный)
            default -> Color.LIGHT_GRAY;
        };
    }

    private Color getEndColorForPriority(int priority) {
        return switch (priority) {
            case 1 -> new Color(200, 50, 50); // Красный (конечный)
            case 2 -> new Color(200, 150, 50); // Оранжевый (конечный)
            case 3 -> new Color(50, 200, 50); // Зеленый (конечный)
            default -> Color.LIGHT_GRAY;
        };
    }

    private void expandTask(JPanel taskPanel) {
        // Получаем задачу из панели
        Task task = getTaskFromPanel(taskPanel);
        if (task != null) {
            JFrame dialog = new JFrame("Информация о задаче");
            dialog.setSize(INFO_DIALOG_WIDTH, INFO_DIALOG_HEIGHT);
            dialog.setLayout(new GridLayout(6, 2));

            JLabel nameLabel = new JLabel("Название задачи:");
            JLabel nameValue = new JLabel(task.getName());
            JLabel descriptionLabel = new JLabel("Описание:");
            JLabel descriptionValue = new JLabel(task.getDescription());
            JLabel typeLabel = new JLabel("Тип задачи:");
            JLabel typeValue = new JLabel(task.getType().toString());
            JLabel priorityLabel = new JLabel("Приоритет:");
            JLabel priorityValue = new JLabel(getPriorityName(task.getPriority()));
            // Условие для вывода даты только для не ежедневных задач
            if (task.getType() != TaskType.DAILY) {
                JLabel deadlineLabel = new JLabel("Срок выполнения:");
                JLabel deadlineValue = new JLabel(task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                dialog.add(deadlineLabel);
                dialog.add(deadlineValue);
            }

            dialog.add(nameLabel);
            dialog.add(nameValue);
            dialog.add(descriptionLabel);
            dialog.add(descriptionValue);
            dialog.add(typeLabel);
            dialog.add(typeValue);
            dialog.add(priorityLabel);
            dialog.add(priorityValue);
            dialog.setLocationRelativeTo(mainFrame);
            dialog.setVisible(true);
        }
    }

    private void editTask(JPanel taskPanel) {
        // Получаем задачу из панели
        Task task = getTaskFromPanel(taskPanel);
        if (task != null) {
            editTask(task); // Используем существующий метод editTask()
        }
    }

    private void deleteTask(JPanel taskPanel) {
        // Получаем задачу из панели
        Task task = getTaskFromPanel(taskPanel);
        if (task != null) {
            int index = tasks.indexOf(task); // Находим индекс задачи
            if (index != -1) {
                if (confirmDelete()) {
                    tasks.remove(index); // Удаляем задачу
                    updateTaskList();
                }
            }
        }
    }

    private Task getTaskFromPanel(JPanel taskPanel) {
        // Находим задачу, соответствующую панели
        for (int i = 0; i < tasks.size(); i++) {
            if (taskListContainer.getComponent(i) == taskPanel) {
                return tasks.get(i);
            }
        }
        return null; // Если задача не найдена
    }

    private void createTaskDialog() {
        JFrame dialog = new JFrame("Создать задачу");
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
        JComboBox<String> priorityComboBox = new JComboBox<>(new String[]{"ОСОБО ВАЖНЫЙ", "ВАЖНЫЙ", "ОБЫЧНЫЙ"});

        JLabel deadlineLabel = new JLabel("Срок выполнения:");
        JTextField deadlineField = new JTextField();
        // Скрываем поле для ежедневных задач
        deadlineField.setEnabled(false);

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

        typeComboBox.addActionListener(e -> {
            TaskType selectedType = (TaskType) typeComboBox.getSelectedItem();
            if (selectedType == TaskType.DAILY) {
                deadlineField.setEnabled(false);
                deadlineField.setText(""); // Очищаем поле
            } else {
                deadlineField.setEnabled(true);
            }
        });

        createButton.addActionListener(e -> {
            String name = nameField.getText();
            String description = descriptionField.getText();
            TaskType type = (TaskType) typeComboBox.getSelectedItem();
            int priority = getPriorityFromName(priorityComboBox.getSelectedItem().toString());
            LocalDate deadline = null;
            if (type != TaskType.DAILY) {
                try {
                    deadline = LocalDate.parse(deadlineField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "Неверный формат даты. Используйте формат YYYY-MM-DD.");
                    return;
                }

                if (deadline.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dialog, "Дата дедлайна не может быть ранее сегодняшней.");
                    return;
                }
            }

            // Валидация названия задачи
            if (name.length() > 20) {
                JOptionPane.showMessageDialog(dialog, "Название задачи не может быть длиннее 20 символов.");
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

    private void editTask(Task task) {
        JFrame dialog = new JFrame("Редактировать задачу");
        dialog.setSize(EDIT_TASK_DIALOG_WIDTH, EDIT_TASK_DIALOG_HEIGHT);
        dialog.setLayout(new GridLayout(8, 2));
        JLabel nameLabel = new JLabel("Название задачи:");
        JTextField nameField = new JTextField(task.getName());
        JLabel descriptionLabel = new JLabel("Описание:");
        JTextArea descriptionField = new JTextArea(task.getDescription());
        descriptionField.setLineWrap(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionField);

        JLabel typeLabel = new JLabel("Тип задачи:");
        JComboBox<TaskType> typeComboBox = new JComboBox<>(TaskType.values());
        typeComboBox.setSelectedItem(task.getType());

        JLabel priorityLabel = new JLabel("Приоритет:");
        JComboBox<String> priorityComboBox = new JComboBox<>(new String[]{"ОСОБО ВАЖНЫЙ", "ВАЖНЫЙ", "ОБЫЧНЫЙ"});
        priorityComboBox.setSelectedItem(getPriorityName(task.getPriority()));
        JLabel deadlineLabel = new JLabel("Срок выполнения:");
        JTextField deadlineField = new JTextField(task.getDeadline() == null ? "" : task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

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

        typeComboBox.addActionListener(e -> {
            TaskType selectedType = (TaskType) typeComboBox.getSelectedItem();
            if (selectedType == TaskType.DAILY) {
                deadlineField.setEnabled(false);
                deadlineField.setText(""); // Очищаем поле
            } else {
                deadlineField.setEnabled(true);
            }
        });

        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String description = descriptionField.getText();
            TaskType type = (TaskType) typeComboBox.getSelectedItem();
            int priority = getPriorityFromName(priorityComboBox.getSelectedItem().toString());
            LocalDate deadline = null;
            if (type != TaskType.DAILY) {
                try {
                    deadline = LocalDate.parse(deadlineField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "Неверный формат даты. Используйте формат YYYY-MM-DD.");
                    return;
                }

                if (deadline.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dialog, "Дата дедлайна не может быть ранее сегодняшней.");
                    return;
                }
            }

            // Валидация названия задачи
            if (name.length() > 20) {
                JOptionPane.showMessageDialog(dialog, "Название задачи не может быть длиннее 20 символов.");
                return;
            }

            if (name.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, заполните все поля.");
                return;
            }

            // Обновляем задачу
            task.setName(name);
            task.setDescription(description);
            task.setType(type);
            task.setPriority(priority);
            task.setDeadline(deadline);

            updateTaskList();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    private void sortByDeadline() {
        // Сортировка по сроку выполнения (не ежедневные задачи)
        tasks.sort(Comparator.comparing(Task::getDeadline).thenComparing(Task::getName));
        updateTaskList();
    }

    private void sortByImportance() {
        // Сортировка по важности (по убыванию)
        tasks.sort(Comparator.comparingInt(Task::getPriority).reversed().thenComparing(Task::getName));
        updateTaskList();
    }

    // Методы для сохранения и загрузки задач из файла
    private void saveTasksToFile() {
        taskSaver.saveTasks(tasks);
    }

    private void loadTasksFromFile() {
        tasks = taskSaver.loadTasks();
    }

    private boolean confirmSave() {
        return JOptionPane.showConfirmDialog(mainFrame, "Выйти и сохранить изменения?", "Подтверждение", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private boolean confirmDelete() {
        return JOptionPane.showConfirmDialog(mainFrame, "Вы уверены, что хотите удалить задачу? Это действие необратимо.", "Удаление", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
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
                " **Сортировать по сроку:**\n" +
                "1. Нажмите кнопку \"Сортировать по сроку\" в панели действий.\n\n" +
                "**Сортировать по важности:**\n" +
                "1. Нажмите кнопку \"Сортировать по важности\" в панели действий.\n\n" +
                "**Просмотр задачи:**\n" +
                "1. Выберите задачу из списка.\n" +
                "2. Дважды щелкните по задаче, чтобы открыть ее полную информацию.\n" +
                "3. Нажмите кнопку \"Развернуть\" в панели действий, чтобы открыть информацию о выбранной задаче.\n\n" +
                "**Справка:**\n" +
                "1. Нажмите кнопку \"?\" в панели действий, чтобы открыть это окно справки.\n\n" +
                "Приятного использования!";

        JOptionPane.showMessageDialog(mainFrame, helpText, "Справка", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getPriorityName(int priority) {
        return switch (priority) {
            case 1 -> "ОСОБО ВАЖНЫЙ";
            case 2 -> "ВАЖНЫЙ";
            case 3 -> "ОБЫЧНЫЙ";
            default -> "";
        };
    }

    private int getPriorityFromName(String priorityName) {
        return switch (priorityName) {
            case "ОСОБО ВАЖНЫЙ" -> 1;
            case "ВАЖНЫЙ" -> 2;
            case "ОБЫЧНЫЙ" -> 3;
            default -> 0;
        };
    }

    // Добавление фильтра "Без фильтра" для типа и приоритета
    private String getFilterName(TaskType type, int priority) {
        if (type == null && priority == 0) {
            return "Без фильтра";
        } else {
            return type == null ? "Все типы" : type.toString();
        }
    }
}

class TaskSaver {
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