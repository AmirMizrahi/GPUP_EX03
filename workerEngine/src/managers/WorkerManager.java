package managers;

import DTO.TaskDTO;
import DTO.TargetDTOForWorker;
import DTO.WorkerTargetDTO;
import targets.WorkerCompilationTarget;
import targets.WorkerSimulationTarget;
import targets.WorkerTarget;
import task.WorkerTask;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerManager {
    private final Integer threadsAmount;
    private List<WorkerTask> subscribedTasks = new LinkedList<>();
    private Integer threadsOnWork = 0;
    private ThreadPoolExecutor threadExecutor;
    private List<WorkerTarget> targetsOnWork;
    private TargetManager targetManager;
    private Object dummy = new Object();

    public WorkerManager(Integer threadsAmount) {
        this.threadsAmount = threadsAmount;
        this.threadExecutor = new ThreadPoolExecutor(this.threadsAmount, this.threadsAmount, 1, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        targetsOnWork = new LinkedList<>();
        targetManager = new TargetManager();
    }

    public void addSubscriber(TaskDTO newSubscribedTask) {
        subscribedTasks.add(new WorkerTask(newSubscribedTask));
    }

    public void updateSubscribedTask(List<TaskDTO> taskDTOS) {
        for (TaskDTO newTask : taskDTOS) {
            for (WorkerTask oldTask : subscribedTasks) {
                if (oldTask.getTaskName().compareTo(newTask.getTaskName()) == 0) {
                    oldTask.setTaskStatus(newTask.getTaskStatus());
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
        synchronized (dummy) {
            this.threadsOnWork = this.threadsOnWork + threadsOnWork;
            System.out.println("--------------------------------------------Threads on work: " + (this.threadsOnWork));
        }
    }

    public Integer getAvailableThreadsAmount() {
        synchronized (dummy) {
            System.out.println("--------------------------------------------Threads request: " + (this.threadsAmount - this.threadsOnWork));
            return this.threadsAmount - this.threadsOnWork;
        }
    }

    public void addTargetToThreadPool(TargetDTOForWorker dto) throws InterruptedException {
        synchronized (dummy) {
            if (dto.getTaskType().compareToIgnoreCase("simulation") == 0) {
                WorkerSimulationTarget simulationTarget = new WorkerSimulationTarget(dto, dto.getTaskInfo());
                targetsOnWork.add(simulationTarget);
                targetManager.addTarget(simulationTarget);
                this.threadExecutor.execute(makeRunnable(simulationTarget));
            } else {
                WorkerCompilationTarget compilationTarget = new WorkerCompilationTarget(dto, dto.getTaskInfo());
                targetsOnWork.add(compilationTarget);
                targetManager.addTarget(compilationTarget);
                this.threadExecutor.execute(makeRunnable(compilationTarget));
            }
        }
    }

    private Runnable makeRunnable(WorkerTarget target) {
        return () -> {
            try {
                target.run();
                synchronized (dummy) {
                    threadsOnWork--;
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };
    }

    public boolean areThereTargetsToSend() {
        synchronized (dummy) {
            boolean res = false;
            for (WorkerTarget workerTarget : targetsOnWork) {
                if (workerTarget.isRunFinished()) {
                    res = true;
                    break;
                }
            }
            return res;
        }
    }

    public List<Map<String,String>> getUpdatedTargetsResults(){
        synchronized (dummy) {
            List<Map<String, String>> resultsList = new LinkedList<>();
            for (WorkerTarget workerTarget : targetsOnWork) {
                if (workerTarget.isRunFinished()) {
                    resultsList.add(workerTarget.getResult());
                }
            }
            for (Map<String, String> stringStringMap : resultsList) {
                this.targetsOnWork.remove(stringStringMap);
            }
            return resultsList;
        }
    }

    public List<WorkerTargetDTO> getAllTargets() {
        synchronized (dummy) {
            List<WorkerTargetDTO> toReturn = new LinkedList<>();
            List<WorkerTarget> list = targetManager.getAllTargets();

            list.forEach(target -> toReturn.add(new WorkerTargetDTO(target.getResult().get("targetName"),
                    target.getResult().get("taskName"),
                    target.getResult().get("status"),
                    target.getResult().get("taskType"),
                    target.getProgress(),
                    target.getWorkersAmount())));

            return toReturn;
        }
    }

    public Integer getThreadsAmount() {
        synchronized (dummy) {
            return threadsAmount;
        }
    }

    public Integer getThreadsOnWork() {
        synchronized (dummy) {
            return threadsOnWork;
        }
    }

}