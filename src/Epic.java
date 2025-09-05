import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();

    public Epic(int taskId, String title, String description, TaskStatus taskStatus, HashMap<Integer, SubTask> subTasks) {
        super(taskId, title, description, taskStatus);
        this.subTasks = subTasks;
    }

    public Epic(int taskId, String title, String description, TaskStatus taskStatus) {
        super(taskId, title, description, taskStatus);
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(HashMap<Integer, SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    public void updateEpicStatus() {
        if (subTasks.isEmpty()) {
            setTaskStatus(TaskStatus.NEW);
            return;
        }

        boolean isAllNew = true;
        boolean isAllDone = true;

        for (SubTask subTask : subTasks.values()) {

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

    public void updateSubTask(int subTaskId, SubTask subTask) {
        subTasks.put(subTaskId, subTask);
        updateEpicStatus();
    }

    public void deleteSubTaskById(int subTaskId) {
        subTasks.remove(subTaskId);
        updateEpicStatus();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "\n    title='" + getTitle() + '\'' +
                ", \n    description='" + getDescription() + '\'' +
                ", \n    taskId=" + getTaskId() +
                ", \n    taskStatus=" + getTaskStatus() +
                "\n    subTaskCount=" + subTasks.size() +
                "\n}";
    }
}
