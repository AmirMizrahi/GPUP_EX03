package DTO;

public class WorkerTargetDTO {
    private final int workersAmount;
    private final Double progress;
    private String targetName;
    private String taskName;
    private String status;
    private final String taskType;

    public WorkerTargetDTO(String targetName, String taskName, String status, String taskType, Double progress, int workersAmount) {
        this.targetName = targetName;
        this.taskName = taskName;
        this.status = status;
        this.taskType = taskType;
        this.workersAmount = workersAmount;
        this.progress = progress;
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

    public int getWorkersAmount() {
        return workersAmount;
    }

    public Double getProgress() {
        return progress;
    }
}
