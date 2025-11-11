import exceptions.TaskTimeIntersectionException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class InMemoryTaskManager implements TaskManager {
    protected int nextTaskId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, SubTask> subTasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(
                    task -> task.startTime,
                    Comparator.nullsFirst(Comparator.naturalOrder())
            )
    );

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
    public List<Task> getAllTasksAllTypes() {
        List<Task> allTasks = new ArrayList<>(getTasks());
        allTasks.addAll(getEpics());
        allTasks.addAll(getSubTasks());
        return allTasks;
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    @Override
    public List<SubTask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return null;

        List<Integer> subTaskIds = epic.getSubTaskIds();

        ArrayList<SubTask> subTasks = new ArrayList<>();
        subTaskIds.stream()
                .map(this::getSubTaskById)
                .filter(Objects::nonNull)
                .forEach(subTasks::add);

        return subTasks;

    }

    @Override
    public void deleteAllTasks() {
        new HashSet<>(tasks.keySet()).forEach(this::deleteTaskById);
    }

    @Override
    public void deleteAllEpics() {
        new HashSet<>(epics.keySet()).forEach(this::deleteEpicById);
    }

    @Override
    public void deleteAllSubTasks() {
        new HashSet<>(subTasks.keySet()).forEach(this::deleteSubTaskById);
    }

    @Override
    public void deleteAllTasksAllTypes() {
        deleteAllTasks();
        deleteAllEpics();  // deleteAllEpics включает в себя удаление подзадач
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
        if (taskIntersectsExistingTasks(task)) {
            throw new TaskTimeIntersectionException("Задача пересекается по времени выполнения с существующей.");
        }

        int taskId = nextTaskId;
        nextTaskId++;
        task.setTaskId(taskId);
        tasks.put(taskId, task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);
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
        if (taskIntersectsExistingTasks(task)) {
            throw new TaskTimeIntersectionException("Подзадача пересекается по времени выполнения с существующей.");
        }

        int taskId = nextTaskId;
        if (taskId == task.getEpicId()) return -1;
        Epic epic = epics.get(task.getEpicId());
        if (epic == null) return -1;

        nextTaskId++;
        task.setTaskId(taskId);
        subTasks.put(taskId, task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);

        epic.addSubTaskId(taskId);
        updateEpicStatus(epic);
        updateEpicTime(epic);
        return taskId;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getTaskId())) return;
        if (taskIntersectsExistingTasks(task)) {
            throw new TaskTimeIntersectionException("Задача пересекается по времени выполнения с существующей.");
        }
        tasks.put(task.getTaskId(), task);
        updatePrioritizedTasks(task);
    }

    @Override
    public void updateEpic(Epic task) {
        if (!epics.containsKey(task.getTaskId())) return;
        excludeNonExistentSubTaskIds(task);
        epics.put(task.getTaskId(), task);
        updateEpicStatus(task);
        updateEpicTime(task);
    }

    @Override
    public void updateSubTask(SubTask task) {
        if (!subTasks.containsKey(task.getTaskId())) return;
        if (taskIntersectsExistingTasks(task)) {
            throw new TaskTimeIntersectionException("Подзадача пересекается по времени выполнения с существующей.");
        }
        Epic epic = epics.get(task.getEpicId());
        if (epic == null) return;

        subTasks.put(task.getTaskId(), task);
        updatePrioritizedTasks(task);

        updateEpicStatus(epic);
        updateEpicTime(epic);
    }

    @Override
    public void deleteTaskById(Integer taskId) {
        Task task = getTaskById(taskId);
        tasks.remove(taskId);
        historyManager.remove(taskId);
        prioritizedTasks.remove(task);
    }

    @Override
    public void deleteEpicById(Integer taskId) {
        Epic epic = getEpicById(taskId);
        epics.remove(taskId);
        historyManager.remove(taskId);
        prioritizedTasks.remove(epic);

        epic.getSubTaskIds().forEach(this::deleteSubTaskById);
    }

    @Override
    public void deleteSubTaskById(Integer taskId) {
        SubTask subtask = getSubTaskById(taskId);
        subTasks.remove(taskId);
        historyManager.remove(taskId);
        prioritizedTasks.remove(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return;
        if (!epic.getSubTaskIds().contains(taskId)) return;
        epic.removeSubTaskId(taskId);
        updateEpicStatus(epic);
        updateEpicTime(epic);
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

        boolean isAllNew = subTasks.stream().allMatch(subTask -> subTask.getTaskStatus() == TaskStatus.NEW);
        boolean isAllDone = subTasks.stream().allMatch(subTask -> subTask.getTaskStatus() == TaskStatus.DONE);

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

    private void updateEpicTime(Epic epic) {
        if (epic == null) return;

        Duration newDuration = Duration.ofMinutes(
                getEpicSubtasks(epic.getTaskId()).stream()
                        .map(subTask -> subTask.duration)
                        .filter(Objects::nonNull)
                        .mapToLong(Duration::toMinutes)
                        .sum()
        );
        epic.setDuration(newDuration);

        LocalDateTime newStartTime = getEpicSubtasks(epic.getTaskId()).stream()
                .map(subTask -> subTask.startTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        epic.setStartTime(newStartTime);

        LocalDateTime endTime;
        if (newDuration == null || newStartTime == null) {
            endTime = null;
        } else {
            endTime = newStartTime.plus(newDuration);
        }
        epic.setEndTime(endTime);
    }

    private void updatePrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        } else {
            prioritizedTasks.remove(task);
        }
    }

    private void excludeNonExistentSubTaskIds(Epic epic) {
        new HashSet<>(epic.getSubTaskIds()).stream()
                .filter(subTaskId -> !subTasks.containsKey(subTaskId))
                .forEach(epic::removeSubTaskId);
    }

    private boolean taskIntersectsExistingTasks(Task task) {
        return getPrioritizedTasks().stream().anyMatch(task::tasksIntersect);
    }
}
