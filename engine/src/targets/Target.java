package targets;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Target{
    private final String name;
    private TargetType category;
    private TargetStatus status;
    private StatusAfterTask statusAfterTask;
    private final LinkedList<String> outTargets;
    private final LinkedList<String> inTargets;
    private final String data;
    private Instant startingTime;
    private String filePath;
    private List<String> logs;

    public Target(String name, String data){
        this.name = name;
        this.category = TargetType.INDEPENDENT;
        this.status = TargetStatus.WAITING;
        this.statusAfterTask = StatusAfterTask.SKIPPED;
        this.outTargets = new LinkedList<>();
        this.inTargets= new LinkedList<>();
        this.data = data;
        this.logs = new LinkedList<>();
    }

    public void setLogFilePath(String currentTargetFilePath) {
        this.filePath = currentTargetFilePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public enum TargetType
    {
        INDEPENDENT, LEAF, MIDDLE, ROOT
    }

    public enum TargetStatus
    {
        WAITING, IN_PROCESS, SKIPPED, FROZEN, FINISHED
    }

    public enum StatusAfterTask
    {
        SKIPPED, SUCCESS, WARNING, FAILURE
        //Default = Skipped / Didn't start yet
    }

    public TargetType getCategory() {
        return category;
    }

    public TargetStatus getStatus() {
        return status;
    }

    public StatusAfterTask getStatusAfterTask() {
        return statusAfterTask;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public LinkedList<String> getOutTargets() {
        return outTargets;
    }

    public LinkedList<String> getInTargets() {
        return inTargets;
    }

    public void setCategory(TargetType category) {
        this.category = category;
    }

    public void setStatus(TargetStatus status) {
        this.status = status;
        if(status == TargetStatus.IN_PROCESS)
            startingTime = Instant.now();
    }

    public Instant getStartingTime() {
        return startingTime;
    }

    public void setStatusAfterTask(StatusAfterTask statusAfterTask) {
        this.statusAfterTask = statusAfterTask;
    }

    public void addToOutTargetsList(Target newOutTarget) {
        this.outTargets.add(newOutTarget.getName());
    }

    public void addToInTargetsList(Target newInTarget) {
        this.inTargets.add(newInTarget.getName());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equals(name, target.name);
    }

    //@Override
            //public int hashCode() {
        //    return Objects.hash(name, category, status, statusAfterTask, outTargets, inTargets);
        //}

    public List<String> getLogs() {
        return logs;
    }

    public void addLog(String log) {
        this.logs.add(log);
    }
}
