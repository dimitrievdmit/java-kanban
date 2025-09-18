import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subTaskIds = new ArrayList<>();

    public Epic(String title, String description, TaskStatus taskStatus) {
        super(title, description, taskStatus);
    }

    public Epic(int taskId, String title, String description, TaskStatus taskStatus) {
        super(taskId, title, description, taskStatus);
    }

    public Epic(int taskId, String title, String description, TaskStatus taskStatus, List<Integer> subTaskIds) {
        super(taskId, title, description, taskStatus);
        this.subTaskIds = subTaskIds;
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
}
