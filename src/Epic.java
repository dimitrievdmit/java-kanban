import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTaskIds = new ArrayList<>();

    public Epic(String title, String description, TaskStatus taskStatus) {
        super(title, description, taskStatus);
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTaskId(Integer subTaskId) {
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
