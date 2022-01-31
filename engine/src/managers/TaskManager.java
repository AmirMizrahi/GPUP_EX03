package managers;

import DTO.UserDTO;
import tasks.AbstractTask;
import tasks.Task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TaskManager {
    private final Map<String, Task> taskNameToTask;
    private Set<String> taskNameEntered; //Will use it to to synchronize part when creating new task

    public TaskManager() {
        taskNameToTask = new HashMap<>();
    }

    public synchronized void addTask(String taskName, Task task) {
        taskNameToTask.put(taskName, task);
    }

//    public synchronized void removeUser(String username) {
    //      graphNameToGraph.remove(username);
    //  }

    public synchronized Map<String,Task> getTasks() {
        return Collections.unmodifiableMap(taskNameToTask);
    }

    public boolean isTaskExists(String taskName) {
        return taskNameToTask.containsKey(taskName);
    }

    public void updateTaskStatus(String taskName, AbstractTask.TASK_STATUS taskStatus) {
        for (Map.Entry<String, Task> entry : taskNameToTask.entrySet()) {
            if(entry.getKey().compareTo(taskName) == 0){
                entry.getValue().setStatus(taskStatus);
                break;
            }
        }
    }
}
