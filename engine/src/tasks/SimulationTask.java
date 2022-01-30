package tasks;

import targets.Target;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class SimulationTask extends AbstractTask implements Task{
    private final Integer taskTime;
    private final TIME_OPTION op;
    private final Double chanceToSucceed;
    private final Double succeedWithWarning;


    public SimulationTask(Integer taskTime, TIME_OPTION op, Double chanceToSucceed, Double succeedWithWarning, WAYS_TO_START_SIM_TASK way,
                          List<Target> targetsToRunOn, String pathName, boolean firstTime, String userName, String graphName) throws IllegalArgumentException {
        super(way, firstTime, targetsToRunOn, pathName, "Simulation", userName, graphName);
        this.taskTime = taskTime;
        this.op = op;
        this.chanceToSucceed = chanceToSucceed;
        this.succeedWithWarning = succeedWithWarning;
        //Need to change by user input
        if(this.taskTime <= 0 )
            throw new IllegalArgumentException("Error with the taskTime argument, taskTime need to be larger then 0.");
        if(this.chanceToSucceed < 0 || 1 < this.chanceToSucceed)
            throw  new IllegalArgumentException("Error with chanceToSucceed argument, chanceToSucceed need to be between 0 to 1.");
        if(this.succeedWithWarning < 0 || 1 < this.succeedWithWarning)
            throw  new IllegalArgumentException("Error with succeedWithWarning argument, succeedWithWarning need to be between 0 to 1.");
    }


/*    private void acceptListOfConsumers(List<Consumer<String>> list,String str) {
        list.forEach(consumer->consumer.accept(str));
    }*/

    @Override
    public void runTask(List<Consumer<String>> consumeImmediately,Consumer<File> consumeWhenFinished, Target current,List<Target> targetList) throws InterruptedException, IOException {
        String currentTargetFilePath;
        relevantTargetsForSummaryLogFile.add(current);
        File targetLogFile = new File(fileFullName + "\\" + current.getName() + ".log");
        targetLogFile.createNewFile();
        currentTargetFilePath = targetLogFile.getAbsolutePath();
        Consumer<String> consumer = consumerBuilder(currentTargetFilePath);/////////////
        consumeImmediately.add(consumer);/////////////////////////
        //need to print to the screen how much time I am going to sleep
        printNameAndStatus(current, orderOfProcess++,consumeImmediately);
        //need to print I am going to sleep and the current time
        current.setStatus(Target.TargetStatus.IN_PROCESS);

//        //
//        Target updatedTarget = this.currentStatusOfTargets.get(current.getName());
//        updatedTarget.setStatus(Target.TargetStatus.IN_PROCESS);
//        this.currentStatusOfTargets.put(current.getName(), updatedTarget);
//        //

        Integer sleepingTime = calculateSleepingTime();
        targetsToRunningTime.put(current,sleepingTime);//////////////////
        totalRunningTime += sleepingTime;///////////
        printDetailsBeforeSleep(sleepingTime,consumeImmediately);
        Thread.sleep(sleepingTime);
        setStatusAfterTask(current); //result of build!
        //setStatusAfterTaskForAllEffected(current, targetList, currentTargetFilePath,consumerList); //Change another targets if needed
        printDetailsAfterTask(current,consumeImmediately);

        synchronized (consumeWhenFinished) {
            consumeWhenFinished.accept(targetLogFile);
        }
        //need to print I have waked up and the current time
        //consumerList.remove(consumer);////////////////////
    }

    private void printDetailsBeforeSleep(Integer sleepingTime,List<Consumer<String>> consumerList){
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("New status: In Process"));
        this.printerBridge.acceptListOfConsumers(consumerList,  this.printerBridge.getStringWithTimeStampAttached(String.format("Going to sleep for %d milliseconds", sleepingTime)));
    }

/*    private void printStringToFileWithTimeStamp(String filePath, String toWrite,List<Consumer<String>> consumerList){
        acceptListOfConsumers(consumerList,getStringWithTimeStampAttached(toWrite));
    }*/



/*    private void setStatusAfterTaskForAllEffected(Target target, List<Target> targets, String currentTargetFilePath,List<Consumer<String>> consumerList) {
        if(target.getStatusAfterTask() == Target.StatusAfterTask.FAILURE) {
            Map<Target,DFS_COLOR> M = new HashMap<>();
            for (Target t: targets) {
                M.put(t,DFS_COLOR.WHITE);
            }
            skippedDFS(target, target, M, currentTargetFilePath,consumerList);
            //Target B is now Skipped.
        }
        else//Success chain
            maybeWaitingChain(target, currentTargetFilePath,consumerList);
        //Target B is now Waiting.
    }*/

/*    //TargetStatus = WAITING, IN_PROCESS, SKIPPED, FROZEN, FINISHED --> NEVER DELETE ME!
    private void skippedDFS(Target target, Target origin, Map<Target,DFS_COLOR> M, String currentTargetFilePath,List<Consumer<String>> consumerList){
        M.replace(target,DFS_COLOR.GREY); // to avoid cycle

        if(!target.equals(origin)){
            target.setStatus(Target.TargetStatus.SKIPPED);
            printStringToFileWithTimeStamp(currentTargetFilePath, String.format("Target %s is SKIPPED", target.getName()),consumerList);
        }
        for (Target neighbor:target.getInTargets()) {
            if (M.get(neighbor) == DFS_COLOR.WHITE)
                skippedDFS(neighbor, origin, M, currentTargetFilePath,consumerList);
        }
        M.replace(target,DFS_COLOR.BLACK);
    }*/

/*    private void maybeWaitingChain(Target target, String currentTargetFilePath,List<Consumer<String>> consumerList){
        boolean allMyOutAreSuccess = true;
        for (Target current: target.getInTargets()) {
            if (current.getStatus() == Target.TargetStatus.FROZEN) {
                for (Target outTarget : current.getOutTargets()) {
                    if (outTarget.getStatusAfterTask() != Target.StatusAfterTask.SUCCESS && outTarget.getStatusAfterTask() != Target.StatusAfterTask.WARNING) {
                        allMyOutAreSuccess = false;
                        break;
                    }
                }
                if (allMyOutAreSuccess)
                {
                    current.setStatus(Target.TargetStatus.WAITING);
                    printStringToFileWithTimeStamp(currentTargetFilePath, String.format("Target %s is WAITING", current.getName()),consumerList);
                }

                allMyOutAreSuccess = true;
            }
        }
    }*/

    private void setStatusAfterTask(Target target){
        Random number = new Random();
        if(number.nextDouble() <= this.chanceToSucceed)//Target Succeed
        {
            if(number.nextDouble() <= this.succeedWithWarning)//succeed
                target.setStatusAfterTask(Target.StatusAfterTask.WARNING);
            else//succeed with warning
                target.setStatusAfterTask(Target.StatusAfterTask.SUCCESS);
        }
        else{//Target Failed
            target.setStatusAfterTask(Target.StatusAfterTask.FAILURE);
        }
        target.setStatus(Target.TargetStatus.FINISHED);
    }

    private Integer calculateSleepingTime() {
        int timeToSleep;
        if(this.op == TIME_OPTION.FIXED){
            timeToSleep = this.taskTime;
        }
        else{
            Random random = new Random();
            timeToSleep = random.nextInt(this.taskTime);
        }
        return timeToSleep;
    }

/*    private String getStringWithTimeStampAttached(String attachMe){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String strDate= formatter.format(date);

        return (strDate + " - " + attachMe);
    }*/

    public enum TIME_OPTION{
        FIXED,RANDOM
    }
}