import java.util.Collection;
import java.util.HashMap;

public class TaskManager {
    private static int nextTaskId = 1;
    private static HashMap<Integer, Task> tasks = new HashMap<>();
    private static HashMap<Integer, Epic> epics = new HashMap<>();
    private static HashMap<Integer, SubTask> subTasks = new HashMap<>();

    public TaskManager() {
    }

    public static int getNextTaskId() {
        return nextTaskId;
    }

    public static void setNextTaskId(int nextTaskId) {
        TaskManager.nextTaskId = nextTaskId;
    }

    public static HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public static Collection<Task> getTaskArray() {
        return tasks.values();
    }

    public static void setTasks(HashMap<Integer, Task> tasks) {
        TaskManager.tasks = tasks;
    }

    public static HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public static Collection<Epic> getEpicArray() {
        return epics.values();
    }

    public static void setEpics(HashMap<Integer, Epic> epics) {
        TaskManager.epics = epics;
    }

    public static HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public static void setSubTasks(HashMap<Integer, SubTask> subTasks) {
        TaskManager.subTasks = subTasks;
    }

    public static Collection<SubTask> getSubTaskArray() {
        return subTasks.values();
    }

    public static void deleteAllTasks() {
        tasks = new HashMap<>();
    }

    public static void deleteAllEpics() {
        epics = new HashMap<>();
    }

    public static void deleteAllSubTasks() {
        subTasks = new HashMap<>();
    }

    public static void deleteAllTasksAllTypes() {
        deleteAllTasks();
        deleteAllEpics();
        deleteAllSubTasks();
        nextTaskId = 0;
    }

    public static Task getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    public static Epic getEpicById(int taskId) {
        return epics.get(taskId);
    }

    public static SubTask getSubTaskById(int taskId) {
        return subTasks.get(taskId);
    }

    public static void createTask(Task task) {
        tasks.put(nextTaskId, task);
        nextTaskId += 1;
    }

    public static void createEpic(Epic task) {
        epics.put(nextTaskId, task);
        nextTaskId += 1;
        task.updateEpicStatus();
    }

    public static void createSubTask(SubTask task) {
        subTasks.put(nextTaskId, task);
        Epic epic = epics.get(task.getEpicId());
        epic.updateSubTask(nextTaskId, task);
        nextTaskId += 1;
    }

    public static void updateTask(Task task) {
        tasks.put(task.getTaskId(), task);
    }

    public static void updateEpic(Epic task) {
        epics.put(task.getTaskId(), task);
        task.updateEpicStatus();
    }

    public static void updateSubTask(SubTask task) {
        subTasks.put(task.getTaskId(), task);
        Epic epic = epics.get(task.getEpicId());
        epic.updateSubTask(task.getTaskId(), task);
    }

    public static void deleteTaskById(int taskId) {
        tasks.remove(taskId);
    }

    public static void deleteEpicById(int taskId) {
        Epic epic = getEpicById(taskId);
        HashMap<Integer, SubTask> epicSubTasks = epic.getSubTasks();
        epics.remove(taskId);
        if (epicSubTasks.isEmpty()) return;
        for (SubTask subTask : epicSubTasks.values()) {
            convertSubTaskToTask(subTask);
        }
    }

    public static void deleteSubTaskById(int taskId) {
        SubTask subtask = getSubTaskById(taskId);
        subTasks.remove(taskId);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return;
        epic.deleteSubTaskById(taskId);
        epic.updateEpicStatus();
    }

    public static HashMap<Integer, SubTask> getSubTasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        return epic.getSubTasks();
    }

    public static void convertSubTaskToTask(SubTask subTask) {
        Task task = new Task(subTask.getTaskId(), subTask.getTitle(), subTask.getDescription(), subTask.getTaskStatus());
        deleteSubTaskById(subTask.getTaskId());
        updateTask(task);
    }
}
