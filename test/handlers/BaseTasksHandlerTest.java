package handlers;

import java.io.IOException;
import java.net.http.HttpResponse;

abstract class BaseTasksHandlerTest extends BaseHttpHandlerTest {

    void baseShouldCreateTask(String base_path, String taskJson, int taskId, String expectedTaskJson) throws IOException, InterruptedException {
//      Создать задачу через АПИ
        sendPost(base_path, taskJson);

//      Получаем задачу через АПИ
        String getByIdPath = String.format("%s/%s", base_path, taskId);
        HttpResponse<String> response = sendGet(getByIdPath);

//      Проверяем, что задачи совпадают.
        assertStatus(getByIdPath, 200, response);
        assertBody(getByIdPath, expectedTaskJson, response);
    }

    void baseShouldUpdateTask(
            String base_path,
            String taskJson,
            Object updatedTask,
            int taskId,
            String expectedTaskJson
    ) throws IOException, InterruptedException {
//      Создать задачу через АПИ
        sendPost(base_path, taskJson);

//      Обновить задачу через АПИ
        HttpResponse<String> postResponse = sendPost(base_path, gson.toJson(updatedTask));
        assertStatus(base_path, 201, postResponse);

//      Получаем задачу через АПИ
        String getByIdPath = String.format("%s/%s", base_path, taskId);
        HttpResponse<String> response = sendGet(getByIdPath);

//      Проверяем, что задачи совпадают.
        assertStatus(getByIdPath, 200, response);
        assertBody(getByIdPath, expectedTaskJson, response);
    }

    void baseShouldDeleteTask(
            String base_path,
            String taskJson,
            int taskId
    ) throws IOException, InterruptedException {
//      Создать задачу через АПИ
        sendPost(base_path, taskJson);

//      Удаляем задачу через АПИ
        String idPath = String.format("%s/%s", base_path, taskId);
        HttpResponse<String> deleteResponse = sendDelete(idPath);
//      Проверяем, что задача не найдена.
        assertStatus(idPath, 200, deleteResponse);

//      Получаем задачу через АПИ
        HttpResponse<String> getResponse = sendGet(idPath);

//      Проверяем, что задача не найдена.
        assertStatus(idPath, 404, getResponse);
    }


    void baseShouldGetAllTasks(
            String base_path,
            String task1Json,
            String task2Json,
            String expectedTasksJson
    ) throws IOException, InterruptedException {
//      Создать задачи через АПИ
        sendPost(base_path, task1Json);
        sendPost(base_path, task2Json);

//      Получить созданные задачи через АПИ
        HttpResponse<String> response = sendGet(base_path);

//      Проверяем, что задачи совпадают.
        assertStatus(base_path, 200, response);
        assertBody(base_path, expectedTasksJson, response);
    }
}