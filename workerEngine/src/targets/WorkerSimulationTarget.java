package targets;

import DTO.TaskDTOForWorker;
import java.util.Map;
import java.util.Random;

public class WorkerSimulationTarget extends WorkerAbstractTarget {

    private TaskDTOForWorker dto;
    private final Integer taskTime;
    private final TIME_OPTION op;
    private final Double chanceToSucceed;
    private final Double succeedWithWarning;

    public WorkerSimulationTarget(TaskDTOForWorker dto, Map<String, String> taskInfo) {
        this.dto = dto;
        this.taskTime = Integer.parseInt(taskInfo.get("taskTime"));
        this.op = TIME_OPTION.valueOf(taskInfo.get("op"));
        this.chanceToSucceed = Double.parseDouble(taskInfo.get("chanceToSucceed"));
        this.succeedWithWarning = Double.parseDouble(taskInfo.get("succeedWithWarning"));
        this.isRunFinished = false;
    }

    @Override
    public void run() throws InterruptedException{
        Integer sleepingTime = calculateSleepingTime();
        Thread.sleep(sleepingTime);
        String result = setStatusAfterTask(); //result of build!
        this.isRunFinished = true;
        updateResults(dto, sleepingTime.toString(), result);
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

    private String setStatusAfterTask(){
        Random number = new Random();
        String result;
        if(number.nextDouble() <= this.chanceToSucceed)//Target Succeed
        {
            if(number.nextDouble() <= this.succeedWithWarning)//succeed
                result = "WARNING";
            else//succeed with warning
                result = "SUCCESS";
        }
        else{//Target Failed
            result = "FAILURE";
        }
        return result;
    }

//    private void updateResults() {
//
//    }

    public enum TIME_OPTION{
        FIXED,RANDOM
    }
}
