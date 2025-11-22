package handlers;

import typetokens.SubTaskListTypeToken;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.TaskIdFormatException;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import schemas.enums.Endpoint;
import schemas.tasks.SubTask;
import services.HttpTaskServer;
import services.TaskManager;

import java.io.IOException;

public class SubTasksHandler extends BaseHttpHandler {
    public SubTasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handleInner(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(requestPath, exchange.getRequestMethod());

        try {
            handlePaths(exchange, endpoint, requestPath);
        } catch (TaskIdFormatException e) {
            sendBadId(exchange);
        } catch (TaskNotFoundException e) {
            sendTaskNotFound(exchange);
        } catch (TaskTimeIntersectionException e) {
            sendHasOverlaps(exchange);
        }

    }

    private void handlePaths(
            HttpExchange exchange, Endpoint endpoint, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException, TaskTimeIntersectionException {
        switch (endpoint) {
            case GET_COLLECTION -> handleGetSubTasks(exchange);
            case GET_OBJECT_BY_ID -> handleGetSubTaskById(exchange, requestPath);
            case CREATE_OR_UPDATE_OBJECT -> handleSubTasksPost(exchange);
            case DELETE_OBJECT_BY_ID -> handleDeleteSubTaskById(exchange, requestPath);
            default -> sendNotFound(exchange);
        }
    }

    private void handleGetSubTasks(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getSubTasks(), new SubTaskListTypeToken().getType());
        writeResponse(exchange, response, 200);
    }

    private void handleGetSubTaskById(
            HttpExchange exchange, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException {
        int taskId = getTaskId(requestPath);
        String response = HttpTaskServer.getGson().toJson(taskManager.getSubTaskById(taskId));
        writeResponse(exchange, response, 200);
    }

    private void handleCreateSubTask(HttpExchange exchange, SubTask task) throws IOException, TaskTimeIntersectionException {
        taskManager.createSubTask(task);
        writeResponse(exchange, "Задача добавлена.", 201);
    }

    private void handleUpdateSubTask(
            HttpExchange exchange, SubTask task
    ) throws IOException, TaskTimeIntersectionException, TaskNotFoundException {
        taskManager.updateSubTask(task);
        writeResponse(exchange, "Задача обновлена.", 201);
    }

    private void handleSubTasksPost(
            HttpExchange exchange
    ) throws IOException, TaskTimeIntersectionException, TaskNotFoundException {
        String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        SubTask task = HttpTaskServer.getGson().fromJson(body, SubTask.class);

        if (task.getTaskId() != 0) {
            handleUpdateSubTask(exchange, task);
        } else {
            handleCreateSubTask(exchange, task);
        }
    }

    private void handleDeleteSubTaskById(
            HttpExchange exchange, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException {
        int taskId = getTaskId(requestPath);
        taskManager.deleteSubTaskById(taskId);
        writeResponse(exchange, "Задача удалена", 200);
    }
}