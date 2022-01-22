package tasks;
import targets.Target;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface Task {
    void runTask(List<Consumer<String>> consumeImmediately, Consumer<File> consumeWhenFinished, Target current, List<Target> targetList) throws InterruptedException, IOException;
    void doWhenTaskIsFinished(List<Target> targetList, List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished) throws IOException;
    }
