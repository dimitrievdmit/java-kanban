import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    HistoryManager historyManager;
    InMemoryHistoryManager inMemoryHistoryManager;
    private final String firstTitle = "testTitle";
    private final String firstDescription = "testDescription";
    private final TaskStatus firstStatus = TaskStatus.NEW;
    private static final int firstTaskId = 1;
    private static final int firstTaskEpicId = 2;
    private static final ArrayList<Integer> firstEpicSubTasks = new ArrayList<>(Arrays.asList(1, 2));

    @BeforeEach
    void setup() {
        historyManager = Managers.getDefaultHistory();
        inMemoryHistoryManager = (InMemoryHistoryManager) historyManager;
    }

    private <T extends Task> void updateTask(T task) {
        task.setTitle("newTitle");
        task.setDescription("newDescription");
        task.setTaskStatus(TaskStatus.DONE);
    }

    @Test
    void shouldKeepOldTaskVersions() {
        Task task = new Task(firstTaskId, firstTitle, firstDescription, firstStatus);
        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");

        List<Task> tasks = historyManager.getHistory();
        Task oldTask = tasks.getFirst();
        updateTask(task);

        assertNotEquals(task.title, oldTask.title);
        assertNotEquals(task.description, oldTask.description);
        assertNotEquals(task.taskStatus, oldTask.taskStatus);

    }

    @Test
    void shouldKeepOldSubTaskVersions() {
        SubTask task = new SubTask(firstTaskId, firstTitle, firstDescription, firstStatus, firstTaskEpicId);

        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");

        updateTask(task);
        task.setEpicId(999);

        List<Task> tasks = historyManager.getHistory();
        SubTask oldTask = (SubTask) tasks.getFirst();

        assertNotEquals(task.title, oldTask.title);
        assertNotEquals(task.description, oldTask.description);
        assertNotEquals(task.taskStatus, oldTask.taskStatus);
        assertNotEquals(task.getEpicId(), oldTask.getEpicId());

    }

    @Test
    void shouldKeepOldEpicVersions() {
        Epic task = new Epic(firstTaskId, firstTitle, firstDescription, firstStatus, firstEpicSubTasks);

        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");

        updateTask(task);
        task.addSubTaskId(999);

        List<Task> tasks = historyManager.getHistory();
        Epic oldTask = (Epic) tasks.getFirst();

        assertNotEquals(task.title, oldTask.title);
        assertNotEquals(task.description, oldTask.description);
        assertNotEquals(task.taskStatus, oldTask.taskStatus);
        assertNotEquals(task.getSubTaskIds(), oldTask.getSubTaskIds());

    }

    @Test
    void shouldRemoveExistingTaskWhenAddingTaskAgain() {
        Task task = new Task(firstTaskId, firstTitle, firstDescription, firstStatus);
        historyManager.add(task);

        updateTask(task);
        historyManager.add(task);

        assertNotEquals(firstTitle, task.title);
        assertNotEquals(firstDescription, task.description);
        assertNotEquals(firstStatus, task.taskStatus);

    }

    ArrayList<Task> setupThreeTasks(int firstId, int secondId, int thirdId) {
        Task task = new Task(firstId, firstTitle, firstDescription, firstStatus);
        historyManager.add(task);

        Task task2 = new Task(secondId, "secondTitle", "secondDescription", firstStatus);
        historyManager.add(task2);

        Task task3 = new Task(thirdId, "thirdTitle", "thirdDescription", firstStatus);
        historyManager.add(task3);

        ArrayList<Task> taskArrayList = new ArrayList<>();
        taskArrayList.add(task);
        taskArrayList.add(task2);
        taskArrayList.add(task3);

        return taskArrayList;
    }

    @Test
    void shouldReassignLinksWhenDeletingFirstNode() {
        int firstId = 1;
        int secondId = 2;
        int thirdId = 3;
        ArrayList<Task> taskArrayList = setupThreeTasks(firstId, secondId, thirdId);

        historyManager.remove(firstId);

        assertEquals(inMemoryHistoryManager.getHead().item, taskArrayList.get(1));
        assertEquals(inMemoryHistoryManager.getTail().item, taskArrayList.getLast());
    }

    @Test
    void shouldReassignLinksWhenDeletingMiddleNode() {
        int firstId = 1;
        int secondId = 2;
        int thirdId = 3;
        ArrayList<Task> taskArrayList = setupThreeTasks(firstId, secondId, thirdId);

        historyManager.remove(secondId);

        assertEquals(inMemoryHistoryManager.getHead().item, taskArrayList.getFirst());
        assertEquals(inMemoryHistoryManager.getTail().item, taskArrayList.getLast());
    }

    @Test
    void shouldReassignLinksWhenDeletingLastNode() {
        int firstId = 1;
        int secondId = 2;
        int thirdId = 3;
        ArrayList<Task> taskArrayList = setupThreeTasks(firstId, secondId, thirdId);

        historyManager.remove(thirdId);

        assertEquals(inMemoryHistoryManager.getHead().item, taskArrayList.getFirst());
        assertEquals(inMemoryHistoryManager.getTail().item, taskArrayList.get(1));
    }

}