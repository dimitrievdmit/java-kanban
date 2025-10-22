import java.util.Comparator;

public class TaskIdComparator implements Comparator<Task> {

    @Override
    public int compare(Task task1, Task task2) {
        return Integer.compare(task1.taskId, task2.taskId);
    }
}
