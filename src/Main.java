import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

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

    private static void printTasksWithHistory(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getTaskId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
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
//        status = TaskStatus.NEW;
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

        for (Task task : taskManager.getTasks()) {
            task.setTaskStatus(status);
            taskManager.updateTask(task);

            TaskStatus taskStatus = taskManager.getTaskById(task.getTaskId()).getTaskStatus();
            assert taskStatus == status;
        }

        for (Epic epic : taskManager.getEpics()) {
            TaskStatus originalStatus = epic.getTaskStatus();  // Для теста ниже, что статус не изменился.

            epic.setTaskStatus(status);
            taskManager.updateEpic(epic);

            TaskStatus epicStatus = taskManager.getEpicById(epic.getTaskId()).getTaskStatus();
            assert epicStatus == originalStatus;
        }

        for (SubTask subTask : taskManager.getSubTasks()) {
            subTask.setTaskStatus(status);
            taskManager.updateSubTask(subTask);

            TaskStatus taskStatus = taskManager.getSubTaskById(subTask.getTaskId()).getTaskStatus();
            assert taskStatus == status;
        }

    }

    public static void testDeletingTask(TaskManager taskManager) {
        if (taskManager.getTasks().isEmpty()) return;
        int taskId = taskManager.getTasks().getFirst().getTaskId();
        taskManager.deleteTaskById(taskId);

        Task task = taskManager.getTaskById(taskId);
        assert task == null;
    }

    public static void testDeletingEpic(TaskManager taskManager) {
        if (taskManager.getEpics().isEmpty()) return;
        int taskId = taskManager.getEpics().getFirst().getTaskId();
        Epic epic = taskManager.getEpicById(taskId);
        List<Integer> epicSubTaskIds = epic.getSubTaskIds();

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
        if (taskManager.getSubTasks().isEmpty()) return;
        int taskId = taskManager.getSubTasks().getFirst().getTaskId();
        taskManager.deleteSubTaskById(taskId);

        Task task = taskManager.getSubTaskById(taskId);
        assert task == null;
    }

}
