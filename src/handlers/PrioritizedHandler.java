package handlers;


import typetokens.TaskListTypeToken;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import schemas.enums.Endpoint;
import services.HttpTaskServer;
import services.TaskManager;

import static schemas.enums.Endpoint.GET_COLLECTION;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handleInner(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(requestPath, exchange.getRequestMethod());

        if (endpoint == GET_COLLECTION) {
            handleGetPrioritized(exchange);
        } else {
            sendNotFound(exchange);
        }

    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getPrioritizedTasks(), new TaskListTypeToken().getType());
        writeResponse(exchange, response, 200);
    }
}
