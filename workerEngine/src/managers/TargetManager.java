package managers;

import targets.WorkerTarget;

import java.util.LinkedList;
import java.util.List;

public class TargetManager {
    private List<WorkerTarget> targetsFromServer;

    public TargetManager(){
        targetsFromServer = new LinkedList<>();
    }

    public void addTarget(WorkerTarget target){
        targetsFromServer.add(target);
    }

    public List<WorkerTarget> getAllTargets(){return this.targetsFromServer;}

//    public WorkerTarget getTarget(String name) {
//        for (WorkerTarget workerTarget : targetsFromServer) {
//            if(name.compareTo(workerTarget.getResult().get("targetName")) == 0)
//                return workerTarget;
//        }
//        return null;
//    }
}
