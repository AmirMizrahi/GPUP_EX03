package task;

import DTO.TargetDTO;
import DTO.TaskDTO;
import DTO.UserDTO;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class WorkerTask {
    private final String taskName;
    private final String uploaderName;
    //private int taskTotalPrice;
    //private int currentWorkersAmount;
    private final String graphName;
    private String taskStatus;

    public WorkerTask(TaskDTO dto) {
        this.taskName = dto.getTaskName();
        this.uploaderName = dto.getUploaderName();
        this.graphName = dto.getGraphName();
        this.taskStatus = dto.getTaskStatus();
    }

    public String getTaskName() {
        return this.taskName;
    }

    public String getUploaderName() {
        return this.uploaderName;
    }

    public String getGraphName() {
        return this.graphName;
    }

    public String getTaskStatus() {return this.taskStatus;}

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
}
