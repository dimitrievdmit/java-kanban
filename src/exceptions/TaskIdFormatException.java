package exceptions;

public class TaskIdFormatException extends Exception {
    public TaskIdFormatException() {
        super("Некорректный идентификатор задачи.");
    }
}
