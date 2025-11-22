package handlers;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import services.HttpTaskServer;
import services.Managers;
import services.TaskManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

abstract class BaseHttpHandlerTest {
    protected static TaskManager httpTaskManager = Managers.getDefault();
    protected static TaskManager controlTaskManager = Managers.getDefault();
    protected static HttpTaskServer httpTaskServer;
    protected final Gson gson = HttpTaskServer.getGson();
    protected static final String BASE_URL = "http://localhost";
    protected static int port;
    protected static String url_with_port;

    @BeforeEach
    void setUp() throws IOException {
        // Создаём второй 2 менеджера, чтобы сравнивать результаты от АПИ и через обычные методы.
        httpTaskManager = Managers.getDefault();
        controlTaskManager = Managers.getDefault();

//        Получаем свой порт для каждого теста, чтобы не было проблем с параллелизмом.
        port = getFreePort();
        url_with_port = BASE_URL + ":" + port;
        HttpTaskServer httpTaskServer = new HttpTaskServer(httpTaskManager, port);
        httpTaskServer.start();
    }

    @AfterEach
    void tearDown() {
        if (httpTaskServer != null) {
            httpTaskServer.stop();
        }
    }

    protected HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("%s/%s", url_with_port, path));
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(url)
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    protected HttpResponse<String> sendPost(String path, String requestBody) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("%s/%s", url_with_port, path));
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .uri(url)
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    protected HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("%s/%s", url_with_port, path));
            HttpRequest request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(url)
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    protected void assertStatus(String path, int expectedStatus, HttpResponse<String> response) {
        assertEquals(expectedStatus, response.statusCode(),
                String.format("Запрос на %s вернул неожиданный статус: %s", path, response.statusCode()));
    }

    protected void assertBody(String path, String expectedResult, HttpResponse<String> response) {
        assertEquals(expectedResult, response.body(),
                String.format("Запрос на %s вернул неожиданное тело запроса: \n%s", path, response.body()));
    }

    // Получаем свой порт для каждого теста, чтобы не было проблем с параллелизмом.
    private static int getFreePort() {
        int port = 0;
        // For ServerSocket port number 0 means that the port number is automatically allocated.
        try (ServerSocket socket = new ServerSocket(0)) {
            // Disable timeout and reuse address after closing the socket.
            socket.setReuseAddress(true);
            port = socket.getLocalPort();
        } catch (IOException ignored) {}
        if (port > 0) {
            return port;
        }
        throw new RuntimeException("Could not find a free port");
    }

}