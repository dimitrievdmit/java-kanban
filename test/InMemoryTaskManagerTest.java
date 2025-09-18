import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class InMemoryTaskManagerTest {
    private TaskManager taskManager;
    private final String title = "testTitle";
    private final String description = "testDescription";
    private final TaskStatus status = TaskStatus.NEW;

    private final String newTitle = "new" + title;
    private final String newDescription = "new" + description;
    private final TaskStatus newStatus = TaskStatus.IN_PROGRESS;

    @BeforeEach
    void setup() {
        taskManager = Managers.getDefault();
    }


    @Test
    void shouldCreateTask() {
        Task task = new Task(title, description, status);
        int taskId = taskManager.createTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }


    @Test
    void shouldCreateEpic() {
        Epic epic = new Epic(title, description, status);
        int taskId = taskManager.createEpic(epic);

        final Epic savedTask = taskManager.getEpicById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(epic, savedTask, "Задачи не совпадают.");
    }


    @Test
    void shouldCreateSubTask() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        SubTask subTask = new SubTask(title, description, status, epicId);
        int taskId = taskManager.createSubTask(subTask);

        final SubTask savedTask = taskManager.getSubTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(subTask, savedTask, "Задачи не совпадают.");
    }

    @Test
    void shouldGetAllTasks() {
        int taskId1 = taskManager.createTask(new Task(title, description, status));
        int taskId2 = taskManager.createTask(new Task(title, description, status));

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
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int epicId2 = taskManager.createEpic(new Epic(title, description, status));
        int subTaskId1 = taskManager.createSubTask(new SubTask(title, description, status, epicId));
        int subTaskId2 = taskManager.createSubTask(new SubTask(title, description, status, epicId2));

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
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int epicId2 = taskManager.createEpic(new Epic(title, description, status));

        Epic task1 = taskManager.getEpicById(epicId);
        Epic task2 = taskManager.getEpicById(epicId2);

        ArrayList<Epic> expectedTasks = new ArrayList<>(2);
        expectedTasks.add(task1);
        expectedTasks.add(task2);

        List<Epic> actualTasks = taskManager.getEpics();

        assertNotNull(actualTasks, "Задачи не возвращаются.");
        assertEquals(2, actualTasks.size(), "Неверное количество задач.");
        // Был удивлён, что ArrayList не является массивом и assertArrayEquals не подходит
        assertEquals(expectedTasks, actualTasks, "Задачи не совпадают.");
    }

    @Test
    void shouldGetAllSubTasksByEpicId() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int subTaskId1 = taskManager.createSubTask(new SubTask(title, description, status, epicId));
        int subTaskId2 = taskManager.createSubTask(new SubTask(title, description, status, epicId));

        int epicId2 = taskManager.createEpic(new Epic(title, description, status));
        taskManager.createSubTask(new SubTask(title, description, status, epicId2));

        SubTask task1 = taskManager.getSubTaskById(subTaskId1);
        SubTask task2 = taskManager.getSubTaskById(subTaskId2);

        ArrayList<SubTask> expectedTasks = new ArrayList<>(2);
        expectedTasks.add(task1);
        expectedTasks.add(task2);

        List<SubTask> actualTasks = taskManager.getEpicSubtasks(epicId);

        assertNotNull(actualTasks, "Задачи не возвращаются.");
        assertEquals(2, actualTasks.size(), "Неверное количество задач.");
        // Был удивлён, что ArrayList не является массивом и assertArrayEquals не подходит
        assertEquals(expectedTasks, actualTasks, "Задачи не совпадают.");
    }

    @Test
    void shouldUpdateTaskFields() {
        int taskId = taskManager.createTask(new Task(title, description, status));

        Task task = new Task(taskId, newTitle, newDescription, newStatus);
        taskManager.updateTask(task);

        Task actualTask = taskManager.getTaskById(taskId);

        assertNotEquals(title, actualTask.title, "Задача не обновилась.");
        assertNotEquals(description, actualTask.description, "Задача не обновилась.");
        assertNotEquals(status, actualTask.taskStatus, "Задача не обновилась.");
    }

    @Test
    void shouldUpdateSubTaskFields() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int taskId = taskManager.createSubTask(new SubTask(title, description, status, epicId));

        SubTask task = new SubTask(taskId, newTitle, newDescription, newStatus, epicId);
        taskManager.updateSubTask(task);

        SubTask actualTask = taskManager.getSubTaskById(taskId);

        assertNotEquals(title, actualTask.title, "Задача не обновилась.");
        assertNotEquals(description, actualTask.description, "Задача не обновилась.");
        assertNotEquals(status, actualTask.taskStatus, "Задача не обновилась.");
    }

    @Test
    void shouldUpdateEpicFields() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));

        Epic task = new Epic(epicId, newTitle, newDescription, status);
        taskManager.updateEpic(task);
        ArrayList<Integer> oldSubTaskIds = new ArrayList<>(task.getSubTaskIds());

        TaskStatus subTaskStatus = TaskStatus.IN_PROGRESS;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        Epic actualTask = taskManager.getEpicById(epicId);

        assertNotEquals(title, actualTask.title, "Задача не обновилась.");
        assertNotEquals(description, actualTask.description, "Задача не обновилась.");
        assertNotEquals(status, actualTask.taskStatus, "Задача не обновилась.");
        assertNotEquals(oldSubTaskIds, actualTask.getSubTaskIds(), "Задача не обновилась.");
    }

    @Test
    void shouldDeleteTask() {
        int taskId = taskManager.createTask(new Task(title, description, status));
        taskManager.deleteTaskById(taskId);

        Task task = taskManager.getTaskById(taskId);
        assertNull(task, "Задача не удалилась.");
    }

    @Test
    void shouldDeleteSubTask() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int taskId = taskManager.createSubTask(new SubTask(title, description, status, epicId));
        taskManager.deleteSubTaskById(taskId);

        SubTask subTask = taskManager.getSubTaskById(taskId);
        assertNull(subTask, "Задача не удалилась.");
    }

    @Test
    void shouldDeleteEpic() {
        int taskId = taskManager.createEpic(new Epic(title, description, status));

        taskManager.deleteEpicById(taskId);

        Epic epic = taskManager.getEpicById(taskId);
        assertNull(epic, "Задача не удалилась.");
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask(new Task(title, description, status));
        taskManager.createTask(new Task(title, description, status));

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteAllSubTasks() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        taskManager.createSubTask(new SubTask(title, description, status, epicId));
        taskManager.createSubTask(new SubTask(title, description, status, epicId));

        taskManager.deleteAllSubTasks();

        assertTrue(taskManager.getSubTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteAllEpicsAndSubTasksWhenDeletingAllEpics() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int epicId2 = taskManager.createEpic(new Epic(title, description, status));
        taskManager.createSubTask(new SubTask(title, description, status, epicId));
        taskManager.createSubTask(new SubTask(title, description, status, epicId2));

        taskManager.deleteAllEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Задачи не удалились.");
        assertTrue(taskManager.getSubTasks().isEmpty(), "Задачи не удалились.");
    }

    @Test
    void shouldDeleteEpicSubTasksWhenDeletingEpic() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int subTaskId = taskManager.createSubTask(new SubTask(title, description, status, epicId));

        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getSubTaskById(subTaskId));
    }

    @Test
    void shouldIgnoreGivenIdWhenCreatingTask() {
        Task task = new Task(title, description, status);
        int taskId = 9999999;
        task.setTaskId(taskId);
        int actualTaskId = taskManager.createTask(task);
        assertNotEquals(taskId, actualTaskId);
    }


    @Test
    void shouldIgnoreGivenIdWhenCreatingSubTask() {
        int taskId = 9999999;
        int epicId = taskManager.createEpic(new Epic(title, description, status));
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
    void shouldKeepGivenFieldsWhenCreatingTask() {
        Task task = new Task(title, description, status);
        int taskId = taskManager.createTask(task);
        Task actualTask = taskManager.getTaskById(taskId);

        assertEquals(task.title, actualTask.title);
        assertEquals(task.description, actualTask.description);
        assertEquals(task.taskStatus, actualTask.taskStatus);
    }


    @Test
    void shouldKeepGivenFieldsWhenCreatingSubTask() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        SubTask task = new SubTask(title, description, status, epicId);
        int taskId = taskManager.createSubTask(task);
        SubTask actualTask = taskManager.getSubTaskById(taskId);

        assertEquals(task.title, actualTask.title);
        assertEquals(task.description, actualTask.description);
        assertEquals(task.taskStatus, actualTask.taskStatus);
        assertEquals(task.getEpicId(), actualTask.getEpicId());
    }


    @Test
    void shouldKeepGivenFieldsWhenCreatingEpic() {
        Epic task = new Epic(title, description, status);
        int taskId = taskManager.createEpic(task);
        Epic actualTask = taskManager.getEpicById(taskId);

        assertEquals(task.title, actualTask.title);
        assertEquals(task.description, actualTask.description);
        // Статус может отличаться от данного в связи с особыми правилами для эпиков.
    }

    @Test
    void shouldIgnoreStatusWhenCreatingEpic() {
        TaskStatus wrongStatus = TaskStatus.DONE;
        Epic task = new Epic(title, description, wrongStatus);
        int taskId = taskManager.createEpic(task);
        Epic actualTask = taskManager.getEpicById(taskId);

        assertNotEquals(wrongStatus, actualTask.getTaskStatus());
    }

    @Test
    void epicShouldBeNewWithOneNewSubTask() {
        Epic epic = new Epic(title, description, status);
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.NEW;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeNewWithTwoNewSubTasks() {
        Epic epic = new Epic(title, description, status);
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
        Epic epic = new Epic(title, description, status);
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.IN_PROGRESS;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeInProgressWithTwoInProgressSubTasks() {
        Epic epic = new Epic(title, description, status);
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
        Epic epic = new Epic(title, description, status);
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.DONE;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        taskManager.createSubTask(subTask);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, actualEpic.getTaskStatus());
    }

    @Test
    void epicShouldBeDoneWithTwoDoneSubTasks() {
        Epic epic = new Epic(title, description, status);
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
    void epicShouldBeNewWhenDeletingLastSubTask() {
        Epic epic = new Epic(title, description, status);
        int epicId = taskManager.createEpic(epic);

        TaskStatus subTaskStatus = TaskStatus.DONE;
        SubTask subTask = new SubTask(title, description, subTaskStatus, epicId);
        int subTaskId = taskManager.createSubTask(subTask);

        taskManager.deleteSubTaskById(subTaskId);

        Epic actualEpic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, actualEpic.getTaskStatus());
    }

    @Test
    void shouldNotCreateSubTaskWithoutValidEpicId() {
        int subTaskId = taskManager.createSubTask(new SubTask(title, description, status, 999));
        assertEquals(-1, subTaskId);
    }

    @Test
    void shouldNotUpdateSubTaskToInValidEpicId() {
        int epicId = taskManager.createEpic(new Epic(title, description, status));
        int subTaskId = taskManager.createSubTask(new SubTask(title, description, status, epicId));
        int wrongEpicId = 999;
        taskManager.updateSubTask(new SubTask(subTaskId, title, description, status, wrongEpicId));
        assertNotEquals(wrongEpicId, taskManager.getSubTaskById(subTaskId).getEpicId());
    }

}