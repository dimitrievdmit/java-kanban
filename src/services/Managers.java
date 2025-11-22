package services;

import exceptions.ManagerSaveException;

import java.io.File;
import java.io.IOException;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBackedTaskManager() {
        File saveFile;
        try {
            saveFile = File.createTempFile("FileBackedTaskManagerSaveFile", null);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при работе с файлом сохранения.", e);
        }
        return FileBackedTaskManager.loadFromFile(saveFile);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
