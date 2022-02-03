package targets;

import java.io.IOException;
import java.util.Map;

public interface WorkerTarget {
    void run() throws InterruptedException, IOException;
    boolean isRunFinished();
    Map<String, String> getResult();
}
