import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultShouldReturnInitialisedObject() {
        TaskManager taskManager = Managers.getDefault();
        assertTrue(taskManager.getTasks().isEmpty());  // Проверить, что не статичный метод вызывается без ошибки.
    }

    @Test
    void getDefaultHistoryShouldReturnInitialisedObject() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertTrue(historyManager.getHistory().isEmpty());  // Проверить, что не статичный метод вызывается без ошибки.
    }
}