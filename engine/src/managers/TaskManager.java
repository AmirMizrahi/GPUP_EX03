package managers;

import DTO.TargetDTO;
import DTO.TargetDTOForWorker;
import targets.Target;
import tasks.AbstractTask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

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

    public List<TargetDTOForWorker> getTasksForWorker(String userName, int availableThreads) throws IOException, InterruptedException {
        List<TargetDTOForWorker> taskList = new LinkedList<>();
        int currentTargetsAmount = 0;
        boolean gotMaxTargets = false, gotAtLeastOneTarget = true;

        while(!gotMaxTargets && gotAtLeastOneTarget) {
            gotAtLeastOneTarget = false;
            for (Map.Entry<String, Task> entry : taskNameToTask.entrySet()) {
                if(currentTargetsAmount == availableThreads) {
                    gotMaxTargets = true;
                    break;
                }
                if (entry.getValue().isUserSubscribed(userName) && entry.getValue().getStatus() == AbstractTask.TASK_STATUS.PLAY &&
                        !entry.getValue().isUserPaused(userName)) {
                    Target t = entry.getValue().getTargetForWorker();
                    if (t != null) {
                       // List<Consumer<String>> bla = new LinkedList<>();
                        entry.getValue().printBeforeProcess(new LinkedList<>() ,t);
                        taskList.add(new TargetDTOForWorker(entry.getKey(), new TargetDTO(t), entry.getValue().getTaskType(),
                                entry.getValue().getTaskInfo(), entry.getValue().getProgress(), entry.getValue().getWorkersAmount()));
                        t.setStatus(Target.TargetStatus.IN_PROCESS);
                        currentTargetsAmount++;
                        gotAtLeastOneTarget = true;
                    }
                }
            }
        }
        return taskList;
    }

    public void removeSubscriberFromTask(String userName, String taskName) {
        for (Map.Entry<String, Task> entry : taskNameToTask.entrySet()) {
            if (entry.getKey().compareTo(taskName) == 0) {
                entry.getValue().removeSub(userName);
            }
        }
    }

    public void updatePauseFromWorker(String userName, String taskName, Boolean isPauseSelected) {
        for (Map.Entry<String, Task> entry : taskNameToTask.entrySet()) {
            if (entry.getKey().compareTo(taskName) == 0) {
                entry.getValue().updatePauseResume(userName, isPauseSelected);
            }
        }
    }
}
