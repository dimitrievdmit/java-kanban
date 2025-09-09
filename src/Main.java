import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        System.out.println();
        System.out.println("===========================");
        System.out.println("Создание задач:");
        testCreatingTasks(taskManager);
        printTasks(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на Выполнено:");
        testChangingTaskStatuses(taskManager, TaskStatus.DONE);
        printTasks(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на В работе:");
        testChangingTaskStatuses(taskManager, TaskStatus.IN_PROGRESS);
        printTasks(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на Новая:");
        testChangingTaskStatuses(taskManager, TaskStatus.NEW);
        printTasks(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить обычную задачу:");
        testDeletingTask(taskManager);
        printTasks(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить Эпик:");
        testDeletingEpic(taskManager);
        printTasks(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить подзадачу:");
        testDeletingSubTask(taskManager);
        printTasks(taskManager);
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить все типы задач:");
        taskManager.deleteAllTasksAllTypes();
        printTasks(taskManager);
        System.out.println("===========================");

    }

    public static void testCreatingTasks(TaskManager taskManager) {

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
        status = TaskStatus.NEW;
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

    public static void printTasks(TaskManager taskManager) {
        System.out.println();
        System.out.println("Список задач:");
        System.out.println(taskManager.getTaskArray());

        System.out.println();
        System.out.println("Список эпиков:");
        System.out.println(taskManager.getEpicArray());

        System.out.println();
        System.out.println("Список подзадач:");
        System.out.println(taskManager.getSubTaskArray());
    }

    public static void testChangingTaskStatuses(TaskManager taskManager, TaskStatus status) {

        for (Task task : taskManager.getTaskArray()) {
            task.setTaskStatus(status);
            taskManager.updateTask(task);

            TaskStatus taskStatus = taskManager.getTaskById(task.getTaskId()).getTaskStatus();
            assert taskStatus == status;
        }

        for (Epic epic : taskManager.getEpicArray()) {
            TaskStatus originalStatus = epic.getTaskStatus();  // Для теста ниже, что статус не изменился.

            epic.setTaskStatus(status);
            taskManager.updateEpic(epic);

            TaskStatus epicStatus = taskManager.getEpicById(epic.getTaskId()).getTaskStatus();
            assert epicStatus == originalStatus;
        }

        for (SubTask subTask : taskManager.getSubTaskArray()) {
            subTask.setTaskStatus(status);
            taskManager.updateSubTask(subTask);

            TaskStatus taskStatus = taskManager.getSubTaskById(subTask.getTaskId()).getTaskStatus();
            assert taskStatus == status;
        }

    }

    public static void testDeletingTask(TaskManager taskManager) {
        if (taskManager.getTaskArray().isEmpty()) return;
        int taskId = taskManager.getTaskArray().iterator().next().getTaskId();
        taskManager.deleteTaskById(taskId);

        Task task = taskManager.getTaskById(taskId);
        assert task == null;
    }

    public static void testDeletingEpic(TaskManager taskManager) {
        if (taskManager.getEpicArray().isEmpty()) return;
        int taskId = taskManager.getEpicArray().iterator().next().getTaskId();
        Epic epic = taskManager.getEpicById(taskId);
        ArrayList<Integer> epicSubTaskIds = epic.getSubTaskIds();

        taskManager.deleteEpicById(taskId);

        Task task = taskManager.getEpicById(taskId);
        assert task == null;

        // Проверить, что подзадачи отвязались от эпика и стали задачами
        if (epicSubTaskIds.isEmpty()) return;
        for (Integer subTaskId : epicSubTaskIds) {
            assert taskManager.getTaskById(subTaskId) != null;
        }
    }

    public static void testDeletingSubTask(TaskManager taskManager) {
        if (taskManager.getSubTaskArray().isEmpty()) return;
        int taskId = taskManager.getSubTaskArray().iterator().next().getTaskId();
        taskManager.deleteSubTaskById(taskId);

        Task task = taskManager.getSubTaskById(taskId);
        assert task == null;
    }

}
