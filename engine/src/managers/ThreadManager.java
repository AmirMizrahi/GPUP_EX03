package managers;

import bridges.PrinterBridge;
import exceptions.TargetNotFoundException;
import graph.Graph;
import graph.SerialSet;
import targets.Target;
import tasks.SimulationTask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

public class ThreadManager {
    private final Object keyForMainExecute;
    private final Object keyForSerialSet;
    private final List<Target> targetList;
    private final ThreadPoolExecutor threadPool;
    private final PrinterBridge printerBridge;
    private final List<Consumer<String>> consumerList;
    private final Task task;
    private Map<String,List<SerialSet>> targetToListOfSerialSets;
    private Object isPaused;
    private final Graph graph;

    public ThreadManager(Object keyForMainExecute, List<Target> targetList, ThreadPoolExecutor threadPool, List<Consumer<String>> consumerList,
                         Task task, Map<String, List<SerialSet>> targetToListOfSerialSets, Object isPaused, Graph graph) {
        this.keyForMainExecute = keyForMainExecute;
        this.keyForSerialSet = new Object();
        this.targetList = targetList;
        this.threadPool = threadPool;
        this.printerBridge = new PrinterBridge();
        this.consumerList = consumerList;
        this.task = task;
        this.targetToListOfSerialSets = targetToListOfSerialSets;
        this.isPaused = isPaused;
        this.graph = graph;
    }

    public void onFinishedTarget(Target target,List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished) throws TargetNotFoundException {
        setStatusAfterTaskForAllEffected(target,this.targetList,consumerList, consumeWhenFinished);
        if (isJobDone()){
            synchronized (keyForMainExecute) {
                keyForMainExecute.notifyAll();
            }
        }
    }

    private boolean isJobDone() {
        boolean res = true;
        for (Target t:  this.targetList) {
            if (t.getStatus() != Target.TargetStatus.SKIPPED && t.getStatus() != Target.TargetStatus.FINISHED)
                res = false;
        }
        return res;
    }

    private void setStatusAfterTaskForAllEffected(Target target, List<Target> targets,List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished) throws TargetNotFoundException {
        if(target.getStatusAfterTask() == Target.StatusAfterTask.FAILURE) {
            Map<Target, DFS_COLOR> M = new HashMap<>();
            for (Target t: targets) {
                M.put(t, DFS_COLOR.WHITE);
            }
            skippedDFS(target, target, M,consumerList);
            //Target B is now Skipped.
        }
        else//Success chain
            maybeWaitingChain(target,consumerList, consumeWhenFinished);
        //Target B is now Waiting.
    }

    //TargetStatus = WAITING, IN_PROCESS, SKIPPED, FROZEN, FINISHED --> NEVER DELETE ME!
    private void skippedDFS(Target target, Target origin, Map<Target, DFS_COLOR> M,List<Consumer<String>> consumerList) throws TargetNotFoundException {
        M.replace(target, DFS_COLOR.GREY); // to avoid cycle

        if(!target.equals(origin)){
            target.setStatus(Target.TargetStatus.SKIPPED);
            this.printerBridge.printStringToFileWithTimeStamp(String.format("Target %s is SKIPPED", target.getName()),consumerList);
        }
        for (String neighborName:target.getInTargets()) {
            Target neighbor = this.graph.getTargetByName(neighborName);
            if (M.get(neighbor) == DFS_COLOR.WHITE)
                skippedDFS(neighbor, origin, M,consumerList);
        }
        M.replace(target, DFS_COLOR.BLACK);
    }

    private void maybeWaitingChain(Target target,List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished) throws TargetNotFoundException {
        boolean allMyOutAreSuccess = true;
        for (String currentName : target.getInTargets()) {
            Target current = this.graph.getTargetByName(currentName);
            if (current.getStatus() == Target.TargetStatus.FROZEN) {
                for (String outTargetName : current.getOutTargets()) {
                    Target outTarget = this.graph.getTargetByName(outTargetName);
                    if (outTarget.getStatusAfterTask() != Target.StatusAfterTask.SUCCESS && outTarget.getStatusAfterTask() != Target.StatusAfterTask.WARNING) {
                        allMyOutAreSuccess = false;
                        break;
                    }
                }
                if (allMyOutAreSuccess) {
                    synchronized (current) {
                        current.setStatus(Target.TargetStatus.WAITING);
                        this.printerBridge.printStringToFileWithTimeStamp(String.format("Target %s is WAITING", current.getName()), consumerList);
                        this.threadPool.execute(this.makeRunnable(current,isPaused, consumeWhenFinished));
                    }
                }

                allMyOutAreSuccess = true;
            }
        }
    }

    public Runnable makeRunnable(Target t, Object isPaused, Consumer<File> consumeWhenFinished){


        Runnable r = () -> {
            try {
                synchronized (isPaused){
                    while (Manager.isPauseOccurred()){
                        try {
                            isPaused.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                boolean flag = true;
                List<Consumer<String>> targetConsumers = new LinkedList<>();
                targetConsumers.add(this.consumerList.get(0));
                if(this.targetToListOfSerialSets.get(t.getName()).size() != 0)
                {    //If t don't have a List<Serialset>, don't check for a serial set :)
                        synchronized (keyForSerialSet) {
                            if (this.isAllSerialSetsOfTargetAreClosed(t))
                                this.targetToListOfSerialSets.get(t.getName()).forEach(x -> x.setInProcess(true)); //Set all Serialsets which includes t to TRUE
                            else {
                                threadPool.execute(makeRunnable(t,isPaused, consumeWhenFinished));
                                flag = false;
                            }
                        }
                        if(flag) {
                            System.out.println("Thread name: " + Thread.currentThread().getName() + " Target: " + t.getName() + " 5555555555555555555555555555555555555555555555 start");//need to delete
                            this.task.runTask(targetConsumers,consumeWhenFinished, t, targetList);
                            this.onFinishedTarget(t, targetConsumers, consumeWhenFinished);
                            this.targetToListOfSerialSets.get(t.getName()).forEach(x -> x.setInProcess(false)); //Set all Serialsets which includes t to FALSE (after run)
                            System.out.println("Thread name: " + Thread.currentThread().getName() + " Target: " + t.getName() + " 5555555555555555555555555555555555555555555555 end");//need to delete

                        }
                }
                else
                {
                    System.out.println("Thread name: " + Thread.currentThread().getName() + " Target: " + t.getName() + " 5555555555555555555555555555555555555555555555 start");//need to delete
                    this.task.runTask(targetConsumers,consumeWhenFinished, t, targetList);
                    this.onFinishedTarget(t, targetConsumers, consumeWhenFinished);
                    System.out.println("Thread name: " + Thread.currentThread().getName() + " Target: " + t.getName() + " 5555555555555555555555555555555555555555555555 end");//need to delete
                }
            }
            catch (InterruptedException | IOException | TargetNotFoundException ignored) {}
        };
        return r;
    }

    private boolean isAllSerialSetsOfTargetAreClosed(Target t){
        boolean returnedRes = true;

        for (SerialSet set : targetToListOfSerialSets.get(t.getName())) {
            if(set.isInProcess()){
                returnedRes = false;
                break;
            }
        }
        return returnedRes;
    }

    public void setNewThreadPoolAmountTM(Integer value) {
        this.threadPool.setCorePoolSize(value);
        this.threadPool.setMaximumPoolSize(value);
    }

    public enum DFS_COLOR{
        WHITE, GREY, BLACK
    }
}
