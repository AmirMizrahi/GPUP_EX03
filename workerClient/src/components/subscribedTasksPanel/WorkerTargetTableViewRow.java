package components.subscribedTasksPanel;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class WorkerTargetTableViewRow {

    private final SimpleStringProperty targetName;
    private final SimpleStringProperty taskName;
    private final SimpleStringProperty taskType;
    private final SimpleStringProperty status;
 //   private final SimpleIntegerProperty price;

    public WorkerTargetTableViewRow(String targetName, String taskName, String taskType, String status /*,int price*/) {
        this.targetName = new SimpleStringProperty(targetName);
        this.taskName = new SimpleStringProperty(taskName);
        this.taskType = new SimpleStringProperty(taskType);
        this.status = new SimpleStringProperty(status);
        //this.price = new SimpleIntegerProperty(price);
    }

    public String getTargetName() {
        return targetName.get();
    }

    public SimpleStringProperty targetNameProperty() {
        return targetName;
    }

    public String getTaskName() {
        return taskName.get();
    }

    public SimpleStringProperty taskNameProperty() {
        return taskName;
    }

    public String getTaskType() {
        return taskType.get();
    }

    public SimpleStringProperty taskTypeProperty() {
        return taskType;
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

//    public int getPrice() {
//        return price.get();
//    }
//
//    public SimpleIntegerProperty priceProperty() {
//        return price;
//    }
}
