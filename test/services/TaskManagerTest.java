package services;

import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemas.enums.TaskStatus;
import schemas.tasks.Epic;
import schemas.tasks.SubTask;
import schemas.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;


public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    public final static String TITLE = "testTitle";
    public final static String DESCRIPTION = "testDescription";
    public final static TaskStatus STATUS = TaskStatus.NEW;
    public final static Duration DURATION = Duration.ofMinutes(2);
    public final static LocalDateTime START_TIME = LocalDateTime.now();

    public final static String NEW_TITLE = "new" + TITLE;
    public final static String NEW_DESCRIPTION = "new" + DESCRIPTION;
    public final static TaskStatus NEW_STATUS = TaskStatus.IN_PROGRESS;
    public final static LocalDateTime NEW_START_TIME = START_TIME.plus(DURATION).plusSeconds(5);

    abstract T getManager();

    @BeforeEach
    void setup() {
        taskManager = getManager();
    }

    public static Task getDefaultTask() {
        return new Task(TITLE, DESCRIPTION, STATUS, DURATION, START_TIME);
    }

    public static Task getDefaultTask(LocalDateTime startTime) {
        return new Task(TITLE, DESCRIPTION, STATUS, DURATION, startTime);
    }

    public static Epic getDefaultEpic() {
        return new Epic(TITLE, DESCRIPTION, STATUS, DURATION, START_TIME);
    }

    public static SubTask getDefaultSubTask(int epicId) {
        return new SubTask(TITLE, DESCRIPTION, STATUS, epicId, DURATION, NEW_START_TIME);
    }

    public static SubTask getDefaultSubTask(int epicId, LocalDateTime subTaskStartTime) {
        return new SubTask(TITLE, DESCRIPTION, STATUS, epicId, DURATION, subTaskStartTime);
    }

    // Проверки создания задач
    @Test
    void shouldCreateTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        Task task = getDefaultTask();
        int taskId = taskManager.createTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }


    @Test
    void shouldCreateEpic() throws TaskNotFoundException {
        Epic epic = getDefaultEpic();
        int taskId = taskManager.createEpic(epic);

        final Epic savedTask = taskManager.getEpicById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(epic, savedTask, "Задачи не совпадают.");
    }


    @Test
    void shouldCreateSubTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        SubTask subTask = getDefaultSubTask(epicId);
        int taskId = taskManager.createSubTask(subTask);

        final SubTask savedTask = taskManager.getSubTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(subTask, savedTask, "Задачи не совпадают.");
    }

    // Проверки получения задач
    @Test
    void shouldGetAllTasks() throws TaskNotFoundException, TaskTimeIntersectionException {
        int taskId1 = taskManager.createTask(getDefaultTask());
        int taskId2 = taskManager.createTask(getDefaultTask(NEW_START_TIME));

        Task task1 = taskManager.getTaskById(taskId1);
        Task task2 = taskManager.getTaskById(taskId2);

        ArrayList<Task> expectedTasks = new ArrayList<>(2);
        expectedTasks.add(task1);
        expectedTasks.add(task2);

        List<Task> actualTasks = taskManager.getTasks();

        assertNotNull(actualTasks, "Задачи не возвращаются.");
        assertEquals(2, actualTasks.size(), "Неверное количество задач.");
        // Был удивлён, что ArrayList не является массивом и assertArrayEquals не подходит
        assertEquals(expectedTasks, actualTasks, "Задачи не совпадают.");

    }

    @Test
    void shouldGetAllSubTasks() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int epicId2 = taskManager.createEpic(getDefaultEpic());
        int subTaskId1 = taskManager.createSubTask(getDefaultSubTask(epicId, START_TIME));
        int subTaskId2 = taskManager.createSubTask(getDefaultSubTask(epicId2, START_TIME.plus(DURATION).plusDays(5)));

        SubTask task1 = taskManager.getSubTaskById(subTaskId1);
        SubTask task2 = taskManager.getSubTaskById(subTaskId2);

        ArrayList<SubTask> expectedTasks = new ArrayList<>(2);
        expectedTasks.add(task1);
        expectedTasks.add(task2);

        List<SubTask> actualTasks = taskManager.getSubTasks();

        assertNotNull(actualTasks, "Задачи не возвращаются.");
        assertEquals(2, actualTasks.size(), "Неверное количество задач.");
        // Был удивлён, что ArrayList не является массивом и assertArrayEquals не подходит
        assertEquals(expectedTasks, actualTasks, "Задачи не совпадают.");
    }

    @Test
    void shouldGetAllEpics() throws TaskNotFoundException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int epicId2 = taskManager.createEpic(getDefaultEpic());

        Epic task1 = taskManager.getEpicById(epicId);
        Epic task2 = taskManager.getEpicById(epicId2);

        ArrayList<Epic> expectedTasks = new ArrayList<>(2);
        expectedTasks.add(task1);
        expectedTasks.add(task2);

        List<Epic> actualTasks = taskManager.getEpics();

        assertNotNull(actualTasks, "Задачи не возвращаются.");
        assertEquals(2, actualTasks.size(), "Неверное количество задач.");
        assertEquals(expectedTasks, actualTasks, "Задачи не совпадают.");
    }

    @Test
    void shouldGetAllSubTasksByEpicId() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int subTaskId1 = taskManager.createSubTask(getDefaultSubTask(epicId, START_TIME));
        int subTaskId2 = taskManager.createSubTask(getDefaultSubTask(epicId, START_TIME.plus(DURATION).plusDays(5)));

        int epicId2 = taskManager.createEpic(getDefaultEpic());
        taskManager.createSubTask(getDefaultSubTask(epicId2));

        SubTask task1 = taskManager.getSubTaskById(subTaskId1);
        SubTask task2 = taskManager.getSubTaskById(subTaskId2);

        ArrayList<SubTask> expectedTasks = new ArrayList<>(2);
        expectedTasks.add(task1);
        expectedTasks.add(task2);

        List<SubTask> actualTasks = taskManager.getEpicSubtasks(epicId);

        assertNotNull(actualTasks, "Задачи не возвращаются.");
        assertEquals(2, actualTasks.size(), "Неверное количество задач.");
        assertEquals(expectedTasks, actualTasks, "Задачи не совпадают.");
    }

    // Проверки обновления задач
    @Test
    void shouldUpdateTaskFields() throws TaskNotFoundException, TaskTimeIntersectionException {
        int taskId = taskManager.createTask(getDefaultTask());

        Task task = new Task(taskId, NEW_TITLE, NEW_DESCRIPTION, NEW_STATUS);
        taskManager.updateTask(task);

        Task actualTask = taskManager.getTaskById(taskId);

        assertNotEquals(TITLE, actualTask.getTitle(), "Задача не обновилась.");
        assertNotEquals(DESCRIPTION, actualTask.getDescription(), "Задача не обновилась.");
        assertNotEquals(STATUS, actualTask.getTaskStatus(), "Задача не обновилась.");
        assertNotEquals(DURATION, actualTask.getDuration(), "Задача не обновилась.");
        assertNotEquals(START_TIME, actualTask.getStartTime(), "Задача не обновилась.");
    }

    @Test
    void shouldUpdateSubTaskFields() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int taskId = taskManager.createSubTask(getDefaultSubTask(epicId));

        SubTask task = new SubTask(taskId, NEW_TITLE, NEW_DESCRIPTION, NEW_STATUS, epicId);
        taskManager.updateSubTask(task);

        SubTask actualTask = taskManager.getSubTaskById(taskId);

        assertNotEquals(TITLE, actualTask.getTitle(), "Задача не обновилась.");
        assertNotEquals(DESCRIPTION, actualTask.getDescription(), "Задача не обновилась.");
        assertNotEquals(STATUS, actualTask.getTaskStatus(), "Задача не обновилась.");
        assertNotEquals(DURATION, actualTask.getDuration(), "Задача не обновилась.");
        assertNotEquals(NEW_START_TIME, actualTask.getStartTime(), "Задача не обновилась.");
    }

    @Test
    void shouldUpdateEpicFields() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());

        Epic task = new Epic(epicId, NEW_TITLE, NEW_DESCRIPTION, STATUS);
        taskManager.updateEpic(task);
        ArrayList<Integer> oldSubTaskIds = new ArrayList<>(task.getSubTaskIds());

        TaskStatus subTaskStatus = TaskStatus.IN_PROGRESS;
        SubTask subTask = new SubTask(TITLE, DESCRIPTION, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        Epic actualTask = taskManager.getEpicById(epicId);

        assertNotEquals(TITLE, actualTask.getTitle(), "Задача не обновилась.");
        assertNotEquals(DESCRIPTION, actualTask.getDescription(), "Задача не обновилась.");
        assertNotEquals(STATUS, actualTask.getTaskStatus(), "Задача не обновилась.");
        assertNotEquals(DURATION, actualTask.getDuration(), "Задача не обновилась.");
        assertNotEquals(START_TIME, actualTask.getStartTime(), "Задача не обновилась.");
        assertNotEquals(oldSubTaskIds, actualTask.getSubTaskIds(), "Задача не обновилась.");
    }

    // Проверки удаления задач
    @Test
    void shouldDeleteTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        int taskId = taskManager.createTask(getDefaultTask());
        taskManager.deleteTaskById(taskId);

        assertThrows(TaskNotFoundException.class, () -> taskManager.getTaskById(taskId), "Задача не удалилась.");
    }

    @Test
    void shouldDeleteSubTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int taskId = taskManager.createSubTask(getDefaultSubTask(epicId));
        taskManager.deleteSubTaskById(taskId);

        assertThrows(TaskNotFoundException.class, () -> taskManager.getSubTaskById(taskId), "Задача не удалилась.");
    }

    @Test
    void shouldDeleteEpic() throws TaskNotFoundException {
        int taskId = taskManager.createEpic(getDefaultEpic());

        taskManager.deleteEpicById(taskId);

        assertThrows(TaskNotFoundException.class, () -> taskManager.getEpicById(taskId), "Задача не удалилась.");
    }

    @Test
    void shouldDeleteAllTasks() throws TaskTimeIntersectionException {
        taskManager.createTask(getDefaultTask());
        taskManager.createTask(getDefaultTask(NEW_START_TIME));

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteAllSubTasks() throws TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        taskManager.createSubTask(getDefaultSubTask(epicId, START_TIME));
        taskManager.createSubTask(getDefaultSubTask(epicId, START_TIME.plus(DURATION).plusDays(5)));

        taskManager.deleteAllSubTasks();

        assertTrue(taskManager.getSubTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteAllEpicsAndSubTasksWhenDeletingAllEpics() throws TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int epicId2 = taskManager.createEpic(getDefaultEpic());
        taskManager.createSubTask(getDefaultSubTask(epicId, START_TIME));
        taskManager.createSubTask(getDefaultSubTask(epicId2, START_TIME.plus(DURATION).plusDays(5)));

        taskManager.deleteAllEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Задачи не удалились.");
        assertTrue(taskManager.getSubTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteEpicSubTasksWhenDeletingEpic() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int subTaskId = taskManager.createSubTask(getDefaultSubTask(epicId));

        taskManager.deleteEpicById(epicId);

        assertThrows(TaskNotFoundException.class, () -> taskManager.getSubTaskById(subTaskId), "Задача не удалилась.");
    }

    // Проверки, что поля не изменяются в процессе создания
    @Test
    void shouldKeepGivenFieldsWhenCreatingTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        Task task = getDefaultTask();
        int taskId = taskManager.createTask(task);
        Task actualTask = taskManager.getTaskById(taskId);

        assertEquals(task.getTitle(), actualTask.getTitle());
        assertEquals(task.getDescription(), actualTask.getDescription());
        assertEquals(task.getTaskStatus(), actualTask.getTaskStatus());
        assertEquals(task.getDuration(), actualTask.getDuration());
        assertEquals(task.getStartTime(), actualTask.getStartTime());
    }


    @Test
    void shouldKeepGivenFieldsWhenCreatingSubTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        SubTask task = getDefaultSubTask(epicId);
        int taskId = taskManager.createSubTask(task);
        SubTask actualTask = taskManager.getSubTaskById(taskId);

        assertEquals(task.getTitle(), actualTask.getTitle());
        assertEquals(task.getDescription(), actualTask.getDescription());
        assertEquals(task.getTaskStatus(), actualTask.getTaskStatus());
        assertEquals(task.getDuration(), actualTask.getDuration());
        assertEquals(task.getStartTime(), actualTask.getStartTime());
        assertEquals(task.getEpicId(), actualTask.getEpicId());
    }


    @Test
    void shouldKeepGivenFieldsWhenCreatingEpic() throws TaskNotFoundException {
        Epic task = getDefaultEpic();
        int taskId = taskManager.createEpic(task);
        Epic actualTask = taskManager.getEpicById(taskId);

        assertEquals(task.getTitle(), actualTask.getTitle());
        assertEquals(task.getDescription(), actualTask.getDescription());
        // Статус может отличаться от данного в связи с особыми правилами для эпиков.
        assertEquals(task.getDuration(), actualTask.getDuration());
        assertEquals(task.getStartTime(), actualTask.getStartTime());
    }

    // Проверки расчёта статуса Эпика
    @Test
    void epicShouldBeNewWithOneNewSubTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask(TITLE, DESCRIPTION, TaskStatus.NEW, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeNewWithTwoNewSubTasks() throws TaskNotFoundException, TaskTimeIntersectionException {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.NEW;
        SubTask subTask = new SubTask(TITLE, DESCRIPTION, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        SubTask subTask2 = new SubTask(TITLE, DESCRIPTION, subTaskStatus, epicId);
        taskManager.createSubTask(subTask2);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeInProgressWithOneInProgressSubTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask(TITLE, DESCRIPTION, TaskStatus.IN_PROGRESS, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeInProgressWithTwoInProgressSubTasks() throws TaskNotFoundException, TaskTimeIntersectionException {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.IN_PROGRESS;
        SubTask subTask = new SubTask(TITLE, DESCRIPTION, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        SubTask subTask2 = new SubTask(TITLE, DESCRIPTION, subTaskStatus, epicId);
        taskManager.createSubTask(subTask2);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeDoneWithOneDoneSubTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask(TITLE, DESCRIPTION, TaskStatus.DONE, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeDoneWithTwoDoneSubTasks() throws TaskNotFoundException, TaskTimeIntersectionException {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.DONE;
        SubTask subTask = new SubTask(TITLE, DESCRIPTION, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        SubTask subTask2 = new SubTask(TITLE, DESCRIPTION, subTaskStatus, epicId);
        taskManager.createSubTask(subTask2);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeInProgressWithOneNewAndOneDoneSubTasks() throws TaskNotFoundException, TaskTimeIntersectionException {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask(TITLE, DESCRIPTION, TaskStatus.NEW, epicId);
        taskManager.createSubTask(subTask);

        SubTask subTask2 = new SubTask(TITLE, DESCRIPTION, TaskStatus.DONE, epicId);
        taskManager.createSubTask(subTask2);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeNewWhenDeletingLastSubTask() throws TaskNotFoundException, TaskTimeIntersectionException {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.DONE;
        SubTask subTask = new SubTask(TITLE, DESCRIPTION, subTaskStatus, epicId);
        int subTaskId = taskManager.createSubTask(subTask);

        taskManager.deleteSubTaskById(subTaskId);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, actualEpic.getTaskStatus());
    }

    // Проверки работы с невалидными данными при создании задач
    @Test
    void shouldIgnoreStatusWhenCreatingEpic() throws TaskNotFoundException {
        TaskStatus wrongStatus = TaskStatus.DONE;
        Epic task = new Epic(TITLE, DESCRIPTION, wrongStatus);
        int taskId = taskManager.createEpic(task);
        Epic actualTask = taskManager.getEpicById(taskId);

        assertNotEquals(wrongStatus, actualTask.getTaskStatus(), "Эпик создался с неверным статусом");
    }

    @Test
    void shouldIgnoreGivenIdWhenCreatingTask() throws TaskTimeIntersectionException {
        Task task = getDefaultTask();
        int taskId = 9999999;
        task.setTaskId(taskId);
        int actualTaskId = taskManager.createTask(task);
        assertNotEquals(taskId, actualTaskId);
    }

    @Test
    void shouldIgnoreGivenIdWhenCreatingSubTask() throws TaskTimeIntersectionException {
        int taskId = 9999999;
        int epicId = taskManager.createEpic(getDefaultEpic());
        SubTask task = new SubTask(taskId, TITLE, DESCRIPTION, STATUS, epicId);
        int actualTaskId = taskManager.createSubTask(task);
        assertNotEquals(taskId, actualTaskId);
    }

    @Test
    void shouldIgnoreGivenIdWhenCreatingEpic() {
        int taskId = 9999999;
        Epic task = new Epic(taskId, TITLE, DESCRIPTION, STATUS);
        int actualTaskId = taskManager.createEpic(task);
        assertNotEquals(taskId, actualTaskId);
    }

    @Test
    void shouldNotCreateSubTaskWithoutValidEpicId() throws TaskTimeIntersectionException {
        int subTaskId = taskManager.createSubTask(getDefaultSubTask(999));
        assertEquals(-1, subTaskId, "Отсутствует валидный связанный эпик.");
    }

    @Test
    void shouldNotUpdateSubTaskToInValidEpicId() throws TaskNotFoundException, TaskTimeIntersectionException {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int subTaskId = taskManager.createSubTask(getDefaultSubTask(epicId));
        int wrongEpicId = 999;
        taskManager.updateSubTask(new SubTask(subTaskId, TITLE, DESCRIPTION, STATUS, wrongEpicId));
        assertNotEquals(wrongEpicId, taskManager.getSubTaskById(subTaskId).getEpicId(), "Эпик обновился на несуществующий.");

        int actualEpicId = taskManager.getEpicById(epicId).getTaskId();
        assertEquals(actualEpicId, epicId, "Эпик обновился на несуществующий.");
    }

    @Test
    void shouldIgnoreGivenNonExistentSubTaskIdsWhenCreatingEpic() {
        Epic epic = getDefaultEpic();

        epic.addSubTaskId(epic.getTaskId() - 1);
        taskManager.createEpic(epic);

        List<Integer> expectedSubTaskIds = new ArrayList<>();
        assertEquals(epic.getSubTaskIds(), expectedSubTaskIds);
    }

    @Test
    void shouldIgnoreGivenNonExistentSubTaskIdsWhenUpdatingEpic() throws TaskNotFoundException {
        Epic epic = getDefaultEpic();
        taskManager.createEpic(epic);

        epic.addSubTaskId(epic.getTaskId() - 1);
        taskManager.updateEpic(epic);

        List<Integer> expectedSubTaskIds = new ArrayList<>();
        assertEquals(epic.getSubTaskIds(), expectedSubTaskIds);
    }

    // Проверки работы с отсортированным списком задач
    @Test
    void shouldAddTaskWithTimeToPrioritizedTasks() throws TaskTimeIntersectionException {
        Task task = getDefaultTask();
        taskManager.createTask(task);

        SubTask subTask = getDefaultSubTask(taskManager.createEpic(getDefaultEpic()));
        taskManager.createSubTask(subTask);

        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertTrue(prioritizedTasks.contains(task), "Задача со временем не добавилась в отсортированный список.");
        assertTrue(prioritizedTasks.contains(subTask), "Подзадача со временем не добавилась в отсортированный список.");
    }
    @Test
    void shouldSortTasksByTimeInPrioritizedTasks() throws TaskTimeIntersectionException {
        Task taskWithEarlierTime = getDefaultTask(START_TIME);
        taskManager.createTask(taskWithEarlierTime);

        Task taskWithWithEvenLaterTime = getDefaultTask(NEW_START_TIME.plus(DURATION).plusSeconds(5));
        taskManager.createTask(taskWithWithEvenLaterTime);

        SubTask subTaskWithLaterTime = getDefaultSubTask(taskManager.createEpic(getDefaultEpic()), NEW_START_TIME);
        taskManager.createSubTask(subTaskWithLaterTime);

        TreeSet<Task> prioritizedTasks = (TreeSet<Task>) taskManager.getPrioritizedTasks();

        assertEquals(prioritizedTasks.first(), taskWithEarlierTime, "Нарушен порядок сортировки задач.");
        assertEquals(prioritizedTasks.last(), taskWithWithEvenLaterTime, "Нарушен порядок сортировки задач.");
    }

    @Test
    void shouldNotAddTaskWithoutTimeToPrioritizedTasks() throws TaskTimeIntersectionException {
        Task task = getDefaultTask();
        task.setStartTime(null);
        taskManager.createTask(task);

        SubTask subTask = getDefaultSubTask(taskManager.createEpic(getDefaultEpic()));
        subTask.setStartTime(null);
        taskManager.createSubTask(subTask);

        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertFalse(prioritizedTasks.contains(task), "Задача без времени добавилась в отсортированный список.");
        assertFalse(prioritizedTasks.contains(subTask), "Подзадача без времени добавилась в отсортированный список.");
    }

    @Test
    void shouldRemoveTaskFromPrioritizedTasksIfTimeIsRemoved() throws TaskNotFoundException, TaskTimeIntersectionException {
        Task task = getDefaultTask();
        taskManager.createTask(task);

        SubTask subTask = getDefaultSubTask(taskManager.createEpic(getDefaultEpic()));
        taskManager.createSubTask(subTask);

        task.setStartTime(null);
        taskManager.updateTask(task);
        subTask.setStartTime(null);
        taskManager.updateSubTask(subTask);

        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertFalse(
                prioritizedTasks.contains(task),
                "Задача со временем не удалилась из отсортированного списка при удалении времени."
        );
        assertFalse(
                prioritizedTasks.contains(subTask),
                "Подзадача со временем не удалилась из отсортированного списка при удалении времени."
        );
    }

    // Проверки пересечения интервалов времени
    @Test
    void shouldThrowWhenCreatingIntersectingTasks() throws TaskTimeIntersectionException {
        taskManager.createTask(getDefaultTask(START_TIME));
        assertThrows(TaskTimeIntersectionException.class, () -> taskManager.createTask(getDefaultTask(START_TIME)));
    }

    @Test
    void shouldNotThrowWhenCreatingNonIntersectingTasks() throws TaskTimeIntersectionException {
        taskManager.createTask(getDefaultTask(START_TIME));
        assertDoesNotThrow(() -> taskManager.createTask(getDefaultTask(NEW_START_TIME)));
    }
}