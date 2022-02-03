package targets;

import java.util.Map;

public class WorkerSimulationTarget extends WorkerAbstractTarget {

    private String targetName;
    private final Integer taskTime;
    private final TIME_OPTION op;
    private final Double chanceToSucceed;
    private final Double succeedWithWarning;

    public WorkerSimulationTarget(String targetName, Map<String, String> taskInfo) {
        this.targetName = targetName;
        this.taskTime = Integer.parseInt(taskInfo.get("taskTime"));
        this.op = TIME_OPTION.valueOf(taskInfo.get("op"));
        this.chanceToSucceed = Double.parseDouble(taskInfo.get("chanceToSucceed"));
        this.succeedWithWarning = Double.parseDouble(taskInfo.get("succeedWithWarning"));
    }

    @Override
    public Runnable run(){

    }

    public enum TIME_OPTION{
        FIXED,RANDOM
    }
}
