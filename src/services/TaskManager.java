package services;

import exceptions.TaskNotFoundException;
import exceptions.TaskTimeIntersectionException;
import schemas.tasks.Epic;
import schemas.tasks.SubTask;
import schemas.tasks.Task;

import java.util.List;
import java.util.Set;

public interface TaskManager {
    List<Task> getTasks();

    List<Epic> getEpics();

    List<SubTask> getSubTasks();

    List<Task> getAllTasksAllTypes();

    Set<Task> getPrioritizedTasks();

    List<SubTask> getEpicSubtasks(int epicId) throws TaskNotFoundException;

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();

    void deleteAllTasksAllTypes();

    Task getTaskById(int taskId) throws TaskNotFoundException;

    Epic getEpicById(int taskId) throws TaskNotFoundException;

    SubTask getSubTaskById(int taskId) throws TaskNotFoundException;

    int createTask(Task task) throws TaskTimeIntersectionException;

    int createEpic(Epic task);

    int createSubTask(SubTask task) throws TaskTimeIntersectionException;

    void updateTask(Task task) throws TaskNotFoundException, TaskTimeIntersectionException;

    void updateEpic(Epic task) throws TaskNotFoundException;

    void updateSubTask(SubTask task) throws TaskNotFoundException, TaskTimeIntersectionException;

    void deleteTaskById(Integer taskId) throws TaskNotFoundException;

    void deleteEpicById(Integer taskId) throws TaskNotFoundException;

    void deleteSubTaskById(Integer taskId) throws TaskNotFoundException;

    List<Task> getHistory();
}
