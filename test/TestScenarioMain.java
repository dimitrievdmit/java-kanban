import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import schemas.enums.TaskStatus;
import schemas.tasks.Epic;
import schemas.tasks.SubTask;
import schemas.tasks.Task;
import services.Managers;
import services.TaskManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestScenarioMain {

    public static void main(String[] args) throws TaskNotFoundException, TaskTimeIntersectionException {
        TaskManager taskManager = Managers.getDefault();

        historyManagerUserScenario(taskManager);

        System.out.println();
        System.out.println("===========================");
        System.out.println("Создание задач:");
        testCreatingTasks(taskManager);
        printTasksWithHistory(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на Выполнено:");
        testChangingTaskStatuses(taskManager, TaskStatus.DONE);
        printTasksWithHistory(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на В работе:");
        testChangingTaskStatuses(taskManager, TaskStatus.IN_PROGRESS);
        printTasksWithHistory(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на Новая:");
        testChangingTaskStatuses(taskManager, TaskStatus.NEW);
        printTasksWithHistory(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить обычную задачу:");
        testDeletingTask(taskManager);
        printTasksWithHistory(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить Эпик:");
        testDeletingEpic(taskManager);
        printTasksWithHistory(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить подзадачу:");
        testDeletingSubTask(taskManager);
        printTasksWithHistory(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить все типы задач:");
        taskManager.deleteAllTasksAllTypes();
        printTasksWithHistory(taskManager);
        System.out.println("===========================");

    }


    public static void historyManagerUserScenario(TaskManager taskManager) throws TaskNotFoundException, TaskTimeIntersectionException {

        // Создаём две задачи, эпик с тремя подзадачами и эпик без подзадач.
        List<Integer> taskIds = create2TasksAndEpicWith3Subtasks(taskManager);
        int taskId1 = taskIds.get(0);
        int taskId2 = taskIds.get(1);
        int epicId = taskIds.get(2);
        int subTaskId1 = taskIds.get(3);
        int subTaskId2 = taskIds.get(4);
        int subTaskId3 = taskIds.get(5);
        int epicIdWithNoSubTasks = taskIds.get(6);

//        Запрашиваем созданные задачи в разном порядке
        taskManager.getTaskById(taskId1);
        taskManager.getTaskById(taskId2);

        taskManager.getTaskById(epicId);
        taskManager.getTaskById(subTaskId2);

        taskManager.getTaskById(epicIdWithNoSubTasks);

        taskManager.getTaskById(subTaskId1);
        taskManager.getTaskById(subTaskId2);
        taskManager.getTaskById(subTaskId3);
        taskManager.getTaskById(subTaskId1);

        // Проверяем, что в истории нет повторов.
        List<Task> actualHistoryList = taskManager.getHistory();
        Set<Task> actualHistorySet = new HashSet<>(actualHistoryList);
        assert actualHistoryList.size() == actualHistorySet.size();

        // Проверяем, что удалённая задача пропадает из истории тоже.
        taskManager.deleteTaskById(taskId1);
        taskManager.getHistory().forEach(iTask -> {
            assert iTask.getTaskId() != taskId1;
        });

        // Проверяем, что с удаленным эпиком из истории пропадают и подзадачи.
        taskManager.deleteEpicById(epicId);
        taskManager.getHistory().forEach(iTask -> {
            assert iTask.getTaskId() != epicId;
            assert iTask.getTaskId() != subTaskId1;
            assert iTask.getTaskId() != subTaskId2;
            assert iTask.getTaskId() != subTaskId3;
        });

    }

    private static void printTasksWithHistory(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getTasks().forEach(System.out::println);
        System.out.println("Эпики:");
        manager.getEpics().forEach(epic -> {
            System.out.println(epic);
            try {
                manager.getEpicSubtasks(epic.getTaskId()).forEach(task -> System.out.println("--> " + task));
            } catch (TaskNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Подзадачи:");
        manager.getSubTasks().forEach(System.out::println);

        System.out.println("История:");
        manager.getHistory().forEach(System.out::println);
    }

    public static void testCreatingTasks(TaskManager taskManager) throws TaskNotFoundException, TaskTimeIntersectionException {

        String title = "Задача 1";
        String description = "Протестировать создание обычных задач в статусе Новая.";
        TaskStatus status = TaskStatus.NEW;
        Task task = new Task(title, description, status);
        int taskId = taskManager.createTask(task);
        assert taskManager.getTaskById(taskId).equals(task);

        title = "Задача 2";
        description = "Протестировать создание обычных задач в статусе В работе.";
        status = TaskStatus.IN_PROGRESS;
        task = new Task(title, description, status);
        taskId = taskManager.createTask(task);
        assert taskManager.getTaskById(taskId).equals(task);

        title = "Эпик 1";
        description = "Протестировать создание эпиков.";
        status = TaskStatus.NEW;
        Epic epic = new Epic(title, description, status);
        int epicId = taskManager.createEpic(epic);
        assert taskManager.getEpicById(epicId).equals(epic);

        title = "Подзадача 1";
        description = "Протестировать создание подзадач в статусе Новая.";
//        status = schemas.enums.TaskStatus.NEW;
        SubTask subTask = new SubTask(title, description, status, epicId);
        taskId = taskManager.createSubTask(subTask);
        assert taskManager.getSubTaskById(taskId).equals(subTask);
        TaskStatus epicStatus = taskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.NEW;

        title = "Подзадача 2";
        description = "Протестировать создание подзадач в статусе В работе.";
        status = TaskStatus.IN_PROGRESS;
        subTask = new SubTask(title, description, status, epicId);
        taskId = taskManager.createSubTask(subTask);
        assert taskManager.getSubTaskById(taskId).equals(subTask);
        epicStatus = taskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.IN_PROGRESS;

        title = "Эпик 2";
        description = "Протестировать создание эпиков.";
        status = TaskStatus.DONE;
        epic = new Epic(title, description, status);
        taskManager.createEpic(epic);

        epicStatus = taskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.NEW;

        title = "Подзадача 3";
        description = "Протестировать создание подзадач в статусе В работе.";
        status = TaskStatus.IN_PROGRESS;
        subTask = new SubTask(title, description, status, epicId);
        taskId = taskManager.createSubTask(subTask);
        assert taskManager.getSubTaskById(taskId).equals(subTask);

        epicStatus = taskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.IN_PROGRESS;

    }

    public static void testChangingTaskStatuses(TaskManager taskManager, TaskStatus status) {
        taskManager.getTasks().forEach(task -> {
            task.setTaskStatus(status);
            try {
                taskManager.updateTask(task);
            } catch (TaskNotFoundException | TaskTimeIntersectionException e) {
                throw new RuntimeException(e);
            }

            TaskStatus taskStatus;
            try {
                taskStatus = taskManager.getTaskById(task.getTaskId()).getTaskStatus();
            } catch (TaskNotFoundException e) {
                throw new RuntimeException(e);
            }
            assert taskStatus == status;
        });

        taskManager.getEpics().forEach(epic -> {
            TaskStatus originalStatus = epic.getTaskStatus();  // Для теста ниже, что статус не изменился.

            epic.setTaskStatus(status);
            try {
                taskManager.updateEpic(epic);
            } catch (TaskNotFoundException e) {
                throw new RuntimeException(e);
            }

            TaskStatus epicStatus;
            try {
                epicStatus = taskManager.getEpicById(epic.getTaskId()).getTaskStatus();
            } catch (TaskNotFoundException e) {
                throw new RuntimeException(e);
            }
            assert epicStatus == originalStatus;
        });

        taskManager.getSubTasks().forEach(subTask -> {
            subTask.setTaskStatus(status);
            try {
                taskManager.updateSubTask(subTask);
            } catch (TaskNotFoundException | TaskTimeIntersectionException e) {
                throw new RuntimeException(e);
            }

            TaskStatus taskStatus;
            try {
                taskStatus = taskManager.getSubTaskById(subTask.getTaskId()).getTaskStatus();
            } catch (TaskNotFoundException e) {
                throw new RuntimeException(e);
            }
            assert taskStatus == status;
        });
    }

    public static void testDeletingTask(TaskManager taskManager) throws TaskNotFoundException {
        if (taskManager.getTasks().isEmpty()) return;
        int taskId = taskManager.getTasks().getFirst().getTaskId();
        taskManager.deleteTaskById(taskId);

        Task task = taskManager.getTaskById(taskId);
        assert task == null;
    }

    public static void testDeletingEpic(TaskManager taskManager) throws TaskNotFoundException {
        if (taskManager.getEpics().isEmpty()) return;
        int taskId = taskManager.getEpics().getFirst().getTaskId();
        Epic epic = taskManager.getEpicById(taskId);
        List<Integer> epicSubTaskIds = epic.getSubTaskIds();

        taskManager.deleteEpicById(taskId);

        Task task = taskManager.getEpicById(taskId);
        assert task == null;

        // Проверить, что подзадачи отвязались от эпика и стали задачами
        epicSubTaskIds.forEach(subTaskId -> {
            try {
                taskManager.getTaskById(subTaskId);
            } catch (TaskNotFoundException e) {
                throw new AssertionError(e);
            }
        });
    }

    public static void testDeletingSubTask(TaskManager taskManager) throws TaskNotFoundException {
        if (taskManager.getSubTasks().isEmpty()) return;
        int taskId = taskManager.getSubTasks().getFirst().getTaskId();
        taskManager.deleteSubTaskById(taskId);

        Task task = taskManager.getSubTaskById(taskId);
        assert task == null;
    }


    public static List<Integer> create2TasksAndEpicWith3Subtasks(TaskManager taskManager) throws TaskNotFoundException, TaskTimeIntersectionException {

        List<Integer> taskIds = new ArrayList<>();

        String title = "Задача 1";
        String description = "Протестировать создание обычных задач в статусе Новая.";
        TaskStatus status = TaskStatus.NEW;
        Task task = new Task(title, description, status);
        int taskId = taskManager.createTask(task);
        assert taskManager.getTaskById(taskId).equals(task);
        taskIds.add(taskId);

        title = "Задача 2";
        description = "Протестировать создание обычных задач в статусе В работе.";
        status = TaskStatus.IN_PROGRESS;
        task = new Task(title, description, status);
        taskId = taskManager.createTask(task);
        assert taskManager.getTaskById(taskId).equals(task);
        taskIds.add(taskId);

        title = "Эпик 1";
        description = "Протестировать создание эпиков.";
        status = TaskStatus.NEW;
        Epic epic = new Epic(title, description, status);
        int epicId = taskManager.createEpic(epic);
        assert taskManager.getEpicById(epicId).equals(epic);
        taskIds.add(epicId);

        title = "Подзадача 1";
        description = "Протестировать создание подзадач в статусе Новая.";
        SubTask subTask = new SubTask(title, description, status, epicId);
        taskId = taskManager.createSubTask(subTask);
        assert taskManager.getSubTaskById(taskId).equals(subTask);
        TaskStatus epicStatus = taskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.NEW;
        taskIds.add(taskId);

        title = "Подзадача 2";
        description = "Протестировать создание подзадач в статусе В работе.";
        status = TaskStatus.IN_PROGRESS;
        subTask = new SubTask(title, description, status, epicId);
        taskId = taskManager.createSubTask(subTask);
        assert taskManager.getSubTaskById(taskId).equals(subTask);
        epicStatus = taskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.IN_PROGRESS;
        taskIds.add(taskId);

        title = "Подзадача 3";
        description = "Протестировать создание подзадач в статусе В работе.";
        subTask = new SubTask(title, description, status, epicId);
        taskId = taskManager.createSubTask(subTask);
        assert taskManager.getSubTaskById(taskId).equals(subTask);
        epicStatus = taskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.IN_PROGRESS;
        taskIds.add(taskId);

        title = "Эпик 1";
        description = "Протестировать создание эпиков.";
        status = TaskStatus.NEW;
        Epic epic2 = new Epic(title, description, status);
        int epicId2 = taskManager.createEpic(epic2);
        assert taskManager.getEpicById(epicId2).equals(epic);
        taskIds.add(epicId2);

        return taskIds;
    }


}
