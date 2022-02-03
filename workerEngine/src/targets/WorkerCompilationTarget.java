package targets;

import java.util.Map;

public class WorkerCompilationTarget extends WorkerAbstractTarget{

    private String targetName;
    private String source;
    private String destination;
    public WorkerCompilationTarget(String targetName, Map<String, String> taskInfo) {
        this.targetName = targetName;
        this.source = taskInfo.get("source");
        this.destination = taskInfo.get("destination");
    }

    @Override
    public Runnable run() {

    }
}
