package services;

import exceptions.ManagerSaveException;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import schemas.enums.TaskStatus;
import schemas.enums.TaskType;
import schemas.tasks.Epic;
import schemas.tasks.SubTask;
import schemas.tasks.Task;
import schemas.tasks.TaskIdComparator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static final String SAVE_FILE_HEADER = "id,type,name,status,description,epic,duration,startTime,endTime";
    private static final int SAVE_FILE_HEADER_LEN = SAVE_FILE_HEADER.split(",").length;
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        this.saveFile = saveFile;
    }

    @Override
    public int createTask(Task task) throws TaskTimeIntersectionException {
        int taskId = super.createTask(task);
        save();
        return taskId;
    }

    @Override
    public int createEpic(Epic task) {
        int taskId = super.createEpic(task);
        save();
        return taskId;
    }

    @Override
    public int createSubTask(SubTask task) throws TaskTimeIntersectionException {
        int taskId = super.createSubTask(task);
        save();
        return taskId;
    }

    @Override
    public void updateTask(Task task) throws TaskNotFoundException, TaskTimeIntersectionException {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic task) throws TaskNotFoundException {
        super.updateEpic(task);
        save();
    }

    @Override
    public void updateSubTask(SubTask task) throws TaskNotFoundException, TaskTimeIntersectionException {
        super.updateSubTask(task);
        save();
    }

    @Override
    public void deleteTaskById(Integer taskId) throws TaskNotFoundException {
        super.deleteTaskById(taskId);
        save();
    }

    @Override
    public void deleteEpicById(Integer taskId) throws TaskNotFoundException {
        super.deleteEpicById(taskId);
        save();
    }

    @Override
    public void deleteSubTaskById(Integer taskId) throws TaskNotFoundException {
        super.deleteSubTaskById(taskId);
        save();
    }

    @Override
    public void deleteAllTasksAllTypes() {
        super.deleteAllTasksAllTypes();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile, StandardCharsets.UTF_8))) {
            writer.write(String.format("%s\n", SAVE_FILE_HEADER));
            List<Task> tasks = getAllTasksAllTypes();
            tasks.sort(new TaskIdComparator());

            tasks.forEach(task -> {
                try {
                    writer.write(String.format("%s\n", task.toCsvString()));
                } catch (IOException e) {
                    throw new ManagerSaveException("Ошибка при работе с файлом сохранения.", e);
                }
            });
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при работе с файлом сохранения.", e);
        }
    }

    public static Task fromCsvString(String value) {
        String[] stringRow = value.split(",", -1);
        if (stringRow.length != SAVE_FILE_HEADER_LEN) return null;

        int taskId = Integer.parseInt(stringRow[0]);
        TaskType taskType = TaskType.valueOf(stringRow[1]);
        String title = stringRow[2];
        TaskStatus taskStatus = TaskStatus.valueOf(stringRow[3]);
        String description = stringRow[4];
        Duration duration = stringRow[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(stringRow[6]));
        LocalDateTime startTime = stringRow[7].isEmpty() ? null : LocalDateTime.parse(stringRow[7]);

        switch (taskType) {
            case TASK -> {
                return new Task(taskId, title, description, taskStatus, duration, startTime);
            }
            case EPIC -> {
                LocalDateTime endTime = stringRow[8].isEmpty() ? null : LocalDateTime.parse(stringRow[8]);
                return new Epic(taskId, title, description, taskStatus, duration, startTime, endTime);
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(stringRow[5]);
                return new SubTask(taskId, title, description, taskStatus, epicId, duration, startTime);
            }
            default -> throw new IllegalArgumentException(String.format("Неизвестный тип задачи: %s", taskType));
        }
    }

    public static FileBackedTaskManager loadFromFile(File saveFile) {
        FileBackedTaskManager manager = new FileBackedTaskManager(saveFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile, StandardCharsets.UTF_8))) {
            reader.readLine(); // пропустить заголовок
            while (reader.ready()) {
                Task task = fromCsvString(reader.readLine());
                if (task instanceof Epic epic) {
                    manager.epics.put(epic.getTaskId(), epic);
                } else if (task instanceof SubTask subTask) {
                    Integer subTaskId = subTask.getTaskId();
                    manager.subTasks.put(subTaskId, subTask);
                    // ниже не должно быть null из-за сортировки при сохранении файла
                    manager.epics.get(subTask.getEpicId()).addSubTaskId(subTaskId);

                } else if (task != null) {
                    manager.tasks.put(task.getTaskId(), task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при работе с файлом сохранения.", e);
        }
        return manager;
    }

    public static void main(String[] args) throws TaskNotFoundException, TaskTimeIntersectionException {
        // пользовательский сценарий

        // Создание первого менеджера
        File tmpSaveFile;
        try {
            tmpSaveFile = File.createTempFile("FileBackedTaskManagerTestFile", null);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при работе с файлом сохранения.", e);
        }
        FileBackedTaskManager taskManager = new FileBackedTaskManager(tmpSaveFile);
        final String title = "testTitle";
        final String description = "testDescription";
        final TaskStatus status = TaskStatus.NEW;

        // Создание нескольких задач, эпиков и подзадач
        int taskId = taskManager.createTask(new Task(title, description, status));
        int taskId2 = taskManager.createTask(new Task(title, description, status));

        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int subTaskId = taskManager.createSubTask(new SubTask(title, description, status, epicId));
        int subTaskId2 = taskManager.createSubTask(new SubTask(title, description, status, epicId));

        int epicId2 = taskManager.createEpic(new Epic(title, description, status));
        int subTaskId3 = taskManager.createSubTask(new SubTask(title, description, status, epicId));

        int epicId3 = taskManager.createEpic(new Epic(title, description, status));
        int subTaskId4 = taskManager.createSubTask(new SubTask(title, description, status, epicId));

        // Создание нового менеджера по сохранённому файлу
        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(tmpSaveFile);

        // Проверить, что все задачи, эпики, подзадачи, которые были в старом менеджере, есть в новом.
        assert newManager.getTaskById(taskId) != null;
        assert newManager.getTaskById(taskId2) != null;

        assert newManager.getEpicById(epicId) != null;
        assert newManager.getSubTaskById(subTaskId) != null;
        assert newManager.getSubTaskById(subTaskId2) != null;

        assert newManager.getEpicById(epicId2) != null;
        assert newManager.getSubTaskById(subTaskId3) != null;

        assert newManager.getEpicById(epicId3) != null;
        assert newManager.getSubTaskById(subTaskId4) != null;

    }
}
