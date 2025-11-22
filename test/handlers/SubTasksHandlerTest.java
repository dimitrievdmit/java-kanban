package handlers;

import typeTokens.SubTaskListTypeToken;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemas.tasks.Epic;
import schemas.tasks.SubTask;
import services.TaskManagerTest;

import java.io.IOException;
import java.util.List;

import static services.HttpTaskServer.EPICS_PATH;
import static services.HttpTaskServer.SUBTASKS_PATH;
import static services.TaskManagerTest.*;

class SubTasksHandlerTest extends BaseTasksHandlerTest {
    private static final String BASE_PATH = SUBTASKS_PATH;
    private static int epicId;

    @BeforeEach
    void createEpic() throws IOException, InterruptedException {
        // Нельзя создать подзадачу без эпика.
        Epic task = TaskManagerTest.getDefaultEpic();
        String taskJson = gson.toJson(task);
//      Создать задачу через обычного менеджера
        epicId = controlTaskManager.createEpic(task);
//      Создать задачу через АПИ
        sendPost(EPICS_PATH, taskJson);
    }

    @Test
    void shouldCreateTask() throws TaskNotFoundException, TaskTimeIntersectionException, IOException, InterruptedException {
        SubTask task = TaskManagerTest.getDefaultSubTask(epicId);
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера
        SubTask expectedTask = controlTaskManager.getSubTaskById(controlTaskManager.createSubTask(task));
        String expectedTaskJson = gson.toJson(task);
        baseShouldCreateTask(BASE_PATH, taskJson, expectedTask.getTaskId(), expectedTaskJson);
    }

    @Test
    void shouldUpdateTask() throws TaskNotFoundException, TaskTimeIntersectionException, IOException, InterruptedException {
        SubTask task = TaskManagerTest.getDefaultSubTask(epicId);
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера
        SubTask initialTask = controlTaskManager.getSubTaskById(controlTaskManager.createSubTask(task));

//        Подготовить обновлённую задачу
        SubTask updatedTask = new SubTask(initialTask.getTaskId(), NEW_TITLE, NEW_DESCRIPTION, NEW_STATUS, epicId);

//      Обновить задачу через обычного менеджера
        controlTaskManager.updateSubTask(updatedTask);
        SubTask expectedTask = controlTaskManager.getSubTaskById(initialTask.getTaskId());
        String expectedTaskJson = gson.toJson(expectedTask);

        baseShouldUpdateTask(BASE_PATH, taskJson, updatedTask, initialTask.getTaskId(), expectedTaskJson);
    }

    @Test
    void shouldDeleteTask() throws TaskNotFoundException, TaskTimeIntersectionException, IOException, InterruptedException {
        SubTask task = TaskManagerTest.getDefaultSubTask(epicId);
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера, чтобы узнать ИД созданной задачи
//      По ТЗ, АПИ на создание задачи не должно возвращать данных, даже ИД задачи.
        SubTask initialTask = controlTaskManager.getSubTaskById(controlTaskManager.createSubTask(task));

        baseShouldDeleteTask(BASE_PATH, taskJson, initialTask.getTaskId());
    }

    @Test
    void shouldGetAllTasks() throws TaskTimeIntersectionException, IOException, InterruptedException {
        SubTask task1 = TaskManagerTest.getDefaultSubTask(epicId, START_TIME);
        String task1Json = gson.toJson(task1);
        SubTask task2 = TaskManagerTest.getDefaultSubTask(epicId, START_TIME.plus(DURATION).plusDays(5));
        String task2Json = gson.toJson(task2);

//      Создать задачи через обычного менеджера
        controlTaskManager.createSubTask(task1);
        controlTaskManager.createSubTask(task2);

//      Получить созданные задачи через обычного менеджера
        List<SubTask> expectedTasks = controlTaskManager.getSubTasks();
        String expectedTasksJson = gson.toJson(expectedTasks, new SubTaskListTypeToken().getType());

        baseShouldGetAllTasks(BASE_PATH, task1Json, task2Json, expectedTasksJson);
    }
}