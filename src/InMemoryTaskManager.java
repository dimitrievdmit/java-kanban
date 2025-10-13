import java.util.*;


public class InMemoryTaskManager implements TaskManager {
    private int nextTaskId = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public List<SubTask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return null;

        List<Integer> subTaskIds = epic.getSubTaskIds();

        ArrayList<SubTask> subTasks = new ArrayList<>();
        for (Integer subTaskId : subTaskIds) {
            SubTask subTask = getSubTaskById(subTaskId);
            if (subTask == null) continue;
            subTasks.add(subTask);
        }

        return subTasks;

    }

    @Override
    public void deleteAllTasks() {
        // в связи с усложнением логики методов удаления отдельных задач, метод clear() уже не подходит.
        Set<Integer> independentKeySet = new HashSet<>(tasks.keySet());
        for (Integer taskId : independentKeySet) {
            deleteTaskById(taskId);
        }
    }

    @Override
    public void deleteAllEpics() {
        // в связи с усложнением логики методов удаления отдельных задач, метод clear() уже не подходит
        Set<Integer> independentKeySet = new HashSet<>(epics.keySet());
        for (Integer taskId : independentKeySet) {
            deleteEpicById(taskId);
        }
    }

    @Override
    public void deleteAllSubTasks() {
        // в связи с усложнением логики методов удаления отдельных задач, метод clear() уже не подходит.
        Set<Integer> independentKeySet = new HashSet<>(subTasks.keySet());
        for (Integer taskId : independentKeySet) {
            deleteSubTaskById(taskId);
        }
    }

    @Override
    public void deleteAllTasksAllTypes() {
        deleteAllTasks();
        deleteAllEpics();          // deleteAllEpics включает в себя удаление подзадач
        nextTaskId = 0;
    }

    @Override
    public Task getTaskById(int taskId) {
        Task task = tasks.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int taskId) {
        Epic task = epics.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public SubTask getSubTaskById(int taskId) {
        SubTask task = subTasks.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public int createTask(Task task) {
        int taskId = nextTaskId;
        nextTaskId++;
        task.setTaskId(taskId);
        tasks.put(taskId, task);
        return taskId;
    }

    @Override
    public int createEpic(Epic task) {
        int taskId = nextTaskId;
        nextTaskId++;
        task.setTaskId(taskId);

        // Перезаписать статус на NEW, так как нет подзадач
        task.setTaskStatus(TaskStatus.NEW);
        excludeNonExistentSubTaskIds(task);
        epics.put(taskId, task);
        return taskId;
    }

    @Override
    public int createSubTask(SubTask task) {
        int taskId = nextTaskId;
        if (taskId == task.getEpicId()) return -1;
        Epic epic = epics.get(task.getEpicId());
        if (epic == null) return -1;

        nextTaskId++;
        task.setTaskId(taskId);
        subTasks.put(taskId, task);

        epic.addSubTaskId(taskId);
        updateEpicStatus(epic);
        return taskId;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getTaskId())) return;
        tasks.put(task.getTaskId(), task);
    }

    @Override
    public void updateEpic(Epic task) {
        if (!epics.containsKey(task.getTaskId())) return;
        excludeNonExistentSubTaskIds(task);
        epics.put(task.getTaskId(), task);
        updateEpicStatus(task);
    }

    @Override
    public void updateSubTask(SubTask task) {
        if (!subTasks.containsKey(task.getTaskId())) return;
        Epic epic = epics.get(task.getEpicId());
        if (epic == null) return;

        subTasks.put(task.getTaskId(), task);

        updateEpicStatus(epic);
    }

    @Override
    public void deleteTaskById(Integer taskId) {
        tasks.remove(taskId);
        historyManager.remove(taskId);
    }

    @Override
    public void deleteEpicById(Integer taskId) {
        Epic epic = getEpicById(taskId);
        epics.remove(taskId);
        historyManager.remove(taskId);

        if (epic.getSubTaskIds().isEmpty()) return;
        for (Integer subTaskId : epic.getSubTaskIds()) {
            deleteSubTaskById(subTaskId);
        }
    }

    @Override
    public void deleteSubTaskById(Integer taskId) {
        SubTask subtask = getSubTaskById(taskId);
        subTasks.remove(taskId);
        historyManager.remove(taskId);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return;
        if (!epic.getSubTaskIds().contains(taskId)) return;
        epic.removeSubTaskId(taskId);
        updateEpicStatus(epic);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        if (epic == null) return;

        if (epic.getSubTaskIds().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            return;
        }
        List<SubTask> subTasks = getEpicSubtasks(epic.getTaskId());
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

    private void excludeNonExistentSubTaskIds(Epic epic) {
        Set<Integer> independentKeySet = new HashSet<>(epic.getSubTaskIds());
        for (Integer subTaskId : independentKeySet) {
            if (!subTasks.containsKey(subTaskId)) {
                epic.removeSubTaskId(subTaskId);
            }
        }
    }
}
