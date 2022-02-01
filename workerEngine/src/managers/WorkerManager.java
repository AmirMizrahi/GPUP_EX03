package managers;

import DTO.TaskDTO;

import java.util.LinkedList;
import java.util.List;

public class WorkerManager {
    private final Integer threadsAmount;
    private List<TaskDTO> subscribedTasks = new LinkedList<>();
    private Integer threadsOnWork = 0; //todo need to updated

    public WorkerManager(Integer threadsAmount) {
        this.threadsAmount = threadsAmount;
    }

    public void addSubscriber(TaskDTO newSubscribedTask) {
        subscribedTasks.add(newSubscribedTask);
    }

    public void updateSubscribedTask(List<TaskDTO> taskDTOS) {
        for (TaskDTO newTask : taskDTOS) {
            for (TaskDTO oldTask : subscribedTasks) {
                if(oldTask.getTaskName().compareTo(newTask.getTaskName()) == 0) {
                    subscribedTasks.remove(oldTask);
                    subscribedTasks.add(newTask);
                }
            }
        }
    }

    public boolean isThereWorkToDo() {
        final boolean[] res = {false};
        subscribedTasks.forEach(subscribedTask -> {
            if(subscribedTask.getTaskStatus().compareTo("PLAY") == 0){
                res[0] = true;
            }
        });
        boolean a = res[0];
        return a;
    }

    public Integer getAvailableThreadsAmount() {
        return this.threadsAmount - this.threadsOnWork;
    }
}
