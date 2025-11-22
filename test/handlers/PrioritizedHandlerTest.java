package handlers;

import typeTokens.TaskListTypeToken;
import exceptions.TaskTimeIntersectionException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;

import static services.HttpTaskServer.*;

class PrioritizedHandlerTest extends BaseGetListHandlerTest {
    private static final String BASE_PATH = PRIORITIZED_PATH;

    @Test
    void shouldGetPrioritized() throws TaskTimeIntersectionException, IOException, InterruptedException {

        createTasksAllTypes();

//      Получить отсортированные задачи через обычного менеджера
        String expectedTasksJson = gson.toJson(controlTaskManager.getPrioritizedTasks(), new TaskListTypeToken().getType());

//      Получить отсортированные задачи через АПИ
        HttpResponse<String> response = sendGet(BASE_PATH);

//      Проверяем, что задачи совпадают.
        assertStatus(BASE_PATH, 200, response);
        assertBody(BASE_PATH, expectedTasksJson, response);

    }
}