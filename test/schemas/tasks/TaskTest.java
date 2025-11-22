package schemas.tasks;

import org.junit.jupiter.api.Test;
import schemas.enums.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    @Test
    void sameIdShouldBeEquals() {
        Task task1 = new Task("test", "test", TaskStatus.NEW);
        Task task2 = new Task("test2", "test2", TaskStatus.IN_PROGRESS);
        int taskId = 1;
        task1.setTaskId(taskId);
        task2.setTaskId(taskId);
        assertEquals(task1, task2);
    }

}