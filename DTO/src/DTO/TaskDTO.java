package DTO;


import User.User;
import targets.Target;
import tasks.AbstractTask;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.*;

public class TaskDTO {
    private final String taskName;
    private final String uploaderName;
    private final Set<Target> allTargets = new HashSet<>();
    private final int[] targetTypeArray = new int[4];
    //private final int taskTotalPrice;
    //private final int currentWorkersAmount;
    private final String graphName;
    private final String taskStatus;
    private final List<TestDTO> subscribers = new LinkedList<>();
    private final double progress;
    private final int money;
    private final Instant startingTime;

    public TaskDTO(String taskName, String uploaderName, String graphName, Set<Target> allTargets, String taskStatus, Map<User,Boolean> users, double progress, int money, Instant startingTime) {
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
        this.progress = progress;
        this.money = money;
        this.startingTime = startingTime;

        for (Map.Entry<User, Boolean> entry : users.entrySet()) { //todo maybe synchronize?
            subscribers.add(new TestDTO(new UserDTO(entry.getKey().getName(), entry.getKey().getType(), entry.getKey().getThreadAmount()), entry.getValue()));
        }
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

    public List<TestDTO> getSubscribers() {return this.subscribers;}

    public double getProgress() {
        return progress;
    }

    public int getMoney() {
        return money;
    }

    public Instant getTaskStartingTime() {
        return this.startingTime;
    }
}
