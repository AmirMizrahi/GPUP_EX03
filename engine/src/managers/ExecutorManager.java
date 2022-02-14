package managers;

import graph.*;
import targets.Target;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ExecutorManager {
    private final Task task;
    private final List<Target> targetList;
    private final List<Consumer<String>> consumerList;
    private final int threadsAmount;
    private final Map<String,List<SerialSet>> targetToListOfSerialSets;
    private final Object isPaused;
    private ThreadManager threadManager;
    private Consumer<File> consumeWhenFinished;
    private ThreadPoolExecutor threadExecutor;
    private int maxThreads;
    private final Graph graph;

    public ExecutorManager(Task task, List<Target> targetList, List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished,
                           Map<String,List<SerialSet>> targetToListOfSerialSets, int threadsAmount, int maxThreads, Object isPaused, Graph graph) {
        this.task = task;
        this.targetList = targetList;
        this.consumerList = consumerList;
        this.threadsAmount = threadsAmount;
        this.targetToListOfSerialSets = targetToListOfSerialSets;
        this.isPaused = isPaused;
        this.consumeWhenFinished = consumeWhenFinished;
        this.maxThreads = maxThreads;
        this.graph = graph;
    }

    public void execute() throws InterruptedException, IOException {

        //this.threadExecutor = Executors.newFixedThreadPool(this.maxParallelism);
        this.threadExecutor = new ThreadPoolExecutor(this.threadsAmount,maxThreads,1, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        Object keyForMainExecute = new Object();
        threadManager = new ThreadManager(keyForMainExecute,this.targetList,threadExecutor,this.consumerList,this.task
                , this.targetToListOfSerialSets, isPaused, graph);

        synchronized (keyForMainExecute) {
            for (Target target : this.targetList) {
                if (target.getStatus() == Target.TargetStatus.WAITING){
                    threadExecutor.execute(threadManager.makeRunnable(target,isPaused, consumeWhenFinished));
                }
            }
            keyForMainExecute.wait();
        }

       // this.task.doWhenTaskIsFinished(this.targetList,this.consumerList, consumeWhenFinished);
        threadExecutor.shutdown();

/*      String summaryLogFilePath = createSummaryFile(SimulationFileFullName);     WE HAVE THE SIMULATION FILE FULL NAME IN THE SIMULATION TASK
        Consumer<String> consumer = consumerBuilder(summaryLogFilePath);
        consumerList.add(consumer);
        printToSummaryLogFile(consumerList,totalRunningTime, relevantTargetsForSummaryLogFile, targetsToRunningTime, skippedTargetsForSummaryLogFile);

        else if (current.getStatus == Status.SKIPPED)
            addToSkippedList.add(current);*/
        //return targetList;
    }

    public void setNewThreadPoolAmount(Integer value) {
        this.threadExecutor.setCorePoolSize(value);
        this.threadExecutor.setMaximumPoolSize(value);
        this.threadManager.setNewThreadPoolAmountTM(value);
    }


/*    private void onFinishedTarget(Target target){
        if(target.getStatusAfterTask() == Target.StatusAfterTask.FAILURE) {
            Map<Target, SimulationTask.DFS_COLOR> M = new HashMap<>();
            for (Target t: targetList) {
                M.put(t, SimulationTask.DFS_COLOR.WHITE);
            }
            skippedDFS(target, target, M, currentTargetFilePath,consumerList);
            //Target B is now Skipped.
        }
        else//Success chain
            maybeWaitingChain(target, currentTargetFilePath,consumerList);
        //Target B is now Waiting.
    }*/

    /*    private Boolean isWorkDone(){
        Boolean res = true;
        for (Target target:this.targetList) {
            if (!(target.getStatus() == Target.TargetStatus.FINISHED) || !(target.getStatus() == Target.TargetStatus.SKIPPED)){
                res = false;
                break;
            }
        }
        return res;
    }*/

}
