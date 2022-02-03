package managers;

import DTO.TaskDTO;
import DTO.TaskDTOForWorker;
import targets.WorkerCompilationTarget;
import targets.WorkerSimulationTarget;
import targets.WorkerTarget;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerManager {
    private final Integer threadsAmount;
    private List<TaskDTO> subscribedTasks = new LinkedList<>();
    private Integer threadsOnWork = 0; //todo need to updated
    private ThreadPoolExecutor threadExecutor;
    private List<WorkerTarget> targetsOnWork;

    public WorkerManager(/*Integer threadsAmount*/) {
        this.threadsAmount = 5/*threadsAmount*/;//todo need to change 5
        this.threadExecutor = new ThreadPoolExecutor(this.threadsAmount, this.threadsAmount, 1, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        targetsOnWork = new LinkedList<>();
    }

    public void addSubscriber(TaskDTO newSubscribedTask) {
        subscribedTasks.add(newSubscribedTask);
    }

    public void updateSubscribedTask(List<TaskDTO> taskDTOS) {
        for (TaskDTO newTask : taskDTOS) {
            for (TaskDTO oldTask : subscribedTasks) {
                if (oldTask.getTaskName().compareTo(newTask.getTaskName()) == 0) {
                    subscribedTasks.remove(oldTask);
                    subscribedTasks.add(newTask);
                }
            }
        }
    }

    public boolean isThereWorkToDo() {
        final boolean[] res = {false};
        subscribedTasks.forEach(subscribedTask -> {
            if (subscribedTask.getTaskStatus().compareTo("PLAY") == 0) {
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

    public void addTargetToThreadPool(TaskDTOForWorker dto) throws InterruptedException {
        if (dto.getTaskType().compareToIgnoreCase("simulation") == 0) {
            WorkerSimulationTarget simulationTarget = new WorkerSimulationTarget(dto, dto.getTaskInfo());
            targetsOnWork.add(simulationTarget);
            this.threadExecutor.execute(makeRunnable(simulationTarget));
        } else {
            WorkerCompilationTarget compilationTarget = new WorkerCompilationTarget(dto, dto.getTaskInfo());
            targetsOnWork.add(compilationTarget);
            this.threadExecutor.execute(makeRunnable(compilationTarget));
        }
    }

    private Runnable makeRunnable(WorkerTarget target) {
        return () -> {
            try {
                target.run();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };
    }

    public boolean areThereTargetsToSend() {
        boolean res = false;
        for (WorkerTarget workerTarget : targetsOnWork) {
            if (workerTarget.isRunFinished()) {
                res = true;
                break;
            }
        }
        return res;
    }

    public List<Map<String,String>> getUpdatedTargetsResults(){
        List<Map<String,String>> resultsList = new LinkedList<>();

        for (WorkerTarget workerTarget : targetsOnWork) {
            if(workerTarget.isRunFinished()){
                resultsList.add(workerTarget.getResult());
            }
        }

        for (Map<String, String> stringStringMap : resultsList) {
            this.targetsOnWork.remove(stringStringMap);
        }
        return resultsList;
    }
}