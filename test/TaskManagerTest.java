import exceptions.TaskTimeIntersectionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;


abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected final String title = "testTitle";
    protected final String description = "testDescription";
    protected final TaskStatus status = TaskStatus.NEW;
    protected final Duration duration = Duration.ofMinutes(2);
    protected final LocalDateTime startTime = LocalDateTime.now();

    protected final String newTitle = "new" + title;
    protected final String newDescription = "new" + description;
    protected final TaskStatus newStatus = TaskStatus.IN_PROGRESS;
    protected final LocalDateTime newStartTime = startTime.plus(duration).plusSeconds(5);

    abstract T getManager();

    @BeforeEach
    void setup() {
        taskManager = getManager();
    }

    protected Task getDefaultTask() {
        return new Task(title, description, status, duration, startTime);
    }

    protected Task getDefaultTask(LocalDateTime startTime) {
        return new Task(title, description, status, duration, startTime);
    }

    protected Epic getDefaultEpic() {
        return new Epic(title, description, status, duration, startTime);
    }

    protected SubTask getDefaultSubTask(int epicId) {
        return new SubTask(title, description, status, epicId, duration, newStartTime);
    }

    protected SubTask getDefaultSubTask(int epicId, LocalDateTime subTaskStartTime) {
        return new SubTask(title, description, status, epicId, duration, subTaskStartTime);
    }

    // Проверки создания задач
    @Test
    void shouldCreateTask() {
        Task task = getDefaultTask();
        int taskId = taskManager.createTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }


    @Test
    void shouldCreateEpic() {
        Epic epic = getDefaultEpic();
        int taskId = taskManager.createEpic(epic);

        final Epic savedTask = taskManager.getEpicById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(epic, savedTask, "Задачи не совпадают.");
    }


    @Test
    void shouldCreateSubTask() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        SubTask subTask = getDefaultSubTask(epicId);
        int taskId = taskManager.createSubTask(subTask);

        final SubTask savedTask = taskManager.getSubTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(subTask, savedTask, "Задачи не совпадают.");
    }

    // Проверки получения задач
    @Test
    void shouldGetAllTasks() {
        int taskId1 = taskManager.createTask(getDefaultTask());
        int taskId2 = taskManager.createTask(getDefaultTask(newStartTime));

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
    void shouldGetAllSubTasks() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int epicId2 = taskManager.createEpic(getDefaultEpic());
        int subTaskId1 = taskManager.createSubTask(getDefaultSubTask(epicId, startTime));
        int subTaskId2 = taskManager.createSubTask(getDefaultSubTask(epicId2, startTime.plus(duration).plusDays(5)));

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
    void shouldGetAllEpics() {
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
    void shouldGetAllSubTasksByEpicId() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int subTaskId1 = taskManager.createSubTask(getDefaultSubTask(epicId, startTime));
        int subTaskId2 = taskManager.createSubTask(getDefaultSubTask(epicId, startTime.plus(duration).plusDays(5)));

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
    void shouldUpdateTaskFields() {
        int taskId = taskManager.createTask(getDefaultTask());

        Task task = new Task(taskId, newTitle, newDescription, newStatus);
        taskManager.updateTask(task);

        Task actualTask = taskManager.getTaskById(taskId);

        assertNotEquals(title, actualTask.getTitle(), "Задача не обновилась.");
        assertNotEquals(description, actualTask.getDescription(), "Задача не обновилась.");
        assertNotEquals(status, actualTask.getTaskStatus(), "Задача не обновилась.");
        assertNotEquals(duration, actualTask.getDuration(), "Задача не обновилась.");
        assertNotEquals(startTime, actualTask.getStartTime(), "Задача не обновилась.");
    }

    @Test
    void shouldUpdateSubTaskFields() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int taskId = taskManager.createSubTask(getDefaultSubTask(epicId));

        SubTask task = new SubTask(taskId, newTitle, newDescription, newStatus, epicId);
        taskManager.updateSubTask(task);

        SubTask actualTask = taskManager.getSubTaskById(taskId);

        assertNotEquals(title, actualTask.getTitle(), "Задача не обновилась.");
        assertNotEquals(description, actualTask.getDescription(), "Задача не обновилась.");
        assertNotEquals(status, actualTask.getTaskStatus(), "Задача не обновилась.");
        assertNotEquals(duration, actualTask.getDuration(), "Задача не обновилась.");
        assertNotEquals(newStartTime, actualTask.getStartTime(), "Задача не обновилась.");
    }

    @Test
    void shouldUpdateEpicFields() {
        int epicId = taskManager.createEpic(getDefaultEpic());

        Epic task = new Epic(epicId, newTitle, newDescription, status);
        taskManager.updateEpic(task);
        ArrayList<Integer> oldSubTaskIds = new ArrayList<>(task.getSubTaskIds());

        TaskStatus subTaskStatus = TaskStatus.IN_PROGRESS;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        Epic actualTask = taskManager.getEpicById(epicId);

        assertNotEquals(title, actualTask.getTitle(), "Задача не обновилась.");
        assertNotEquals(description, actualTask.getDescription(), "Задача не обновилась.");
        assertNotEquals(status, actualTask.getTaskStatus(), "Задача не обновилась.");
        assertNotEquals(duration, actualTask.getDuration(), "Задача не обновилась.");
        assertNotEquals(startTime, actualTask.getStartTime(), "Задача не обновилась.");
        assertNotEquals(oldSubTaskIds, actualTask.getSubTaskIds(), "Задача не обновилась.");
    }

    // Проверки удаления задач
    @Test
    void shouldDeleteTask() {
        int taskId = taskManager.createTask(getDefaultTask());
        taskManager.deleteTaskById(taskId);

        Task task = taskManager.getTaskById(taskId);
        assertNull(task, "Задача не удалилась.");
    }

    @Test
    void shouldDeleteSubTask() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int taskId = taskManager.createSubTask(getDefaultSubTask(epicId));
        taskManager.deleteSubTaskById(taskId);

        SubTask subTask = taskManager.getSubTaskById(taskId);
        assertNull(subTask, "Задача не удалилась.");
    }

    @Test
    void shouldDeleteEpic() {
        int taskId = taskManager.createEpic(getDefaultEpic());

        taskManager.deleteEpicById(taskId);

        Epic epic = taskManager.getEpicById(taskId);
        assertNull(epic, "Задача не удалилась.");
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask(getDefaultTask());
        taskManager.createTask(getDefaultTask(newStartTime));

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteAllSubTasks() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        taskManager.createSubTask(getDefaultSubTask(epicId, startTime));
        taskManager.createSubTask(getDefaultSubTask(epicId, startTime.plus(duration).plusDays(5)));

        taskManager.deleteAllSubTasks();

        assertTrue(taskManager.getSubTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteAllEpicsAndSubTasksWhenDeletingAllEpics() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int epicId2 = taskManager.createEpic(getDefaultEpic());
        taskManager.createSubTask(getDefaultSubTask(epicId, startTime));
        taskManager.createSubTask(getDefaultSubTask(epicId2, startTime.plus(duration).plusDays(5)));

        taskManager.deleteAllEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Задачи не удалились.");
        assertTrue(taskManager.getSubTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteEpicSubTasksWhenDeletingEpic() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int subTaskId = taskManager.createSubTask(getDefaultSubTask(epicId));

        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getSubTaskById(subTaskId));
    }

    // Проверки, что поля не изменяются в процессе создания
    @Test
    void shouldKeepGivenFieldsWhenCreatingTask() {
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
    void shouldKeepGivenFieldsWhenCreatingSubTask() {
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
    void shouldKeepGivenFieldsWhenCreatingEpic() {
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
    void epicShouldBeNewWithOneNewSubTask() {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask(title, description, TaskStatus.NEW, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeNewWithTwoNewSubTasks() {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.NEW;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        SubTask subTask2 = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask2);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeInProgressWithOneInProgressSubTask() {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask(title, description, TaskStatus.IN_PROGRESS, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeInProgressWithTwoInProgressSubTasks() {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.IN_PROGRESS;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        SubTask subTask2 = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask2);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeDoneWithOneDoneSubTask() {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask(title, description, TaskStatus.DONE, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeDoneWithTwoDoneSubTasks() {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.DONE;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        SubTask subTask2 = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask2);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeInProgressWithOneNewAndOneDoneSubTasks() {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask(title, description, TaskStatus.NEW, epicId);
        taskManager.createSubTask(subTask);

        SubTask subTask2 = new SubTask(title, description, TaskStatus.DONE, epicId);
        taskManager.createSubTask(subTask2);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeNewWhenDeletingLastSubTask() {
        Epic epic = getDefaultEpic();
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.DONE;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        int subTaskId = taskManager.createSubTask(subTask);

        taskManager.deleteSubTaskById(subTaskId);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, actualEpic.getTaskStatus());
    }

    // Проверки работы с невалидными данными при создании задач
    @Test
    void shouldIgnoreStatusWhenCreatingEpic() {
        TaskStatus wrongStatus = TaskStatus.DONE;
        Epic task = new Epic(title, description, wrongStatus);
        int taskId = taskManager.createEpic(task);
        Epic actualTask = taskManager.getEpicById(taskId);

        assertNotEquals(wrongStatus, actualTask.getTaskStatus(), "Эпик создался с неверным статусом");
    }

    @Test
    void shouldIgnoreGivenIdWhenCreatingTask() {
        Task task = getDefaultTask();
        int taskId = 9999999;
        task.setTaskId(taskId);
        int actualTaskId = taskManager.createTask(task);
        assertNotEquals(taskId, actualTaskId);
    }

    @Test
    void shouldIgnoreGivenIdWhenCreatingSubTask() {
        int taskId = 9999999;
        int epicId = taskManager.createEpic(getDefaultEpic());
        SubTask task = new SubTask(taskId, title, description, status, epicId);
        int actualTaskId = taskManager.createSubTask(task);
        assertNotEquals(taskId, actualTaskId);
    }

    @Test
    void shouldIgnoreGivenIdWhenCreatingEpic() {
        int taskId = 9999999;
        Epic task = new Epic(taskId, title, description, status);
        int actualTaskId = taskManager.createEpic(task);
        assertNotEquals(taskId, actualTaskId);
    }

    @Test
    void shouldNotCreateSubTaskWithoutValidEpicId() {
        int subTaskId = taskManager.createSubTask(getDefaultSubTask(999));
        assertEquals(-1, subTaskId, "Отсутствует валидный связанный эпик.");
    }

    @Test
    void shouldNotUpdateSubTaskToInValidEpicId() {
        int epicId = taskManager.createEpic(getDefaultEpic());
        int subTaskId = taskManager.createSubTask(getDefaultSubTask(epicId));
        int wrongEpicId = 999;
        taskManager.updateSubTask(new SubTask(subTaskId, title, description, status, wrongEpicId));
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
    void shouldIgnoreGivenNonExistentSubTaskIdsWhenUpdatingEpic() {
        Epic epic = getDefaultEpic();
        taskManager.createEpic(epic);

        epic.addSubTaskId(epic.getTaskId() - 1);
        taskManager.updateEpic(epic);

        List<Integer> expectedSubTaskIds = new ArrayList<>();
        assertEquals(epic.getSubTaskIds(), expectedSubTaskIds);
    }

    // Проверки работы с отсортированным списком задач
    @Test
    void shouldAddTaskWithTimeToPrioritizedTasks() {
        Task task = getDefaultTask();
        taskManager.createTask(task);

        SubTask subTask = getDefaultSubTask(taskManager.createEpic(getDefaultEpic()));
        taskManager.createSubTask(subTask);

        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertTrue(prioritizedTasks.contains(task), "Задача со временем не добавилась в отсортированный список.");
        assertTrue(prioritizedTasks.contains(subTask), "Подзадача со временем не добавилась в отсортированный список.");
    }
    @Test
    void shouldSortTasksByTimeInPrioritizedTasks() {
        Task taskWithEarlierTime = getDefaultTask(startTime);
        taskManager.createTask(taskWithEarlierTime);

        Task taskWithWithEvenLaterTime = getDefaultTask(newStartTime.plus(duration).plusSeconds(5));
        taskManager.createTask(taskWithWithEvenLaterTime);

        SubTask subTaskWithLaterTime = getDefaultSubTask(taskManager.createEpic(getDefaultEpic()), newStartTime);
        taskManager.createSubTask(subTaskWithLaterTime);

        TreeSet<Task> prioritizedTasks = (TreeSet<Task>) taskManager.getPrioritizedTasks();

        assertEquals(prioritizedTasks.first(), taskWithEarlierTime, "Нарушен порядок сортировки задач.");
        assertEquals(prioritizedTasks.last(), taskWithWithEvenLaterTime, "Нарушен порядок сортировки задач.");
    }

    @Test
    void shouldNotAddTaskWithoutTimeToPrioritizedTasks() {
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
    void shouldRemoveTaskFromPrioritizedTasksIfTimeIsRemoved() {
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
    void shouldThrowWhenCreatingIntersectingTasks() {
        taskManager.createTask(getDefaultTask(startTime));
        assertThrows(TaskTimeIntersectionException.class, () -> taskManager.createTask(getDefaultTask(startTime)));
    }

    @Test
    void shouldNotThrowWhenCreatingNonIntersectingTasks() {
        taskManager.createTask(getDefaultTask(startTime));
        assertDoesNotThrow(() -> taskManager.createTask(getDefaultTask(newStartTime)));
    }
}