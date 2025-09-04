public class SubTask extends Task {
    private int epicId;

    public SubTask(int taskId, String title, String description, TaskStatus taskStatus, int epicId) {
        super(taskId, title, description, taskStatus);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }
}
