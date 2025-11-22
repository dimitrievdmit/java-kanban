package services;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    InMemoryTaskManager getManager() {
        return new InMemoryTaskManager();
    }
}
