package schemas.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemas.enums.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubTaskTest {
    private static SubTask firstSubTask;
    private static final int firstSubTaskId = 1;
    private static final int firstEpicId = firstSubTaskId + 1;

    @BeforeEach
    void beforeEach() {
        firstSubTask = new SubTask("test", "test", TaskStatus.NEW, firstEpicId);
        firstSubTask.setTaskId(firstSubTaskId);
    }


    @Test
    void sameIdShouldBeEquals() {
        SubTask secondSubTaskId = new SubTask("test2", "test2", TaskStatus.IN_PROGRESS, 2);
        secondSubTaskId.setTaskId(firstSubTaskId);
        assertEquals(firstSubTask, secondSubTaskId);
    }


    @Test
    void shouldNotAddOwnIdAsEpic() {
        firstSubTask.setEpicId(firstSubTaskId);
        assertEquals(firstEpicId, firstSubTask.getEpicId());
    }

}