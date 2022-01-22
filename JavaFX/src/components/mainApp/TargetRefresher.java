package components.mainApp;

import java.util.TimerTask;
import java.util.function.Consumer;

public class TargetRefresher extends TimerTask {

    private final Consumer<Boolean> refresherConsumer;

    public TargetRefresher(Consumer<Boolean> refresherConsumer) {
        this.refresherConsumer = refresherConsumer;
    }

    @Override
    public void run() {
        this.refresherConsumer.accept(true);
            /// List<TargetDTO> a = getFromEngine
            /// UpdateAllTables(a)

            /// Consumer.accept() =>
        //                          //
                                    /// List<TargetDTO> a = getFromEngine
                                    /// UpdateAllTables(a)
    }
}
