import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node<Task>> taskHistory = new HashMap<>();
    private Node<Task> head;
    private Node<Task> tail;

    private void linkLast(int taskId,  Task task) {
        final Node<Task> prevTail = tail;
        final Node<Task> newNode = new Node<>(prevTail, task, null);
        tail = newNode;
        if (prevTail == null)
            head = newNode;
        else
            prevTail.next = newNode;
        taskHistory.put(taskId, tail);
    }

    @Override
    public void add(Task task) {
        if (task == null) return;
        Task taskCopy = deepCopyTask(task);
        if (taskCopy == null) return;

        int taskId = task.getTaskId();
        remove(taskId);
        linkLast(taskId, taskCopy);
    }

    private void removeNode(Node<Task> nodeToRemove) {
        if (nodeToRemove == null) return;

        final Node<Task> before = nodeToRemove.prev;
        final Node<Task> after = nodeToRemove.next;

        if (before != null) {
            before.next = after;
        } else {
            head = after;
        }

        if (after != null) {
            after.prev = before;
        } else {
            tail = before;
        }
    }

    @Override
    public void remove(int id) {
        if (!taskHistory.containsKey(id)) return;

        final Node<Task> nodeToRemove = taskHistory.get(id);
        removeNode(nodeToRemove);

        taskHistory.remove(id);
    }

    private List<Task> getTasks() {
        List<Task> taskArray = new ArrayList<>(taskHistory.size());
        for (Node<Task> taskNode : taskHistory.values()) {
            taskArray.add(taskNode.item);
        }
        return taskArray;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    // Метод deepCopyTask нужен, чтобы в истории сохранялась старая версия задачи
//    Логика не лишняя, так как в ТЗ спринта 4 есть требование ниже:
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

    public static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
