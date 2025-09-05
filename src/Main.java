import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        System.out.println();
        System.out.println("===========================");
        System.out.println("Создание задач:");
        testCreatingTasks();
        printTasks();
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на Выполнено:");
        testChangingTaskStatuses(TaskStatus.DONE);
        printTasks();
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на В работе:");
        testChangingTaskStatuses(TaskStatus.IN_PROGRESS);
        printTasks();
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Сменить статусы на Новая:");
        testChangingTaskStatuses(TaskStatus.NEW);
        printTasks();
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить обычную задачу:");
        testDeletingTask();
        printTasks();
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить Эпик:");
        testDeletingEpic();
        printTasks();
        System.out.println("===========================");

        System.out.println();
        System.out.println("===========================");
        System.out.println("Удалить подзадачу:");
        testDeletingSubTask();
        printTasks();
        System.out.println("===========================");

    }

    public static void testCreatingTasks() {

        int taskId = TaskManager.getNextTaskId();
        String title = "Задача 1";
        String description = "Протестировать создание обычных задач в статусе Новая.";
        TaskStatus status = TaskStatus.NEW;
        Task task = new Task(taskId, title, description, status);
        TaskManager.createTask(task);
        assert TaskManager.getTaskById(taskId).equals(task);

        taskId = TaskManager.getNextTaskId();
        title = "Задача 2";
        description = "Протестировать создание обычных задач в статусе В работе.";
        status = TaskStatus.IN_PROGRESS;
        task = new Task(taskId, title, description, status);
        TaskManager.createTask(task);
        assert TaskManager.getTaskById(taskId).equals(task);

        int epicId = TaskManager.getNextTaskId();
        title = "Эпик 1";
        description = "Протестировать создание эпиков.";
        status = TaskStatus.NEW;
        Epic epic = new Epic(epicId, title, description, status);
        TaskManager.createEpic(epic);
        assert TaskManager.getEpicById(epicId).equals(epic);

        taskId = TaskManager.getNextTaskId();
        title = "Подзадача 1";
        description = "Протестировать создание подзадач в статусе Новая.";
        status = TaskStatus.NEW;
        SubTask subTask = new SubTask(taskId, title, description, status, epicId);
        TaskManager.createSubTask(subTask);
        assert TaskManager.getSubTaskById(taskId).equals(subTask);
        TaskStatus epicStatus = TaskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.NEW;

        taskId = TaskManager.getNextTaskId();
        title = "Подзадача 2";
        description = "Протестировать создание подзадач в статусе В работе.";
        status = TaskStatus.IN_PROGRESS;
        subTask = new SubTask(taskId, title, description, status, epicId);
        TaskManager.createSubTask(subTask);
        assert TaskManager.getSubTaskById(taskId).equals(subTask);
        epicStatus = TaskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.IN_PROGRESS;

        epicId = TaskManager.getNextTaskId();
        title = "Эпик 2";
        description = "Протестировать создание эпиков.";
        status = TaskStatus.DONE;
        epic = new Epic(epicId, title, description, status);
        TaskManager.createEpic(epic);

        epicStatus = TaskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.NEW;

        taskId = TaskManager.getNextTaskId();
        title = "Подзадача 3";
        description = "Протестировать создание подзадач в статусе В работе.";
        status = TaskStatus.IN_PROGRESS;
        subTask = new SubTask(taskId, title, description, status, epicId);
        TaskManager.createSubTask(subTask);
        assert TaskManager.getSubTaskById(taskId).equals(subTask);

        epicStatus = TaskManager.getEpicById(epicId).getTaskStatus();
        assert epicStatus == TaskStatus.IN_PROGRESS;

    }

    public static void printTasks() {
        System.out.println();
        System.out.println("Список задач:");
        System.out.println(TaskManager.getTaskArray());

        System.out.println();
        System.out.println("Список эпиков:");
        System.out.println(TaskManager.getEpicArray());

        System.out.println();
        System.out.println("Список подзадач:");
        System.out.println(TaskManager.getSubTaskArray());
    }

    public static void testChangingTaskStatuses(TaskStatus status) {

        for (Map.Entry<Integer, Task> integerTaskEntry : TaskManager.getTasks().entrySet()) {
            Task task = integerTaskEntry.getValue();
            task.setTaskStatus(status);
            TaskManager.updateTask(task);

            TaskStatus taskStatus = TaskManager.getTaskById(integerTaskEntry.getKey()).getTaskStatus();
            assert taskStatus == status;
        }

        for (Map.Entry<Integer, Epic> integerTaskEntry : TaskManager.getEpics().entrySet()) {
            Epic task = integerTaskEntry.getValue();

            TaskStatus originalStatus = task.getTaskStatus();  // Для теста ниже, что статус не изменился.

            task.setTaskStatus(status);
            TaskManager.updateEpic(task);

            TaskStatus epicStatus = TaskManager.getEpicById(integerTaskEntry.getKey()).getTaskStatus();
            assert epicStatus == originalStatus;
        }

        for (Map.Entry<Integer, SubTask> integerTaskEntry : TaskManager.getSubTasks().entrySet()) {
            SubTask task = integerTaskEntry.getValue();
            task.setTaskStatus(status);
            TaskManager.updateSubTask(task);

            TaskStatus taskStatus = TaskManager.getSubTaskById(integerTaskEntry.getKey()).getTaskStatus();
            assert taskStatus == status;
        }

    }

    public static void testDeletingTask() {
        int taskId = TaskManager.getTasks().keySet().iterator().next();
        TaskManager.deleteTaskById(taskId);

        Task task = TaskManager.getTaskById(taskId);
        assert task == null;
    }

    public static void testDeletingEpic() {
        int taskId = TaskManager.getEpics().keySet().iterator().next();
        Epic epic = TaskManager.getEpicById(taskId);
        Set<Integer> epicSubTaskIds = epic.getSubTasks().keySet();

        TaskManager.deleteEpicById(taskId);

        Task task = TaskManager.getEpicById(taskId);
        assert task == null;

        // Проверить, что подзадачи отвязались от эпика и стали задачами
        if (epicSubTaskIds.isEmpty()) return;
        for (Integer subTaskId : epicSubTaskIds) {
            assert TaskManager.getTaskById(subTaskId) != null;
        }
    }

    public static void testDeletingSubTask() {
        int taskId = TaskManager.getSubTasks().keySet().iterator().next();
        TaskManager.deleteSubTaskById(taskId);

        Task task = TaskManager.getSubTaskById(taskId);
        assert task == null;
    }

}
