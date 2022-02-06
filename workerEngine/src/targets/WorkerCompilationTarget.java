package targets;

import DTO.TargetDTOForWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WorkerCompilationTarget extends WorkerAbstractTarget{

    private TargetDTOForWorker targetDTOForWorker;
    private String sourceName;
    private String destinationName;

    public WorkerCompilationTarget(TargetDTOForWorker targetDTOForWorker, Map<String, String> taskInfo) {
        super(targetDTOForWorker);
        this.targetDTOForWorker = targetDTOForWorker;
        this.sourceName = taskInfo.get("source");
        this.destinationName = taskInfo.get("destination");
    }

    @Override
    public void run() throws IOException {
        compileTarget(new LinkedList<>()); //todo!
        this.isRunFinished = true;
    }

    private void compileTarget(List<Consumer<String>> consumerList) throws IOException {

        Instant start = Instant.now();
        String statusResult = "SUCCESS";
        String resourceName = sourceName + "\\" + targetDTOForWorker.getTargetDTO().getTargetData().replaceAll("\\.", "\\\\") + ".java";

        //..\java-project> javac -d out -cp out path/to/java/file.java

        String[] command = {"javac", "-d", destinationName, "-cp", destinationName, resourceName};
     //   this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached(
     //           "The command that going to process: javac -d " + destinationName + " -cp " + destinationName + sourceName + "\\" + current.getData().replaceAll("\\.", "\\\\") + ".java"));

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
           // this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached(line));
            error = true;
        }
        if(error)
            statusResult = "FAILURE"; ////

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
     //   this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("Target process time: " + timeElapsed +"ms"));

        updateResults(String.valueOf(timeElapsed), statusResult);
    }
}
