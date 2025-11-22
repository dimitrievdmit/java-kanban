package schemas.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemas.enums.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {
    private static Epic firstEpic;
    private static final int firstEpicId = 1;

    @BeforeEach
    void beforeEach() {
        firstEpic = new Epic("test", "test", TaskStatus.NEW);
        firstEpic.setTaskId(firstEpicId);
    }


    @Test
    void sameIdShouldBeEquals() {
        Epic secondEpicId = new Epic("test2", "test2", TaskStatus.IN_PROGRESS);
        secondEpicId.setTaskId(firstEpicId);
        assertEquals(firstEpic, secondEpicId);
    }


    @Test
    void shouldNotAddOwnIdAsSubtask() {
        firstEpic.addSubTaskId(firstEpicId);
        assertTrue(firstEpic.getSubTaskIds().isEmpty());
    }

}