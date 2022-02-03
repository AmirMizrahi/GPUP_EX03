package managers;

import DTO.TargetDTO;
import DTO.TaskDTOForWorker;
import DTO.UserDTO;
import User.User;
import targets.Target;
import tasks.AbstractTask;
import tasks.Task;

import java.util.*;

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

    public List<TaskDTOForWorker> tasksForWorker(String userName, int availableThreads) {
        List<TaskDTOForWorker> taskList = new LinkedList<>();
        int currentTargetsAmount = 0;
        boolean gotMaxTargets = false, gotAtLeastOneTarget = true;

        while(!gotMaxTargets && gotAtLeastOneTarget) {
            gotAtLeastOneTarget = false;
            for (Map.Entry<String, Task> entry : taskNameToTask.entrySet()) {
                if (entry.getValue().isUserSubscribed(userName)) {
                    Target t = entry.getValue().getTargetForWorker();
                    if (t != null) {
                        taskList.add(new TaskDTOForWorker(new TargetDTO(t), entry.getValue().getTaskType(), entry.getValue().getTaskInfo()));
                        currentTargetsAmount++;
                        gotAtLeastOneTarget = true;
                    }
                    if(currentTargetsAmount == availableThreads) {
                        gotMaxTargets = true;
                        break;
                    }
                }
            }
        }
        return taskList;
    }



}
