package handlers;


import typetokens.EpicListTypeToken;
import typetokens.SubTaskListTypeToken;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.TaskIdFormatException;
import exceptions.TaskNotFoundException;
import schemas.enums.Endpoint;
import schemas.tasks.Epic;
import services.HttpTaskServer;
import services.TaskManager;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
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
        }

    }

    private void handlePaths(
            HttpExchange exchange, Endpoint endpoint, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException {
        switch (endpoint) {
            case GET_COLLECTION -> handleGetEpics(exchange);
            case GET_OBJECT_BY_ID -> handleGetEpicById(exchange, requestPath);
            case GET_OBJECT_BY_ID_GET_COLLECTION -> handleGetEpicSubtasks(exchange, requestPath);
            case CREATE_OR_UPDATE_OBJECT -> handleEpicsPost(exchange);
            case DELETE_OBJECT_BY_ID -> handleDeleteEpicById(exchange, requestPath);
            default -> sendNotFound(exchange);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getEpics(), new EpicListTypeToken().getType());
        writeResponse(exchange, response, 200);
    }

    private void handleGetEpicById(
            HttpExchange exchange, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException {
        int taskId = getTaskId(requestPath);
        String response = HttpTaskServer.getGson().toJson(taskManager.getEpicById(taskId));
        writeResponse(exchange, response, 200);
    }

    private void handleGetEpicSubtasks(
            HttpExchange exchange, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException {
        int taskId = getTaskId(requestPath);
        String response = HttpTaskServer.getGson()
                .toJson(taskManager.getEpicSubtasks(taskId), new SubTaskListTypeToken().getType());
        writeResponse(exchange, response, 200);
    }

    private void handleCreateEpic(HttpExchange exchange, Epic task) throws IOException {
        taskManager.createEpic(task);
        writeResponse(exchange, "Задача добавлена.", 201);
    }

    private void handleUpdateEpic(
            HttpExchange exchange, Epic task
    ) throws IOException, TaskNotFoundException {
        taskManager.updateEpic(task);
        writeResponse(exchange, "Задача обновлена.", 201);
    }

    private void handleEpicsPost(
            HttpExchange exchange
    ) throws IOException, TaskNotFoundException {
        String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        Epic task = HttpTaskServer.getGson().fromJson(body, Epic.class);

        if (task.getTaskId() != 0) {
            handleUpdateEpic(exchange, task);
        } else {
            handleCreateEpic(exchange, task);
        }
    }

    private void handleDeleteEpicById(
            HttpExchange exchange, String requestPath
    ) throws IOException, TaskIdFormatException, TaskNotFoundException {
        int taskId = getTaskId(requestPath);
        taskManager.deleteEpicById(taskId);
        writeResponse(exchange, "Задача удалена", 200);
    }
}
