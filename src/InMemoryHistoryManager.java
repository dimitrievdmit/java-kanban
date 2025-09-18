import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final int limit = 10;
    private final List<Task> taskHistory = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;
        Task taskCopy = deepCopyTask(task);
        if (taskCopy == null) return;
        taskHistory.add(taskCopy);
        while (taskHistory.size() > limit) {
            taskHistory.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory;
    }

    public int getLimit() {
        return limit;
    }

    // Метод нужен, чтобы в истории сохранялась старая версия задачи
//    Логика не лишняя, так как в ТЗ есть требование ниже:
//    "убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных."
    private Task deepCopyTask(Task task) {
        if (task.getClass().equals(Task.class)) {
            return new Task(task.taskId, task.title, task.description, task.taskStatus);

        } else if (task.getClass().equals(SubTask.class)) {
            return new SubTask(task.taskId, task.title, task.description, task.taskStatus, ((SubTask) task).getEpicId());

        } else if (task.getClass().equals(Epic.class)) {
            List<Integer> subTaskIds = ((Epic) task).getSubTaskIds();
            ArrayList<Integer> newSubTaskIds = new ArrayList<>(subTaskIds);

            return new Epic(task.taskId, task.title, task.description, task.taskStatus, newSubTaskIds);
        } else {
            return null;
        }
    }
}
