import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<SubTask> getSubTasks();

    ArrayList<SubTask> getEpicSubtasks(int epicId);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();

    void deleteAllTasksAllTypes();

    Task getTaskById(int taskId);

    Epic getEpicById(int taskId);

    SubTask getSubTaskById(int taskId);

    int createTask(Task task);

    int createEpic(Epic task);

    int createSubTask(SubTask task);

    void updateTask(Task task);

    void updateEpic(Epic task);

    void updateSubTask(SubTask task);

    void deleteTaskById(Integer taskId);

    void deleteEpicById(Integer taskId);

    void deleteSubTaskById(Integer taskId);

    List<Task> getHistory();
}
