package tasks;

import bridges.PrinterBridge;
import targets.Target;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractTask implements Task{

    protected final AbstractTask.WAYS_TO_START_SIM_TASK chosenWay;
    protected final Boolean firstTime;
    private final String uploadedUserName;
    protected int orderOfProcess = 1, totalRunningTime = 0;
    protected final List<Target> relevantTargetsForSummaryLogFile;
    protected final Map<Target,Integer> targetsToRunningTime;
    protected String fileFullName;
    protected final PrinterBridge printerBridge;

    protected AbstractTask(WAYS_TO_START_SIM_TASK chosenWay, boolean firstTime, List<Target> targetsToRunOn,
                           String pathName, String taskName, String userName) {
        this.chosenWay = chosenWay;
        this.firstTime = firstTime;
        this.relevantTargetsForSummaryLogFile = new ArrayList<>();
        this.targetsToRunningTime = new HashMap<>();
        this.printerBridge = new PrinterBridge();
        this.fileFullName = createTaskFolder(pathName,taskName);
        this.uploadedUserName = userName;
        targetsToRunOn.forEach(t -> targetsToRunningTime.put(t, 0));
    }

    protected Consumer<String> consumerBuilder(String path){
        return str-> {
            try {
                FileWriter myWriter = new FileWriter(path, true);
                myWriter.write(str+"\n");
                myWriter.close();
            }
            catch (IOException e){}
        };
    }

    private String createTaskFolder(String pathName, String typeOfTask){
        File cTempFile = new File(pathName);
        cTempFile.mkdir();// nn to chk

        String taskFileFullName = this.printerBridge.getStringWithTimeStampAttached("");
        taskFileFullName = taskFileFullName.replace("-","");
        taskFileFullName = taskFileFullName.trim();
        taskFileFullName = typeOfTask + " - " + taskFileFullName;
        taskFileFullName = taskFileFullName.replaceAll(":",".");

        File taskFile = new File(pathName + "\\" + taskFileFullName);
        taskFile.mkdir();// nn to chk
        return taskFile.getAbsolutePath();
    }

    private File createSummaryFile(String SimulationFileFullName) throws IOException {
        File file = new File(SimulationFileFullName + "\\" + "Summary.log");
        file.createNewFile();
        return file;
    }

    public void doWhenTaskIsFinished(List<Target> targetList, List<Consumer<String>> consumerList,Consumer<File> consumeWhenFinished) throws IOException {
        List<Target> skippedTargetsForSummaryLogFile = new ArrayList<>();

        for (Target t: targetList) {
            if (t.getStatus() != Target.TargetStatus.SKIPPED && t.getStatus() != Target.TargetStatus.FINISHED)
                throw (new IllegalArgumentException("The task isn't finished yet."));
            else if (t.getStatus() == Target.TargetStatus.SKIPPED)
                skippedTargetsForSummaryLogFile.add(t);
        }

        File summaryLogFile = createSummaryFile(fileFullName);
        String summaryLogFilePath = summaryLogFile.getAbsolutePath();
        Consumer<String> consumer = consumerBuilder(summaryLogFilePath);
        consumerList.add(consumer);
        printToSummaryLogFile(consumerList,totalRunningTime, relevantTargetsForSummaryLogFile, targetsToRunningTime, skippedTargetsForSummaryLogFile);
        consumeWhenFinished.accept(summaryLogFile);
    }

    private void printToSummaryLogFile(List<Consumer<String>> consumerList ,int totalRunningTime, List<Target> targetList, Map<Target,Integer> targetsToRunningTime, List<Target> skippedTargetsForSummaryLogFile){
        String totalRunningTimeInFormat = calcTotalRunningTimeToSummaryFile(totalRunningTime);
        if(firstTime && this.chosenWay == AbstractTask.WAYS_TO_START_SIM_TASK.INCREMENTAL)
            this.printerBridge.acceptListOfConsumers(consumerList,"Chosen 'INCREMENTAL' but this is the first time you activated the current task!");
        this.printerBridge.acceptListOfConsumers(consumerList,"Total running time: "+ totalRunningTimeInFormat);

        String statusAfterTaskInFormat = calcStatisticsForStatusAfterTaskToSummaryLogFile(targetList, skippedTargetsForSummaryLogFile.size());
        this.printerBridge.acceptListOfConsumers(consumerList,statusAfterTaskInFormat);

        targetList.forEach(name->{
            //String name = t.getName();
            Target.StatusAfterTask sat = name.getStatusAfterTask();
            String runningTime = calcTotalRunningTimeToSummaryFile(targetsToRunningTime.get(name));
            this.printerBridge.acceptListOfConsumers(consumerList,String.format("Target name: %s\nStatus after task: %s\nRunning time: %s\n",name,sat,runningTime));
        });

        this.printerBridge.acceptListOfConsumers(consumerList,"Targets who are SKIPPED in this task are: " + skippedTargetsForSummaryLogFile);
    }

    //Success | Warning | Failure | Skipped
    private String calcStatisticsForStatusAfterTaskToSummaryLogFile(List<Target> targetList, int skippedTargetsAmount){
        int[] arr = new int[3];

        for (Target t:targetList) {
            Target.StatusAfterTask finishStatus = t.getStatusAfterTask();
            switch (finishStatus) {
                case SUCCESS:
                    arr[0]++;
                    break;
                case WARNING:
                    arr[1]++;
                    break;
                case FAILURE:
                    arr[2]++;
                    break;
            }
        }
        return String.format("Status after task:\nSuccess: %d\nWarning: %d\nFailure: %d\nSkipped: %d\n", arr[0],arr[1],arr[2],skippedTargetsAmount);
    }

    private String calcTotalRunningTimeToSummaryFile(int total){
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(total),
                TimeUnit.MILLISECONDS.toMinutes(total) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(total) % TimeUnit.MINUTES.toSeconds(1));
    }

    protected void printNameAndStatus(Target target, int orderOfProcess,List<Consumer<String>> consumerList){
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("#" + orderOfProcess));
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("Target name: " + target.getName()));
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("Target status: " + target.getStatus()));
    }

    protected void printDetailsAfterTask(Target target, List<Consumer<String>> consumerList){
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("Target process finished!"));
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("New Target status: " + target.getStatus()));
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("Target status after task is: " + target.getStatusAfterTask()+"\n"));
    }

    public String getUploaderName() {
        return this.uploadedUserName;
    }


    public enum WAYS_TO_START_SIM_TASK
    {
        FROM_SCRATCH,INCREMENTAL
    }
}