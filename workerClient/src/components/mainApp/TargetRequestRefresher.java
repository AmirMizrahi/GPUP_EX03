package components.mainApp;

import java.util.TimerTask;
import java.util.function.Consumer;

public class TargetRequestRefresher extends TimerTask {

    private final Consumer<Boolean> refresherConsumer;

    public TargetRequestRefresher(Consumer<Boolean> refresherConsumer) {
        this.refresherConsumer = refresherConsumer;
    }

    @Override
    public void run() {
        this.refresherConsumer.accept(true);
    }
}
