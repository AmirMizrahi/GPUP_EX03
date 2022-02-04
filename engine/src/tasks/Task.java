package tasks;
import DTO.UserDTO;
import User.User;
import targets.Target;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface Task {
    void runTask(List<Consumer<String>> consumeImmediately, Consumer<File> consumeWhenFinished, Target current, List<Target> targetList) throws InterruptedException, IOException;
    void doWhenTaskIsFinished(List<Target> targetList, List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished) throws IOException;
    String getUploaderName();
    Set<Target> getTargets();
    String getGraphName();
    void setStatus(AbstractTask.TASK_STATUS taskStatus);
    AbstractTask.TASK_STATUS getStatus();
    void addSubscriber(User user);
    List<User> getSubscribers();
    boolean isUserSubscribed(String userName);
    Target getTargetForWorker();
    String getTaskType();
    Map<String,String> getTaskInfo();
    void addToWaitingQueue(Target target);
}
