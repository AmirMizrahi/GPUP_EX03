package targets;

import DTO.TaskDTOForWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WorkerCompilationTarget extends WorkerAbstractTarget{

    private TaskDTOForWorker dto;
    private String sourceName;
    private String destinationName;

    public WorkerCompilationTarget(TaskDTOForWorker dto, Map<String, String> taskInfo) {
        this.dto = dto;
        this.sourceName = taskInfo.get("source");
        this.destinationName = taskInfo.get("destination");
    }

    @Override
    public void run() {
        //compileTarget();
    }

    //private int compileTarget(Target current, List<Consumer<String>> consumerList) throws IOException {

//        Instant start = Instant.now();
//
//        String resourceName = sourceName + "\\" + current.getData().replaceAll("\\.", "\\\\") + ".java";
//        //..\java-project> javac -d out -cp out path/to/java/file.java
//
//        String[] command = {"javac", "-d", destinationName, "-cp", destinationName, resourceName};
//        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached(
//                "The command that going to process: javac -d " + destinationName + " -cp " + destinationName + sourceName + "\\" + current.getData().replaceAll("\\.", "\\\\") + ".java"));
//
//        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(false);
//        Process p = processBuilder.start();
//        BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//        String line;
//        boolean error = false;
//        while (true) {
//            line=r.readLine();
//            if (line == null) {
//                break;
//            }
//            this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached(line));
//            error = true;
//        }
//        if(error)
//            this.results.put("status","FAILURE");
//        else
//            current.setStatusAfterTask(Target.StatusAfterTask.SUCCESS);
//
//        current.setStatus(Target.TargetStatus.FINISHED); //TEMP -> DELETE AND SORT IT
//
//        Instant finish = Instant.now();
//        long timeElapsed = Duration.between(start, finish).toMillis();
//        this.printerBridge.acceptListOfConsumers(consumerList,this.printerBridge.getStringWithTimeStampAttached("Target process time: " + timeElapsed +"ms"));

     //   return (int)timeElapsed;
        //Start timer
        //Compile
        //return timer
 //   }
}
