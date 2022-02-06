package DTO;

public class WorkerTargetDTO {
    private String targetName;
    private String taskName;
    private String status;
    private final String taskType;

    public WorkerTargetDTO(String targetName, String taskName, String status, String taskType) {
        this.targetName = targetName;
        this.taskName = taskName;
        this.status = status;
        this.taskType = taskType;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getStatus() {
        return status;
    }

    public String getTaskType() {
        return taskType;
    }
}
