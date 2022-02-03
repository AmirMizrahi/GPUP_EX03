package DTO;

import java.util.Map;

public class TaskDTOForWorker {
    private TargetDTO targetDTO;
    private String taskType;
    private Map<String, String> taskInfo;

    public TaskDTOForWorker(TargetDTO target, String taskType, Map<String, String> taskInfo){
        this.targetDTO = target;
        this.taskType = taskType;
        this.taskInfo = taskInfo;
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
