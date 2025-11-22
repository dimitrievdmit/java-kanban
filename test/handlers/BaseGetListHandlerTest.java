package handlers;

import exceptions.TaskTimeIntersectionException;
import schemas.tasks.Epic;
import schemas.tasks.SubTask;
import schemas.tasks.Task;
import services.TaskManagerTest;

import java.io.IOException;

import static services.HttpTaskServer.*;
import static services.TaskManagerTest.DURATION;
import static services.TaskManagerTest.NEW_START_TIME;

class BaseGetListHandlerTest extends BaseHttpHandlerTest {
    protected int taskId;
    protected int epicId;
    protected int subTaskId1;
    protected int subTaskId2;


    protected void createTasksAllTypes() throws IOException, InterruptedException, TaskTimeIntersectionException {
        Task task = TaskManagerTest.getDefaultTask();
        String taskJson = gson.toJson(task);

//      Создать задачи через обычного менеджера
        taskId = controlTaskManager.createTask(task);

        Epic epic = TaskManagerTest.getDefaultEpic();
        String epicJson = gson.toJson(epic);

//      Создать задачу через обычного менеджера
        epicId = controlTaskManager.createEpic(epic);

        SubTask task1 = TaskManagerTest.getDefaultSubTask(epicId, NEW_START_TIME);
        String subTask1Json = gson.toJson(task1);
        SubTask task2 = TaskManagerTest.getDefaultSubTask(epicId, NEW_START_TIME.plus(DURATION).plusDays(5));
        String subTask2Json = gson.toJson(task2);

//      Создать задачи через обычного менеджера
        subTaskId1 = controlTaskManager.createSubTask(task1);
        subTaskId2 = controlTaskManager.createSubTask(task2);

//      Создать задачи через АПИ
        assertStatus(TASKS_PATH, 201, sendPost(TASKS_PATH, taskJson));
        assertStatus(EPICS_PATH, 201, sendPost(EPICS_PATH, epicJson));
        assertStatus(SUBTASKS_PATH, 201, sendPost(SUBTASKS_PATH, subTask1Json));
        assertStatus(SUBTASKS_PATH, 201, sendPost(SUBTASKS_PATH, subTask2Json));
    }
}