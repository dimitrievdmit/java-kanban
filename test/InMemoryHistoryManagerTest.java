import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    HistoryManager historyManager;
    InMemoryHistoryManager inMemoryHistoryManager;
    private final String title = "testTitle";
    private final String description = "testDescription";
    private final TaskStatus status = TaskStatus.NEW;
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
        Task task = new Task(firstTaskId, title, description, status);
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
        SubTask task = new SubTask(firstTaskId, title, description, status, firstTaskEpicId);

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
        Epic task = new Epic(firstTaskId, title, description, status, firstEpicSubTasks);

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
    void shouldRemoveFirstWhenAddingAboveLimit() {
        int historyManagerLimit = inMemoryHistoryManager.getLimit();

        for (int i = firstTaskId; i < (firstTaskId + historyManagerLimit + 1); i++) {
            String newTitle = title + i;
            Task task = new Task(i, newTitle, description, status);
            historyManager.add(task);
        }
        int newFirstTaskId = historyManager.getHistory().getFirst().getTaskId();
        assertEquals(firstTaskId, (newFirstTaskId - 1));
    }

}