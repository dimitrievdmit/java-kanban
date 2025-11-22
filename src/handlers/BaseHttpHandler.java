package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import exceptions.TaskIdFormatException;
import schemas.enums.Endpoint;
import services.TaskManager;

import static schemas.enums.Endpoint.*;

abstract class BaseHttpHandler implements HttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    abstract void handleInner(HttpExchange exchange) throws IOException;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            handleInner(exchange);
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    protected void sendBadId(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Некорректный идентификатор задачи.", 400);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Ресурс, к которому обратились не найден.", 404);
    }

    protected void sendTaskNotFound(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Задача, к которой обратились не найдена.", 404);
    }

    protected void sendHasOverlaps(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Добавляемая задача пересекается с существующими.", 406);
    }

    protected void sendInternalServerError(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Произошла непредвиденная ошибка на стороне сервера.", 500);
    }

    protected void writeResponse(
            HttpExchange exchange,
            String responseString,
            int responseCode
    ) throws IOException {
        byte[] resp = responseString.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(responseCode, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (pathParts.length == 2) {
            return switch (requestMethod) {
                case "GET" -> GET_COLLECTION;
                case "POST" -> CREATE_OR_UPDATE_OBJECT;
                default -> Endpoint.UNKNOWN;
            };
        }
        if (pathParts.length == 3) {
            return switch (requestMethod) {
                case "GET" -> GET_OBJECT_BY_ID;
                case "DELETE" -> DELETE_OBJECT_BY_ID;
                default -> Endpoint.UNKNOWN;
            };
        }
        if (pathParts.length == 4 && requestMethod.equals("GET")) {
            return GET_OBJECT_BY_ID_GET_COLLECTION;
        }
        return Endpoint.UNKNOWN;
    }

    protected int getTaskId(String requestPath) throws TaskIdFormatException {
        String[] pathParts = requestPath.split("/");
        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException exception) {
            throw new TaskIdFormatException();
        }
    }
}
