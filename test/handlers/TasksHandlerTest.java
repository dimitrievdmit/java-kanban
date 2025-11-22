package handlers;

import typeTokens.TaskListTypeToken;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import org.junit.jupiter.api.Test;
import schemas.tasks.Task;
import services.TaskManagerTest;

import java.io.IOException;
import java.util.List;

import static services.HttpTaskServer.TASKS_PATH;
import static services.TaskManagerTest.*;

class TasksHandlerTest extends BaseTasksHandlerTest {
    private static final String BASE_PATH = TASKS_PATH;

    @Test
    void shouldCreateTask() throws TaskNotFoundException, TaskTimeIntersectionException, IOException, InterruptedException {
        Task task = TaskManagerTest.getDefaultTask();
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера
        Task expectedTask = controlTaskManager.getTaskById(controlTaskManager.createTask(task));
        String expectedTaskJson = gson.toJson(task);
        baseShouldCreateTask(BASE_PATH, taskJson, expectedTask.getTaskId(), expectedTaskJson);
    }

    @Test
    void shouldUpdateTask() throws TaskNotFoundException, TaskTimeIntersectionException, IOException, InterruptedException {
        Task task = TaskManagerTest.getDefaultTask();
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера
        Task initialTask = controlTaskManager.getTaskById(controlTaskManager.createTask(task));

//        Подготовить обновлённую задачу
        Task updatedTask = new Task(initialTask.getTaskId(), NEW_TITLE, NEW_DESCRIPTION, NEW_STATUS);

//      Обновить задачу через обычного менеджера
        controlTaskManager.updateTask(updatedTask);
        Task expectedTask = controlTaskManager.getTaskById(initialTask.getTaskId());
        String expectedTaskJson = gson.toJson(expectedTask);

        baseShouldUpdateTask(BASE_PATH, taskJson, updatedTask, initialTask.getTaskId(), expectedTaskJson);
    }

    @Test
    void shouldDeleteTask() throws TaskNotFoundException, TaskTimeIntersectionException, IOException, InterruptedException {
        Task task = TaskManagerTest.getDefaultTask();
        String taskJson = gson.toJson(task);

//      Создать задачу через обычного менеджера, чтобы узнать ИД созданной задачи
//      По ТЗ, АПИ на создание задачи не должно возвращать данных, даже ИД задачи.
        Task initialTask = controlTaskManager.getTaskById(controlTaskManager.createTask(task));

        baseShouldDeleteTask(BASE_PATH, taskJson, initialTask.getTaskId());
    }

    @Test
    void shouldGetAllTasks() throws TaskTimeIntersectionException, IOException, InterruptedException {
        Task task1 = TaskManagerTest.getDefaultTask();
        String task1Json = gson.toJson(task1);
        Task task2= TaskManagerTest.getDefaultTask(NEW_START_TIME);
        String task2Json = gson.toJson(task2);

//      Создать задачи через обычного менеджера
        controlTaskManager.createTask(task1);
        controlTaskManager.createTask(task2);

//      Получить созданные задачи через обычного менеджера
        List<Task> expectedTasks = controlTaskManager.getTasks();
        String expectedTasksJson = gson.toJson(expectedTasks, new TaskListTypeToken().getType());

        baseShouldGetAllTasks(BASE_PATH, task1Json, task2Json, expectedTasksJson);
    }
}