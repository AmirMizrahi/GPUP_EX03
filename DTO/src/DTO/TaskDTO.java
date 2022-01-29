package DTO;

import tasks.AbstractTask;
import tasks.Task;

public class TaskDTO {
    private final AbstractTask task;
    private final String taskName;

    public TaskDTO(String taskName, AbstractTask task){
        this.taskName = taskName;
        this.task = task;
    }

    public String getTaskName(){
        return this.taskName;
    }

    public String getUploaderName(){
        return this.task.getUploaderName();
    }

}
