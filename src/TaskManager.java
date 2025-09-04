import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int nextTaskId = 1;
    private static HashMap<Integer, Object> tasks = new HashMap<>();

    public TaskManager() {
    }

    public static int getNextTaskId() {
        return nextTaskId;
    }

    public static void setNextTaskId(int nextTaskId) {
        TaskManager.nextTaskId = nextTaskId;
    }

    public static HashMap<Integer, Object> getTasks() {
        return tasks;
    }

    public static void setTasks(HashMap<Integer, Object> tasks) {
        TaskManager.tasks = tasks;
    }

    public static void deleteAllTasks() {
        tasks = new HashMap<>();
        nextTaskId = 0;
    }

    public static Object getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    public static boolean isValidGeneralTask(Object task) {
        if (task == null) return false;
        if (task.getClass() == Task.class) return true;
        if (task.getClass() == Epic.class) return true;
        if (task.getClass() == SubTask.class) return true;
        return false;
    }

    public static boolean isValidTask(Object task) {
        if (task == null) return false;
        if (task.getClass() == Task.class) return true;
        return false;
    }

    public static boolean isValidEpic(Object task) {
        if (task == null) return false;
        if (task.getClass() == Epic.class) return true;
        return false;
    }

    public static boolean isValidSubTask(Object task) {
        if (task == null) return false;
        if (task.getClass() == SubTask.class) return true;
        return false;
    }

    public static void createTask(Object task) {
        if (!isValidGeneralTask(task)) return;
        tasks.put(nextTaskId, task);
        nextTaskId += 1;
    }

    public static void updateTask(Object task) {
        if (!isValidGeneralTask(task)) return;
        if (isValidTask(task)) {

            tasks.put(task, task);
        }

        boolean isEpic = isValidEpic((task));
        boolean isSubTask = isValidSubTask(task);
        if (!isEpic && !isSubTask) return;

        if (isEpic) {
            Epic epic = (Epic) task;
            epic.updateEpicStatus();
        } else {
            SubTask subTask = (SubTask) task;
            Epic epic = (Epic) tasks.get(subTask.getEpicId());
            epic.updateEpicStatus();
        }
    }

    public static void deleteTaskById(int taskId) {
        tasks.remove(taskId);
    }

    public static ArrayList<SubTask> getEpicTasks(int epicId) {
        Object task = tasks.get(epicId);
        if (!isValidEpic(task)) return null;

        Epic epic = (Epic) task;
        return epic.getSubTasks();
    }

}
