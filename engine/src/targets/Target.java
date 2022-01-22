package targets;

import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Objects;

public class Target implements Serializable {
    private final String name;
    private TargetType category;
    private TargetStatus status;
    private StatusAfterTask statusAfterTask;
    private final LinkedList<Target> outTargets;
    private final LinkedList<Target> inTargets;
    private final String data;
    private Instant startingTime;

    public Target(String name, String data){
        this.name = name;
        this.category = TargetType.INDEPENDENT;
        this.status = TargetStatus.WAITING;
        this.statusAfterTask = StatusAfterTask.SKIPPED;
        this.outTargets = new LinkedList<>();
        this.inTargets= new LinkedList<>();
        this.data = data;
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

    public LinkedList<Target> getOutTargets() {
        return outTargets;
    }

    public LinkedList<Target> getInTargets() {
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
        this.outTargets.add(newOutTarget);
    }

    public void addToInTargetsList(Target newInTarget) {
        this.inTargets.add(newInTarget);
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
}
