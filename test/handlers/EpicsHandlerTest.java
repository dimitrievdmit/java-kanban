package handlers;

import TypeTokens.EpicListTypeToken;
import TypeTokens.SubTaskListTypeToken;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import org.junit.jupiter.api.Test;
import schemas.tasks.Epic;
import schemas.tasks.SubTask;
import services.TaskManagerTest;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import static services.HttpTaskServer.EPICS_PATH;
import static services.HttpTaskServer.SUBTASKS_PATH;
import static services.TaskManagerTest.*;

class EpicsHandlerTest extends BaseTasksHandlerTest {
    private static final String BASE_PATH = EPICS_PATH;

    @Test
    void shouldCreateEpic() throws TaskNotFoundException, IOException, InterruptedException {
        Epic task = TaskManagerTest.getDefaultEpic();
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера
        Epic expectedTask = controlTaskManager.getEpicById(controlTaskManager.createEpic(task));
        String expectedTaskJson = gson.toJson(task);
        baseShouldCreateTask(BASE_PATH, taskJson, expectedTask.getTaskId(), expectedTaskJson);
    }

    @Test
    void shouldUpdateEpic() throws TaskNotFoundException, IOException, InterruptedException {
        Epic task = TaskManagerTest.getDefaultEpic();
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера
        Epic initialTask = controlTaskManager.getEpicById(controlTaskManager.createEpic(task));

//        Подготовить обновлённую задачу
        Epic updatedTask = new Epic(initialTask.getTaskId(), NEW_TITLE, NEW_DESCRIPTION, NEW_STATUS);

//      Обновить задачу через обычного менеджера
        controlTaskManager.updateEpic(updatedTask);
        Epic expectedTask = controlTaskManager.getEpicById(initialTask.getTaskId());
        String expectedTaskJson = gson.toJson(expectedTask);

        baseShouldUpdateTask(BASE_PATH, taskJson, updatedTask, initialTask.getTaskId(), expectedTaskJson);
    }

    @Test
    void shouldDeleteEpic() throws TaskNotFoundException, IOException, InterruptedException {
        Epic task = TaskManagerTest.getDefaultEpic();
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера, чтобы узнать ИД созданной задачи
//      По ТЗ, АПИ на создание задачи не должно возвращать данных, даже ИД задачи.
        Epic initialTask = controlTaskManager.getEpicById(controlTaskManager.createEpic(task));

        baseShouldDeleteTask(BASE_PATH, taskJson, initialTask.getTaskId());
    }

    @Test
    void shouldGetAllEpics() throws IOException, InterruptedException {
        Epic task1 = TaskManagerTest.getDefaultEpic();
        String task1Json = gson.toJson(task1);
        Epic task2 = TaskManagerTest.getDefaultEpic();
        String task2Json = gson.toJson(task2);

//      Создать задачи через обычного менеджера
        controlTaskManager.createEpic(task1);
        controlTaskManager.createEpic(task2);

//      Получить созданные задачи через обычного менеджера
        List<Epic> expectedTasks = controlTaskManager.getEpics();
        String expectedTasksJson = gson.toJson(expectedTasks, new EpicListTypeToken().getType());

        baseShouldGetAllTasks(BASE_PATH, task1Json, task2Json, expectedTasksJson);
    }

    @Test
    void shouldGetEpicSubtasks() throws TaskNotFoundException, TaskTimeIntersectionException, IOException, InterruptedException {
        Epic epic = TaskManagerTest.getDefaultEpic();
        String epicJson = gson.toJson(epic);

//      Создать задачу через обычного менеджера
        int epicId = controlTaskManager.createEpic(epic);

        SubTask task1 = TaskManagerTest.getDefaultSubTask(epicId, START_TIME);
        String task1Json = gson.toJson(task1);
        SubTask task2 = TaskManagerTest.getDefaultSubTask(epicId, START_TIME.plus(DURATION).plusDays(5));
        String task2Json = gson.toJson(task2);

//      Создать задачи через обычного менеджера
        controlTaskManager.createSubTask(task1);
        controlTaskManager.createSubTask(task2);

//      Получить созданные задачи через обычного менеджера
        List<SubTask> expectedTasks = controlTaskManager.getEpicSubtasks(epicId);
        String expectedTasksJson = gson.toJson(expectedTasks, new SubTaskListTypeToken().getType());

//      Создать задачи через АПИ
        sendPost(EPICS_PATH, epicJson);
        sendPost(SUBTASKS_PATH, task1Json);
        sendPost(SUBTASKS_PATH, task2Json);

//      Получить созданные задачи через АПИ
        String getEpicSubtasksPath = String.format("%s/%s/%s", EPICS_PATH, epicId, SUBTASKS_PATH);
        HttpResponse<String> response = sendGet(getEpicSubtasksPath);

//      Проверяем, что задачи совпадают.
        assertStatus(getEpicSubtasksPath, 200, response);
        assertBody(getEpicSubtasksPath, expectedTasksJson, response);

    }
}