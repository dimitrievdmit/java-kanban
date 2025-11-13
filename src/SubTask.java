import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String title, String description, TaskStatus taskStatus, int epicId) {
        super(title, description, taskStatus);
        this.epicId = epicId;
    }

    public SubTask(String title, String description, TaskStatus taskStatus, int epicId, Duration duration, LocalDateTime startTime) {
        super(title, description, taskStatus, duration, startTime);
        this.epicId = epicId;
    }

    public SubTask(int taskId, String title, String description, TaskStatus taskStatus, int epicId) {
        super(taskId, title, description, taskStatus);
        if (epicId == taskId) return;
        this.epicId = epicId;
    }

    public SubTask(int taskId, String title, String description, TaskStatus taskStatus, int epicId, Duration duration, LocalDateTime startTime) {
        super(taskId, title, description, taskStatus, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId == taskId) return;
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "\n    title='" + title + '\'' +
                ", \n    description='" + description + '\'' +
                ", \n    taskId=" + taskId +
                ", \n    taskStatus=" + taskStatus +
                "\n    epicId=" + epicId +
                "\n}";
    }

    public String toCsvString() {
        String epicIdStr = String.valueOf(epicId);
        return String.format(
                "%s,%s,%s,%s,%s,%s,%s,%s,",
                taskId,
                TaskType.SUBTASK,
                title,
                taskStatus,
                description,
                epicIdStr,
                duration == null ? "" : duration.toMinutes(),
                startTime == null ? "" : startTime
        );
    }
}
