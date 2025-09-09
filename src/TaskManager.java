import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TaskManager {
    private int nextTaskId = 1;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();

    public int getNextTaskId() {
        return nextTaskId;
    }

    public Collection<Task> getTaskArray() {
        return tasks.values();
    }

    public Collection<Epic> getEpicArray() {
        return epics.values();
    }

    public Collection<SubTask> getSubTaskArray() {
        return subTasks.values();
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        deleteAllSubTasks();
    }

    public void deleteAllSubTasks() {
        subTasks.clear();
    }

    public void deleteAllTasksAllTypes() {
        deleteAllTasks();
        deleteAllEpics();
        nextTaskId = 0;
    }

    public Task getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    public Epic getEpicById(int taskId) {
        return epics.get(taskId);
    }

    public SubTask getSubTaskById(int taskId) {
        return subTasks.get(taskId);
    }

    public int createTask(Task task) {
        int taskId = nextTaskId;
        task.setTaskId(taskId);
        tasks.put(nextTaskId, task);
        nextTaskId++;
        return taskId;
    }

    public int createEpic(Epic task) {
        int taskId = nextTaskId;
        task.setTaskId(taskId);
        // Если не обновляем статус, то мы должны его перезаписать, чтобы выполнить ТЗ в части "NEW, если нет подзадач"
        task.setTaskStatus(TaskStatus.NEW);
        epics.put(nextTaskId, task);
        nextTaskId++;
        return taskId;
    }

    public int createSubTask(SubTask task) {
        int taskId = nextTaskId;
        task.setTaskId(taskId);
        subTasks.put(nextTaskId, task);
        Epic epic = epics.get(task.getEpicId());
        epic.addSubTaskId(nextTaskId);
        updateEpicStatus(epic);
        nextTaskId++;
        return taskId;
    }

    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getTaskId())) return;
        tasks.put(task.getTaskId(), task);
    }

    public void updateEpic(Epic task) {
        if (!epics.containsKey(task.getTaskId())) return;
        epics.put(task.getTaskId(), task);
        updateEpicStatus(task);
    }

    public void updateSubTask(SubTask task) {
        if (!subTasks.containsKey(task.getTaskId())) return;

        subTasks.put(task.getTaskId(), task);

        Epic epic = epics.get(task.getEpicId());
        updateEpicStatus(epic);
    }

    public void deleteTaskById(Integer taskId) {
        tasks.remove(taskId);
    }

    public void deleteEpicById(Integer taskId) {
        Epic epic = getEpicById(taskId);
        epics.remove(taskId);

        if (epic.getSubTaskIds().isEmpty()) return;
        for (Integer subTaskId : epic.getSubTaskIds()) {
            deleteSubTaskById(subTaskId);
        }
    }

    public void deleteSubTaskById(Integer taskId) {
        SubTask subtask = getSubTaskById(taskId);
        subTasks.remove(taskId);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return;
        if (!epic.getSubTaskIds().contains(taskId)) return;
        epic.removeSubTaskId(taskId);
        updateEpicStatus(epic);
    }

    public ArrayList<SubTask> getSubTaskArrayByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return null;

        ArrayList<Integer> subTaskIds = epic.getSubTaskIds();

        ArrayList<SubTask> subTasks = new ArrayList<>();
        for (Integer subTaskId : subTaskIds) {
            SubTask subTask = getSubTaskById(subTaskId);
            if (subTask == null) continue;
            subTasks.add(subTask);
        }

        return subTasks;

    }

    private void updateEpicStatus(Epic epic) {
        if (epic == null) return;

        if (epic.getSubTaskIds().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            return;
        }
        ArrayList<SubTask> subTasks = getSubTaskArrayByEpic(epic.getTaskId());
        if (subTasks == null) return;

        boolean isAllNew = true;
        boolean isAllDone = true;

        for (SubTask subTask : subTasks) {

            if (subTask.getTaskStatus() != TaskStatus.NEW) {
                isAllNew = false;
            }
            if (subTask.getTaskStatus() != TaskStatus.DONE) {
                isAllDone = false;
            }
            if (!isAllNew && !isAllDone) break;
        }

        if (isAllNew) {
            epic.setTaskStatus(TaskStatus.NEW);
            return;
        }
        if (isAllDone) {
            epic.setTaskStatus(TaskStatus.DONE);
            return;
        }
        epic.setTaskStatus(TaskStatus.IN_PROGRESS);
    }
}
