package DTO;

import java.util.Map;

public class TargetDTOForWorker {
    private String taskName;
    private TargetDTO targetDTO;
    private String taskType;
    private Map<String, String> taskInfo;

    public TargetDTOForWorker(String taskName, TargetDTO target, String taskType, Map<String, String> taskInfo){
        this.taskName = taskName;
        this.targetDTO = target;
        this.taskType = taskType;
        this.taskInfo = taskInfo;
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
}
