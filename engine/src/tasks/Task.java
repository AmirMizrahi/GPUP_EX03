package tasks;
import User.User;
import targets.Target;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface Task {
    void runTask(List<Consumer<String>> consumeImmediately, Consumer<File> consumeWhenFinished, Target current, List<Target> targetList) throws InterruptedException, IOException;
    void doWhenTaskIsFinished(Set<Target> targetList) throws IOException;
    String getUploaderName();
    Set<Target> getTargets();
    String getGraphName();
    void setStatus(AbstractTask.TASK_STATUS taskStatus);
    AbstractTask.TASK_STATUS getStatus();
    void addSubscriber(User user);
    Map<User,Boolean> getSubscribers();
    boolean isUserSubscribed(String userName);
    Target getTargetForWorker();
    String getTaskType();
    Map<String,String> getTaskInfo();
    void addToWaitingQueue(Target target);
    int getCounterName();
    void addOneToCounterName();
    double getProgress();
    int getWorkersAmount();
    void removeSub(String userName);
    int getMoney();
    void updatePauseResume(String userName, Boolean isPauseSelected);
    boolean isUserPaused(String userName);
    void printBeforeProcess(List<Consumer<String>> consumeImmediately, Target current) throws InterruptedException, IOException;
    void printAfterProcess(List<Consumer<String>> consumeImmediately, Target current, String currentTargetFilePath, Integer totalTime, String errors)  throws InterruptedException, IOException;
    Instant getStartingTime();
}
