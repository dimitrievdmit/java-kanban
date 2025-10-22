import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    private File saveFile;

    @BeforeEach
    @Override
    void setup() {
        try {
            saveFile = File.createTempFile("FileBackedTaskManagerTestFile", null);
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
        taskManager = new FileBackedTaskManager(saveFile);
    }

    @Test
    void shouldSaveEmptyFile() throws IOException {
        Task task = new Task(title, description, status);
        int taskId = taskManager.createTask(task);
        taskManager.deleteTaskById(taskId); // удаление последней задачи должно сохранить пустой файл

        String fileContent = Files.readString(saveFile.toPath());
        String expectedFileContent = String.format("%s\n", FileBackedTaskManager.SAVE_FILE_HEADER);
        assertEquals(expectedFileContent, fileContent, "Файл сохранений не пустой.");
    }

    @Test
    void shouldLoadFromEmptyFile() {
        FileBackedTaskManager emptyTaskManager = FileBackedTaskManager.loadFromFile(saveFile);
        assertNotNull(emptyTaskManager, "Менеджер не создался.");
    }

    int[] createMultipleTasks() {
        Task task = new Task(title, description, status);
        int taskId = taskManager.createTask(task);

        int epicId = taskManager.createEpic(new Epic(title, description, status));
        SubTask subTask = new SubTask(title, description, status, epicId);

        int subTaskId = taskManager.createSubTask(subTask);

        return new int[]{taskId, epicId, subTaskId};
    }

    @Test
    void shouldSaveMultipleTasks() throws IOException {
        int[] taskIds = createMultipleTasks();
        int taskId = taskIds[0];
        int epicId = taskIds[1];
        int subTaskId = taskIds[2];
        String expectedFileContent = String.format(
                "%s\n%s\n%s\n%s\n",
                FileBackedTaskManager.SAVE_FILE_HEADER,
                taskManager.getTaskById(taskId).toCsvString(),
                taskManager.getEpicById(epicId).toCsvString(),
                taskManager.getSubTaskById(subTaskId).toCsvString()
        );
        String fileContent = Files.readString(saveFile.toPath());
        assertEquals(expectedFileContent, fileContent, "Файл имеет неожиданное содержимое.");
    }

    @Test
    void shouldLoadMultipleTasks() {
        int[] taskIds = createMultipleTasks();
        int taskId = taskIds[0];
        int epicId = taskIds[1];
        int subTaskId = taskIds[2];

        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(saveFile);

        Task oldTask = taskManager.getTaskById(taskId);
        Task recreatedTask = newManager.getTaskById(taskId);
        assertEquals(oldTask.taskId, recreatedTask.taskId, "Задача изменилась при перезагрузке.");
        assertEquals(oldTask.title, recreatedTask.title, "Задача изменилась при перезагрузке.");
        assertEquals(oldTask.description, recreatedTask.description, "Задача изменилась при перезагрузке.");
        assertEquals(oldTask.taskStatus, recreatedTask.taskStatus, "Задача изменилась при перезагрузке.");

        Epic oldEpic = taskManager.getEpicById(epicId);
        Epic recreatedEpic = newManager.getEpicById(epicId);
        assertEquals(oldEpic.taskId, recreatedEpic.taskId, "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.title, recreatedEpic.title, "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.description, recreatedEpic.description, "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.taskStatus, recreatedEpic.taskStatus, "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.getSubTaskIds(), recreatedEpic.getSubTaskIds(), "Эпик изменился при перезагрузке.");

        SubTask oldSubTask = taskManager.getSubTaskById(subTaskId);
        SubTask recreatedSubTask = newManager.getSubTaskById(subTaskId);
        assertEquals(oldSubTask.taskId, recreatedSubTask.taskId, "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.title, recreatedSubTask.title, "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.description, recreatedSubTask.description, "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.taskStatus, recreatedSubTask.taskStatus, "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.getEpicId(), recreatedSubTask.getEpicId(), "Подзадача изменилась при перезагрузке.");

    }

}