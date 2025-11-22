package services;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private int port = 8080;
    private final HttpServer httpServer;
    private final TaskManager taskManager;

    public static final String TASKS_PATH = "tasks";
    public static final String SUBTASKS_PATH = "subtasks";
    public static final String EPICS_PATH = "epics";
    public static final String HISTORY_PATH = "history";
    public static final String PRIORITIZED_PATH = "prioritized";

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
    }

    //    Конструктор для тестов, чтобы избежать проблем с параллелизмом
    public HttpTaskServer(TaskManager taskManager, int port) throws IOException {
        this.taskManager = taskManager;
        this.port = port;
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/" + TASKS_PATH, new TasksHandler(this.taskManager));
        httpServer.createContext("/" + SUBTASKS_PATH, new SubTasksHandler(this.taskManager));
        httpServer.createContext("/" + EPICS_PATH, new EpicsHandler(this.taskManager));
        httpServer.createContext("/" + HISTORY_PATH, new HistoryHandler(this.taskManager));
        httpServer.createContext("/" + PRIORITIZED_PATH, new PrioritizedHandler(this.taskManager));
    }

    public void start() {
        httpServer.start(); // запускаем сервер
        System.out.println("HTTP-сервер запущен на " + port + " порту.");
    }

    public void stop() {
        httpServer.stop(10);
        System.out.println("HTTP-сервер остановлен.");
    }

    public static Gson getGson() {
        // Метод на случай, если в будущем понадобится делать особую логику отображения и чтения задач.
        return new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.start();
    }
}
