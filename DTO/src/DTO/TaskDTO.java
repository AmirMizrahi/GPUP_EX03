package DTO;


import targets.Target;
import tasks.AbstractTask;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TaskDTO {
    private final String taskName;
    private final String uploaderName;
    private final Set<Target> allTargets = new HashSet<>();
    private final int[] targetTypeArray = new int[4];
    //private final int taskTotalPrice;
    //private final int currentWorkersAmount;
    private final String graphName;
    private final String taskStatus;

    public TaskDTO(String taskName, String uploaderName, String graphName, Set<Target> allTargets, String taskStatus) {
        this.taskName = taskName;
        this.uploaderName = uploaderName;
        this.allTargets.addAll(allTargets);
        for (Target currentTarget : this.allTargets) {
            if (currentTarget.getCategory() == Target.TargetType.INDEPENDENT)
                targetTypeArray[0]++;
            else if (currentTarget.getCategory() == Target.TargetType.LEAF)
                targetTypeArray[1]++;
            else if (currentTarget.getCategory() == Target.TargetType.MIDDLE)
                targetTypeArray[2]++;
            else if (currentTarget.getCategory() == Target.TargetType.ROOT)
                targetTypeArray[3]++;
        }
        this.graphName = graphName;
        this.taskStatus = taskStatus;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public String getUploaderName() {
        return this.uploaderName;
    }

    public Integer getAmountOfTargets() {
        return this.allTargets.size();
    }

    public Integer getIndependentAmount() {
        return this.targetTypeArray[0];
    }

    public Integer getLeafAmount() {
        return this.targetTypeArray[1];
    }

    public Integer getMiddleAmount() {
        return this.targetTypeArray[2];
    }

    public Integer getRootAmount() {
        return this.targetTypeArray[3];
    }

    public String getGraphName() {
        return this.graphName;
    }

    public List<TargetDTO> getAllTargets() {
        List<TargetDTO> toReturn = new LinkedList<>();
        allTargets.forEach(target -> toReturn.add(new TargetDTO(target)));
        return toReturn;
    }

    public String getTaskStatus() {return this.taskStatus;}
}
