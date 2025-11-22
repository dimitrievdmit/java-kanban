package handlers;


import typetokens.TaskListTypeToken;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import services.HttpTaskServer;
import services.TaskManager;

import schemas.enums.Endpoint;

import java.io.IOException;

import static schemas.enums.Endpoint.GET_COLLECTION;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handleInner(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(requestPath, exchange.getRequestMethod());

        if (endpoint == GET_COLLECTION) {
            handleGetHistory(exchange);
        } else {
            sendNotFound(exchange);
        }

    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getHistory(), new TaskListTypeToken().getType());
        writeResponse(exchange, response, 200);
    }
}
