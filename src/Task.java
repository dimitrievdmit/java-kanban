import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected String title;
    protected String description;
    protected int taskId;
    protected TaskStatus taskStatus;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String title, String description, TaskStatus taskStatus) {
        this.title = title;
        this.description = description;
        this.taskStatus = taskStatus;
    }

    public Task(String title, String description, TaskStatus taskStatus, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.taskStatus = taskStatus;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(int taskId, String title, String description, TaskStatus taskStatus) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.taskStatus = taskStatus;
    }

    public Task(int taskId, String title, String description, TaskStatus taskStatus, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.duration = duration;
        this.startTime = startTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (this.getStartTime() == null || this.getDuration() == null) return null;
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return getTaskId() == task.getTaskId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTaskId());
    }

    @Override
    public String toString() {
        return "Task{" +
                "\n    title='" + title + '\'' +
                ", \n    description='" + description + '\'' +
                ", \n    taskId=" + taskId +
                ", \n    taskStatus=" + taskStatus +
                "\n}";
    }

    public String toCsvString() {
        return String.format(
                "%s,%s,%s,%s,%s,,%s,%s,",
                taskId,
                TaskType.TASK,
                title,
                taskStatus,
                description,
                duration == null ? "" : duration.toMinutes(),
                startTime == null ? "" : startTime
        );
    }

    public boolean tasksIntersect(Task otherTask) {
        if (this.getEndTime() == null) return false;
        if (otherTask == null || otherTask.getEndTime() == null) return false;

        if (this.getStartTime().isEqual(otherTask.getStartTime())) return true;
        if (this.getEndTime().isEqual(otherTask.getEndTime())) return true;
        if (
                this.getStartTime().isAfter(otherTask.getStartTime())
                        && this.getStartTime().isBefore(otherTask.getEndTime())
        ) {
            return true;
        }
        return this.getEndTime().isAfter(otherTask.getStartTime()) && this.getEndTime().isBefore(otherTask.getEndTime());
    }
}

