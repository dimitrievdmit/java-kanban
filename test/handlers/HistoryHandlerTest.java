package handlers;

import typeTokens.TaskListTypeToken;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;

import static services.HttpTaskServer.*;

class HistoryHandlerTest extends BaseGetListHandlerTest {
    private static final String BASE_PATH = HISTORY_PATH;

    private void viewAllCreatedTasks() throws IOException, InterruptedException, TaskNotFoundException {
//        Просмотреть задачи через обычного менеджера
        controlTaskManager.getTaskById(taskId);
        controlTaskManager.getEpicById(epicId);
        controlTaskManager.getSubTaskById(subTaskId1);
        controlTaskManager.getSubTaskById(subTaskId2);

//        Просмотреть задачи через АПИ
        assertStatus(TASKS_PATH, 200, sendGet(String.format("%s/%s", TASKS_PATH, taskId)));
        assertStatus(EPICS_PATH, 200, sendGet(String.format("%s/%s", EPICS_PATH, epicId)));
        assertStatus(SUBTASKS_PATH, 200, sendGet(String.format("%s/%s", SUBTASKS_PATH, subTaskId1)));
        assertStatus(SUBTASKS_PATH, 200, sendGet(String.format("%s/%s", SUBTASKS_PATH, subTaskId2)));
    }

    @Test
    void shouldGetHistory() throws TaskTimeIntersectionException, IOException, InterruptedException, TaskNotFoundException {

        createTasksAllTypes();
        viewAllCreatedTasks();

//      Получить историю через обычного менеджера
        String expectedTasksJson = gson.toJson(controlTaskManager.getHistory(), new TaskListTypeToken().getType());

//      Получить историю через АПИ
        HttpResponse<String> response = sendGet(BASE_PATH);

//      Проверяем, что задачи совпадают.
        assertStatus(BASE_PATH, 200, response);
        assertBody(BASE_PATH, expectedTasksJson, response);

    }
}