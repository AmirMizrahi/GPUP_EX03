package managers;

import DTO.TaskDTO;
import DTO.TaskDTOForWorker;
import targets.WorkerCompilationTarget;
import targets.WorkerSimulationTarget;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerManager {
    private final Integer threadsAmount;
    private List<TaskDTO> subscribedTasks = new LinkedList<>();
    private Integer threadsOnWork = 0; //todo need to updated
    private ThreadPoolExecutor threadExecutor;

    public WorkerManager(/*Integer threadsAmount*/) {
        this.threadsAmount = 5/*threadsAmount*/;//todo need to change 5
        this.threadExecutor = new ThreadPoolExecutor(this.threadsAmount,this.threadsAmount,1, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
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

    public void setThreadsOnWork(Integer threadsOnWork) {
        this.threadsOnWork += threadsOnWork;
    }

    public Integer getAvailableThreadsAmount() {
        return this.threadsAmount - this.threadsOnWork;
    }

    public void addTargetToThreadPool(TaskDTOForWorker dto){
        if (dto.getTaskType().compareToIgnoreCase("simulation") == 0) {
            WorkerSimulationTarget simulationTarget = new WorkerSimulationTarget(dto.getTargetDTO().getTargetName(), dto.getTaskInfo());
            this.threadExecutor.execute(simulationTarget.run());
        }
        else {
            WorkerCompilationTarget compilationTarget = new WorkerCompilationTarget(dto.getTargetDTO().getTargetName(), dto.getTaskInfo());
            this.threadExecutor.execute(compilationTarget.run());
        }
    }
}
