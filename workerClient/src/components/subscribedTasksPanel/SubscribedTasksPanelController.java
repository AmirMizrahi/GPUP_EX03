package components.subscribedTasksPanel;

import DTO.UserDTO;
import DTO.WorkerTargetDTO;
import components.mainApp.ControllerW;
import components.mainApp.MainAppControllerW;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import sharedDashboard.GraphListRefresher;
import sharedDashboard.SharedDashboard;
import sharedDashboard.UserTableViewRow;

import java.util.*;
import java.util.function.Consumer;

import static Utils.Constants.DASHBOARD_REFRESH_RATE;

public class SubscribedTasksPanelController implements ControllerW {

    //Controllers
    private MainAppControllerW mainAppControllerW;
    private Node nodeController;
    //
    //UI
    @FXML private Label threadsOnWorkLabel;
    @FXML private Label availableThreadsLabel;
    @FXML private TableView<WorkerTargetTableViewRow> targetsTableView;
    @FXML private TableColumn<WorkerTargetTableViewRow, String> targetTableColumn;
    @FXML private TableColumn<WorkerTargetTableViewRow, String> leftTaskTableColumn;
    @FXML private TableColumn<WorkerTargetTableViewRow, String> taskTypeTableColumn;
    @FXML private TableColumn<WorkerTargetTableViewRow, String> statusTableColumn;
    @FXML private TableColumn<WorkerTargetTableViewRow, String> priceTableColumn;
    @FXML private TableView<?> tasksTableView;
    @FXML private TableColumn<?, ?> rightTaskTableColumn;
    @FXML private TableColumn<?, ?> workersAmountTableColumn;
    @FXML private TableColumn<?, ?> progressTableColumn;
    @FXML private TableColumn<?, ?> targetsCompletedTableColumn;
    @FXML private TableColumn<?, ?> moneyCollectedTableColumn;
    @FXML private TextArea logTextArea;
    @FXML private Button pauseResumeButton;
    @FXML private Button unregisterButton;
    @FXML private Label moneyLabel;
    //
    private Timer targetTableTimer;
    private TimerTask targetTableRefresher;

    @Override
    public void setMainAppControllerW(MainAppControllerW newMainAppControllerW) {
        this.mainAppControllerW = newMainAppControllerW;
    }
    @Override
    public Node getNodeController(){
        return this.nodeController;
    }

    @Override
    public void setNodeController(Node node){
        this.nodeController = node;
    }

    public void initializeSubscribedTasksPanelController() {
        SharedDashboard.wireColumnForUserList(targetTableColumn,"targetName");
        SharedDashboard.wireColumnForUserList(leftTaskTableColumn,"taskName");
        SharedDashboard.wireColumnForUserList(taskTypeTableColumn,"taskType");
        SharedDashboard.wireColumnForUserList(statusTableColumn,"status");
        //SharedDashboard.wireColumnForUserList(priceTableColumn,"price");

        Consumer<Boolean> refresherConsumer = new Consumer() {
            @Override
            public void accept(Object o) {
                updateSubscribedTaskPanel();
            }
        };

        targetTableTimer = new Timer();
        targetTableRefresher = new TargetTableRefresher(refresherConsumer);
        targetTableTimer.schedule(targetTableRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
    }

    private void updateSubscribedTaskPanel(){
        updateTargetTable();
        updateLabels();
    }

    private void updateLabels(){
        Platform.runLater(()->{
            this.threadsOnWorkLabel.setText(this.mainAppControllerW.getCurrentThreadsWorking().toString());
            this.availableThreadsLabel.setText(this.mainAppControllerW.getMaxThreadsAmount().toString());
        });
    }

    private void updateTargetTable() {
        List<WorkerTargetDTO> workerTargets =  mainAppControllerW.getAllWorkerTargets();
        List<WorkerTargetTableViewRow> rows = new LinkedList<>();

        workerTargets.forEach(row-> {
            rows.add(new WorkerTargetTableViewRow(row.getTargetName(), row.getTaskName(), row.getTaskType(), row.getStatus())); //todo price
        });

        Platform.runLater(() -> {
            final ObservableList<WorkerTargetTableViewRow> data = FXCollections.observableArrayList(rows);
            this.targetsTableView.setItems(data);
            targetsTableView.getColumns().clear();
            targetsTableView.getColumns().addAll(targetTableColumn,leftTaskTableColumn,taskTypeTableColumn,statusTableColumn,priceTableColumn);
        });
    }

    @FXML
    void pauseResumeButtonAction(ActionEvent event) {

    }

    @FXML
    void unregisterButtonAction(ActionEvent event) {

    }
}
