import exceptions.ManagerSaveException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File saveFile;

    @Override
    FileBackedTaskManager getManager() {
        try {
            saveFile = File.createTempFile("FileBackedTaskManagerTestFile", null);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при работе с файлом сохранения.", e);
        }
        return new FileBackedTaskManager(saveFile);
    }

    @AfterEach
    void cleanTempFile() throws IOException {
        if (saveFile != null && saveFile.exists()) {
            Files.delete(saveFile.toPath());
        }
    }

    @Test
    void shouldSaveEmptyFile() throws IOException {
        int taskId = taskManager.createTask(getDefaultTask());
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
        int taskId = taskManager.createTask(getDefaultTask());
        int epicId = taskManager.createEpic(getDefaultEpic());
        int subTaskId = taskManager.createSubTask(getDefaultSubTask(epicId));
        int taskIdNoTime = taskManager.createTask(getDefaultTask(newStartTime.plus(duration).plusDays(5)));

        return new int[]{taskId, epicId, subTaskId, taskIdNoTime};
    }

    @Test
    void shouldSaveMultipleTasks() throws IOException {
        int[] taskIds = createMultipleTasks();
        int taskId = taskIds[0];
        int epicId = taskIds[1];
        int subTaskId = taskIds[2];
        int taskIdNoTime = taskIds[3];
        String expectedFileContent = String.format(
                "%s\n%s\n%s\n%s\n%s\n",
                FileBackedTaskManager.SAVE_FILE_HEADER,
                taskManager.getTaskById(taskId).toCsvString(),
                taskManager.getEpicById(epicId).toCsvString(),
                taskManager.getSubTaskById(subTaskId).toCsvString(),
                taskManager.getTaskById(taskIdNoTime).toCsvString()
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
        int taskIdNoTime = taskIds[3];

        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(saveFile);

        Task oldTask = taskManager.getTaskById(taskId);
        Task recreatedTask = newManager.getTaskById(taskId);
        assertEquals(oldTask.getTaskId(), recreatedTask.getTaskId(), "Задача изменилась при перезагрузке.");
        assertEquals(oldTask.getTitle(), recreatedTask.getTitle(), "Задача изменилась при перезагрузке.");
        assertEquals(oldTask.getDescription(), recreatedTask.getDescription(), "Задача изменилась при перезагрузке.");
        assertEquals(oldTask.getTaskStatus(), recreatedTask.getTaskStatus(), "Задача изменилась при перезагрузке.");
        assertEquals(oldTask.getDuration(), recreatedTask.getDuration(), "Задача изменилась при перезагрузке.");
        assertEquals(oldTask.getStartTime(), recreatedTask.getStartTime(), "Задача изменилась при перезагрузке.");

        Epic oldEpic = taskManager.getEpicById(epicId);
        Epic recreatedEpic = newManager.getEpicById(epicId);
        assertEquals(oldEpic.getTaskId(), recreatedEpic.getTaskId(), "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.getTitle(), recreatedEpic.getTitle(), "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.getDescription(), recreatedEpic.getDescription(), "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.getTaskStatus(), recreatedEpic.getTaskStatus(), "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.getSubTaskIds(), recreatedEpic.getSubTaskIds(), "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.getDuration(), recreatedEpic.getDuration(), "Эпик изменился при перезагрузке.");
        assertEquals(oldEpic.getStartTime(), recreatedEpic.getStartTime(), "Эпик изменился при перезагрузке.");

        SubTask oldSubTask = taskManager.getSubTaskById(subTaskId);
        SubTask recreatedSubTask = newManager.getSubTaskById(subTaskId);
        assertEquals(oldSubTask.getTaskId(), recreatedSubTask.getTaskId(), "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.getTitle(), recreatedSubTask.getTitle(), "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.getDescription(), recreatedSubTask.getDescription(), "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.getTaskStatus(), recreatedSubTask.getTaskStatus(), "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.getEpicId(), recreatedSubTask.getEpicId(), "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.getDuration(), recreatedSubTask.getDuration(), "Подзадача изменилась при перезагрузке.");
        assertEquals(oldSubTask.getStartTime(), recreatedSubTask.getStartTime(), "Подзадача изменилась при перезагрузке.");

        Task oldTaskNoTime = taskManager.getTaskById(taskIdNoTime);
        Task recreatedTaskNoTime = newManager.getTaskById(taskIdNoTime);
        assertEquals(oldTaskNoTime.getTaskId(), recreatedTaskNoTime.getTaskId(), "Задача без времени изменилась при перезагрузке.");
        assertEquals(oldTaskNoTime.getTitle(), recreatedTaskNoTime.getTitle(), " Задача без времени изменилась при перезагрузке.");
        assertEquals(oldTaskNoTime.getDescription(), recreatedTaskNoTime.getDescription(), " Задача без времени изменилась при перезагрузке.");
        assertEquals(oldTaskNoTime.getTaskStatus(), recreatedTaskNoTime.getTaskStatus(), " Задача без времени изменилась при перезагрузке.");
        assertEquals(oldTaskNoTime.getDuration(), recreatedTaskNoTime.getDuration(), " Задача без времени изменилась при перезагрузке.");
        assertEquals(oldTaskNoTime.getStartTime(), recreatedTaskNoTime.getStartTime(), " Задача без времени изменилась при перезагрузке.");

    }

    //    Проверить, что выпадет корректная ошибка
    @Test
    void shouldThrowOnIOExceptionWhenReading() throws IOException {
        Files.delete(saveFile.toPath()); // Удалить файл, чтобы вызвать ошибку чтения
        assertThrows(
                ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(saveFile),
                "Нет ошибки или не та ошибка."
        );
    }
//  Не могу нарочно вызвать IOException при записи даже через удаление файла и через обнуление пути.
//    Гугл не помогает.
//    @Test
//    void shouldThrowOnIOExceptionWhenWriting() throws IOException {
//        Files.delete(saveFile.toPath()); // Удалить файл
//        saveFile = null; // Обнулить путь к файлу, чтобы вызвать ошибку записи
//        assertThrows(
//                ManagerSaveException.class,
//                () -> taskManager.createTask(getDefaultTask()),
//                "Нет ошибки или не та ошибка."
//        );
//    }

}