package schemas.tasks;

import schemas.enums.TaskStatus;
import schemas.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subTaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String title, String description, TaskStatus taskStatus) {
        super(title, description, taskStatus);
    }

    public Epic(String title, String description, TaskStatus taskStatus, Duration duration, LocalDateTime startTime) {
        super(title, description, taskStatus, duration, startTime);
    }

    public Epic(int taskId, String title, String description, TaskStatus taskStatus) {
        super(taskId, title, description, taskStatus);
    }

    public Epic(int taskId, String title, String description, TaskStatus taskStatus, List<Integer> subTaskIds) {
        super(taskId, title, description, taskStatus);
        this.subTaskIds = subTaskIds;
    }

    public Epic(int taskId, String title, String description, TaskStatus taskStatus, Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
        super(taskId, title, description, taskStatus, duration, startTime);
        this.endTime = endTime;
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTaskId(Integer subTaskId) {
        if (subTaskId == taskId) return;
        subTaskIds.add(subTaskId);
    }

    public void removeSubTaskId(Integer subTaskId) {
        subTaskIds.remove(subTaskId);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "\n    title='" + title + '\'' +
                ", \n    description='" + description + '\'' +
                ", \n    taskId=" + taskId +
                ", \n    taskStatus=" + taskStatus +
                "\n    subTaskCount=" + subTaskIds.size() +
                "\n}";
    }

    public String toCsvString() {
        return String.format(
                "%s,%s,%s,%s,%s,,%s,%s,%s",
                taskId,
                TaskType.EPIC,
                title,
                taskStatus,
                description,
                duration == null ? "" : duration.toMinutes(),
                startTime == null ? "" : startTime,
                endTime == null ? "" : endTime
        );
    }
}
