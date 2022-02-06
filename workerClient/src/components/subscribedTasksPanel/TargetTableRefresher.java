package components.subscribedTasksPanel;

import DTO.WorkerTargetDTO;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.function.Consumer;

public class TargetTableRefresher extends TimerTask {
    private Consumer<Boolean> usersListConsumer;

    public TargetTableRefresher(Consumer<Boolean> usersListConsumer) {
        this.usersListConsumer = usersListConsumer;
    }

    @Override
    public void run() {

        usersListConsumer.accept(true);
    }
}
