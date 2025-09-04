import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<SubTask> subTasks = new ArrayList<>();

    public Epic(int taskId, String title, String description, TaskStatus taskStatus, ArrayList<SubTask> subTasks) {
        super(taskId, title, description, taskStatus);
        this.subTasks = subTasks;
    }

    public ArrayList<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(ArrayList<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    public void updateEpicStatus () {
        if (subTasks.isEmpty()) {
            setTaskStatus(TaskStatus.NEW);
            return;
        }

        boolean isAllNew = true;
        boolean isAllDone = true;

        for (SubTask subTask : subTasks) {
            if (subTask.getTaskStatus() != TaskStatus.NEW) {
                isAllNew = false;
            }
            if (subTask.getTaskStatus() != TaskStatus.DONE) {
                isAllDone = false;
            }
            if (!isAllNew && !isAllDone) break;
        }

        if (isAllNew) {
            setTaskStatus(TaskStatus.NEW);
            return;
        }
        if (isAllDone) {
            setTaskStatus(TaskStatus.DONE);
            return;
        }
        setTaskStatus(TaskStatus.IN_PROGRESS);
    }

}
