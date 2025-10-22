import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static final String SAVE_FILE_HEADER = "id,type,name,status,description,epic";
    private static final int SAVE_FILE_HEADER_LEN = SAVE_FILE_HEADER.split(",").length;
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        this.saveFile = saveFile;
    }

    public FileBackedTaskManager(InMemoryTaskManager manager, File saveFile) {
        super(manager.nextTaskId, manager.tasks, manager.epics, manager.subTasks, manager.historyManager);
        this.saveFile = saveFile;
    }

    @Override
    public int createTask(Task task) {
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
    public int createSubTask(SubTask task) {
        int taskId = super.createSubTask(task);
        save();
        return taskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic task) {
        super.updateEpic(task);
        save();
    }

    @Override
    public void updateSubTask(SubTask task) {
        super.updateSubTask(task);
        save();
    }

    @Override
    public void deleteTaskById(Integer taskId) {
        super.deleteTaskById(taskId);
        save();
    }

    @Override
    public void deleteEpicById(Integer taskId) {
        super.deleteEpicById(taskId);
        save();
    }

    @Override
    public void deleteSubTaskById(Integer taskId) {
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
            for (Task task : tasks) {
                writer.write(String.format("%s\n", task.toCsvString()));
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
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

        switch (taskType) {
            case TASK -> {
                return new Task(taskId, title, description, taskStatus);
            }
            case EPIC -> {
                return new Epic(taskId, title, description, taskStatus);
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(stringRow[5]);
                return new SubTask(taskId, title, description, taskStatus, epicId);
            }
            default -> throw new IllegalArgumentException(String.format("Неизвестный тип задачи: %s", taskType));
        }
    }

    public static FileBackedTaskManager loadFromFile(File saveFile) {
        InMemoryTaskManager tempManager = new InMemoryTaskManager();

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile, StandardCharsets.UTF_8))) {
            reader.readLine(); // пропустить заголовок
            while (reader.ready()) {
                Task task = fromCsvString(reader.readLine());
                if (task instanceof Epic epic) {
                    tempManager.createEpic(epic);
                } else if (task instanceof SubTask subTask) {
                    tempManager.createSubTask(subTask);
                } else if (task != null) {
                    tempManager.createTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
        return new FileBackedTaskManager(tempManager, saveFile);
    }

    public static void main(String[] args) {
        // пользовательский сценарий

        // Создание первого менеджера
        File tmpSaveFile;
        try {
            tmpSaveFile = File.createTempFile("FileBackedTaskManagerTestFile", null);
        } catch (IOException e) {
            throw new ManagerSaveException();
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
