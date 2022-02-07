package DTO;

import java.util.Map;

public class TargetDTOForWorker {
    private final int workersAmount;
    private final String taskName;
    private final TargetDTO targetDTO;
    private final String taskType;
    private final Map<String, String> taskInfo;
    private final Double progress;

    public TargetDTOForWorker(String taskName, TargetDTO target, String taskType, Map<String, String> taskInfo, Double progress, int workersAmount){
        this.taskName = taskName;
        this.targetDTO = target;
        this.taskType = taskType;
        this.taskInfo = taskInfo;
        this.progress = progress;
        this.workersAmount = workersAmount;
    }

    public String getTaskName() {
        return taskName;
    }

    public TargetDTO getTargetDTO() {
        return targetDTO;
    }

    public String getTaskType() {
        return taskType;
    }

    public Map<String, String> getTaskInfo() {
        return taskInfo;
    }

    public Double getProgress() {
        return progress;
    }

    public int getWorkersAmount() {
        return workersAmount;
    }
}
