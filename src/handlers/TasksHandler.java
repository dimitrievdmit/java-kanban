package handlers;


import TypeTokens.TaskListTypeToken;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.TaskIdFormatException;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import schemas.enums.Endpoint;
import schemas.tasks.Task;
import services.HttpTaskServer;
import services.TaskManager;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
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
            case GET_COLLECTION -> handleGetTasks(exchange);
            case GET_OBJECT_BY_ID -> handleGetTaskById(exchange, requestPath);
            case CREATE_OR_UPDATE_OBJECT -> handleTasksPost(exchange);
            case DELETE_OBJECT_BY_ID -> handleDeleteTaskById(exchange, requestPath);
            default -> sendNotFound(exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getTasks(), new TaskListTypeToken().getType());
        writeResponse(exchange, response, 200);
    }

    private void handleGetTaskById(
            HttpExchange exchange, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException {
        int taskId = getTaskId(requestPath);
        String response = HttpTaskServer.getGson().toJson(taskManager.getTaskById(taskId));
        writeResponse(exchange, response, 200);
    }

    private void handleCreateTask(HttpExchange exchange, Task task) throws IOException, TaskTimeIntersectionException {
        taskManager.createTask(task);
        writeResponse(exchange, "Задача добавлена.", 201);
    }

    private void handleUpdateTask(
            HttpExchange exchange, Task task
    ) throws IOException, TaskTimeIntersectionException, TaskNotFoundException {
        taskManager.updateTask(task);
        writeResponse(exchange, "Задача обновлена.", 201);
    }

    private void handleTasksPost(
            HttpExchange exchange
    ) throws IOException, TaskTimeIntersectionException, TaskNotFoundException {
        String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        Task task = HttpTaskServer.getGson().fromJson(body, Task.class);

        if (task.getTaskId() != 0) {
            handleUpdateTask(exchange, task);
        } else {
            handleCreateTask(exchange, task);
        }
    }

    private void handleDeleteTaskById(
            HttpExchange exchange, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException {
        int taskId = getTaskId(requestPath);
        taskManager.deleteTaskById(taskId);
        writeResponse(exchange, "Задача удалена", 200);
    }

}
