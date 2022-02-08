package tasks;
import targets.Target;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CompilationTask extends AbstractTask implements Task {

    private String sourceName;
    private String destinationName;

    public CompilationTask(Map<String,String> taskInfo, AbstractTask.WAYS_TO_START_SIM_TASK way, List<Target> targetsToRunOn,
                           String pathName, String sourceName, String destinationName, boolean firstTime, String userName, String graphName,
                           int money){
        super(way, firstTime, targetsToRunOn, pathName, "Compilation", userName, graphName, taskInfo,money);
        this.sourceName = sourceName;
        this.destinationName = destinationName;
    }

    @Override
    public void runTask(List<Consumer<String>> consumeImmediately,Consumer<File> consumeWhenFinished, Target current, List<Target> targetList) throws  IOException {
        String currentTargetFilePath;
        int targetRunningTime;

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


        printDetailsBeforeSleep(consumeImmediately);
        targetRunningTime = compileTarget(current, consumeImmediately);
        targetsToRunningTime.put(current,targetRunningTime);//////////////////
        totalRunningTime += targetRunningTime;///////////

        //setStatusAfterTask(current); //result of build!
        //setStatusAfterTaskForAllEffected(current, targetList, currentTargetFilePath,consumerList); //Change another targets if needed
        printDetailsAfterTask(current,consumeImmediately);

        synchronized (consumeWhenFinished) {
            consumeWhenFinished.accept(targetLogFile);
        }

        //need to print I have waked up and the current time
        //consumerList.remove(consumer);////////////////////

    }

    private int compileTarget(Target current, List<Consumer<String>> consumerList) throws IOException {

        Instant start = Instant.now();

        String resourceName = sourceName + "\\" + current.getData().replaceAll("\\.", "\\\\") + ".java";
        //..\java-project> javac -d out -cp out path/to/java/file.java

        String[] command = {"javac", "-d", destinationName, "-cp", destinationName, resourceName};
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached(
                "The command that going to process: javac -d " + destinationName + " -cp " + destinationName + sourceName + "\\" + current.getData().replaceAll("\\.", "\\\\") + ".java"));

        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(false);
        Process p = processBuilder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        boolean error = false;
        while (true) {
            line=r.readLine();
            if (line == null) {
                break;
            }
            this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached(line));
            error = true;
        }
        if(error)
            current.setStatusAfterTask(Target.StatusAfterTask.FAILURE);
        else
            current.setStatusAfterTask(Target.StatusAfterTask.SUCCESS);

        current.setStatus(Target.TargetStatus.FINISHED); //TEMP -> DELETE AND SORT IT

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("Target process time: " + timeElapsed +"ms"));

        return (int)timeElapsed;
        //Start timer
        //Compile
        //return timer
    }

    private void printDetailsBeforeSleep(List<Consumer<String>> consumerList){
        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("New status: In Process"));
    }
}
