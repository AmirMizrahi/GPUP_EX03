package graph;
import targets.Target;

import java.util.LinkedList;
import java.util.List;

public class SerialSet {
    private String name;
    private List<Target> targetsInSerialSet;
    private boolean isInProcess;

    public SerialSet(String name){
        this.name = name;
        this.targetsInSerialSet = new LinkedList<>();
        this.isInProcess = false;
    }

    public void add(Target t){
        targetsInSerialSet.add(t);
    }

    public String getName() {
        return name;
    }

    public List<Target> getTargetsInSerialSet(){
        return this.targetsInSerialSet;
    }

    public boolean isInProcess() {
        return isInProcess;
    }

    public void setInProcess(boolean newStatus) {
        isInProcess = newStatus;
    }
}
