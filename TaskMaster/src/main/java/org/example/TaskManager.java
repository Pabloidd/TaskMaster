
        package org.example;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
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

        // Кнопка справки
        JButton helpButton = new JButton("?");
        helpButton.addActionListener(e -> showHelpDialog());
        toolBarPanel.add(helpButton); // Добавляем кнопку "Справка" в панель инструментов

        // Панель задач
        taskListPanel = new JPanel(new BorderLayout()); // Используем BorderLayout
        taskListContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Используем FlowLayout для расположения в ряд с отступами
        taskListPanel.add(taskListContainer, BorderLayout.CENTER); // Добавляем контейнер на панель
        updateTaskList(); // Обновляем список задач при запуске

        // Панель действий
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton expandTaskButton = new JButton(" Развернуть "); // Кнопка Развернуть
        expandTaskButton.addActionListener(e -> {
            if (selectedTaskPanel != null) {
                expandTask(selectedTaskPanel);
            }
        });
        actionPanel.add(expandTaskButton);

        JButton editTaskButton = new JButton(" Редактировать ");
        editTaskButton.addActionListener(e -> {
            if (selectedTaskPanel != null) {
                editTask(selectedTaskPanel);
            }
        });
        actionPanel.add(editTaskButton);
        JButton deleteTaskButton = new JButton(" Удалить ");
        deleteTaskButton.addActionListener(e -> {
            if (selectedTaskPanel != null) {
                deleteTask(selectedTaskPanel);
            }
        });
        actionPanel.add(deleteTaskButton);

        JButton sortByDeadlineButton = new JButton(" Сортировать по сроку ");
        sortByDeadlineButton.addActionListener(e -> sortByDeadline());
        actionPanel.add(sortByDeadlineButton);

        JButton sortByImportanceButton = new JButton(" Сортировать по важности ");
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

        for (int i = 0; i < tasks.size(); i++) {
            JPanel taskPanel = createTaskPanel(tasks.get(i)); // Создаем блок задачи
            taskListContainer.add(taskPanel); // Добавляем блок в контейнер
        }

        taskListContainer.revalidate(); // Обновляем компоновку контейнера
        taskListContainer.repaint(); // Перерисовываем контейнер
    }

    private JPanel createTaskPanel(Task task) {
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setPreferredSize(new Dimension(TASK_PANEL_WIDTH, TASK_PANEL_HEIGHT));

        // Цвет прямоугольника в зависимости от приоритета
        Color priorityColor = switch (task.getPriority()) {
            case 1 -> new Color(255, 150, 150); // Красный
            case 2 -> new Color(255, 200, 150); // Оранжевый
            case 3 -> new Color(150, 255, 150); // Зеленый
            default -> Color.LIGHT_GRAY;
        };

        // Установка цвета фона, не градиента
        taskPanel.setBackground(priorityColor); // Устанавливаем цвет фона

        // Текст задачи (только название и срок)
        JLabel taskLabel = new JLabel("<html><center>" + task.getName() + "<br>" + task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "</center></html>");
        taskLabel.setHorizontalAlignment(SwingConstants.CENTER);
        taskPanel.add(taskLabel, BorderLayout.CENTER);

        // Обработка клика по блоку
        taskPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Одиночный клик
                    // Сброс выделения предыдущей задачи
                    if (selectedTaskPanel != null) {
                        selectedTaskPanel.setBorder(null);
                        // Возвращаем цвет фона предыдущей задачи
                        selectedTaskPanel.setBackground(getColorForPriority(getTaskFromPanel(selectedTaskPanel).getPriority()));
                    }
                    selectedTaskPanel = taskPanel; // Запоминаем выбранную панель
                    taskPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2)); // Выделение синей рамкой
                } else if (e.getClickCount() == 2) { // Двойной щелчок
                    expandTask(taskPanel); // Вызов функции развернуть
                }
            }
        });

        // Добавление красивой рамки
        taskPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1), // Внешняя черная рамка
                new LineBorder(Color.WHITE, 2)  // Внутренняя белая рамка
        ));

        return taskPanel;
    }

    private Color getColorForPriority(int priority) {
        return switch (priority) {
            case 1 -> new Color(255, 150, 150); // Красный
            case 2 -> new Color(255, 200, 150); // Оранжевый
            case 3 -> new Color(150, 255, 150); // Зеленый
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
            JLabel priorityValue = new JLabel("Приоритет: " + task.getPriority());
            JLabel deadlineLabel = new JLabel("Срок выполнения:");
            JLabel deadlineValue = new JLabel(task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

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

    private void editTask(Task task) {
        JFrame dialog = new JFrame(" Редактировать задачу ");
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
        JComboBox<Integer> priorityComboBox = new JComboBox<>(new Integer[]{1, 2, 3});
        priorityComboBox.setSelectedItem(task.getPriority());

        JLabel deadlineLabel = new JLabel("Срок выполнения:");
        JTextField deadlineField = new JTextField(task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

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
        tasks.sort(Comparator.comparing(Task::getDeadline));
        updateTaskList();
    }

    private void sortByImportance() {
        tasks.sort(Comparator.comparingInt(Task::getPriority).reversed()); // Сортировка по важности по убыванию
        updateTaskList();
    }

    private int findSelectedIndex() {
        // Проходим по всем блокам задач в контейнере
        for (int i = 0; i < taskListContainer.getComponentCount(); i++) {
            Component taskPanel = taskListContainer.getComponent(i);
            // Проверяем, является ли текущий блок выделенным (имеет синюю рамку)
            if (taskPanel instanceof JPanel && ((JPanel) taskPanel).getBorder() != null) {
                Border border = ((JPanel) taskPanel).getBorder();
                if (border instanceof LineBorder) {
                    // Дополнительная проверка цвета рамки
                    if (((LineBorder) border).getLineColor() == Color.BLUE) {
                        return i; // Возвращаем индекс блока задачи
                    }
                }
            }
        }
        return -1; // Если ни один блок не выделен, возвращаем -1
    }


    // Методы для сохранения и загрузки задач из файла
    private void saveTasksToFile() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            for (Task task : tasks) {
                writer.write(task.getName() + "," + task.getDescription() + "," + task.getType() + "," + task.getPriority() + "," + task.getDeadline() + "\n");
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
                if (parts.length == 5) {
                    String name = parts[0];
                    String description = parts[1];
                    TaskType type = TaskType.valueOf(parts[2]);
                    int priority = Integer.parseInt(parts[3]);
                    LocalDate deadline = LocalDate.parse(parts[4], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    Task task = new Task(name, description, type, priority, deadline);
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
}
