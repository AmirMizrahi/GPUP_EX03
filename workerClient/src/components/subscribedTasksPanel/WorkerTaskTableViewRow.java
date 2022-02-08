package components.subscribedTasksPanel;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class WorkerTaskTableViewRow {

    private final SimpleStringProperty taskName;
    private final SimpleIntegerProperty workersAmount;
    private final SimpleDoubleProperty progress;
    private final SimpleIntegerProperty targetsCompleted;
    private final SimpleIntegerProperty moneyCollected;

    public WorkerTaskTableViewRow(String taskName, Integer workersAmount, Double progress, Integer targetsCompleted ,Integer moneyCollected) {
        this.taskName = new SimpleStringProperty(taskName);
        this.workersAmount = new SimpleIntegerProperty(workersAmount);
        this.progress = new SimpleDoubleProperty(progress);
        this.targetsCompleted = new SimpleIntegerProperty(targetsCompleted);
        this.moneyCollected = new SimpleIntegerProperty(moneyCollected);
    }

    public String getTaskName() {
        return taskName.get();
    }

    public SimpleStringProperty taskNameProperty() {
        return taskName;
    }

    public Integer getWorkersAmount() {
        return workersAmount.get();
    }

    public SimpleIntegerProperty workersAmountProperty() {
        return workersAmount;
    }

    public double getProgress() {
        return progress.get();
    }

    public SimpleDoubleProperty progressProperty() {
        return progress;
    }

    public int getTargetsCompleted() {
        return targetsCompleted.get();
    }

    public SimpleIntegerProperty targetsCompletedProperty() {
        return targetsCompleted;
    }

    public int getMoneyCollected() {
        return moneyCollected.get();
    }

    public SimpleIntegerProperty moneyCollectedProperty() {
        return moneyCollected;
    }
}
