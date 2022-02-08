package components.subscribedTasksPanel;

import DTO.GraphDTO;
import DTO.TaskDTO;
import DTO.WorkerTargetDTO;
import Utils.HttpClientUtil;
import com.google.gson.Gson;
import components.mainApp.ControllerW;
import components.mainApp.MainAppControllerW;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import sharedDashboard.SharedDashboard;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static Utils.Constants.*;

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
    @FXML private TableView<WorkerTaskTableViewRow> tasksTableView;
    @FXML private TableColumn<WorkerTaskTableViewRow, String> rightTaskTableColumn;
    @FXML private TableColumn<WorkerTaskTableViewRow, String> workersAmountTableColumn;
    @FXML private TableColumn<WorkerTaskTableViewRow, String> progressTableColumn;
    @FXML private TableColumn<WorkerTaskTableViewRow, String> targetsCompletedTableColumn;
    @FXML private TableColumn<WorkerTaskTableViewRow, String> moneyCollectedTableColumn;
    @FXML private TextArea logTextArea;
    @FXML private Button pauseResumeButton;
    @FXML private Button unregisterButton;
    @FXML private Label moneyLabel;
    @FXML private Label selectedTaskLabel;
    //Properties
    private SimpleStringProperty selectedTask;
    private SimpleBooleanProperty isTaskSelected;
    private SimpleIntegerProperty totalMoneyEarned;
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

    public void initializeSubscribedTasksPanelController(SimpleIntegerProperty totalMoneyEarned) {
        this.selectedTask = new SimpleStringProperty();
        this.isTaskSelected = new SimpleBooleanProperty(false);
        this.totalMoneyEarned = totalMoneyEarned;
        this.moneyLabel.textProperty().bind(totalMoneyEarned.asString());
        this.unregisterButton.disableProperty().bind(isTaskSelected.not());
        SharedDashboard.wireColumnForUserList(targetTableColumn,"targetName");
        SharedDashboard.wireColumnForUserList(leftTaskTableColumn,"taskName");
        SharedDashboard.wireColumnForUserList(taskTypeTableColumn,"taskType");
        SharedDashboard.wireColumnForUserList(statusTableColumn,"status");
        SharedDashboard.wireColumnForUserList(priceTableColumn,"price");
        SharedDashboard.wireColumnForUserList(rightTaskTableColumn,"taskName");
        SharedDashboard.wireColumnForUserList(workersAmountTableColumn,"workersAmount");
        SharedDashboard.wireColumnForUserList(progressTableColumn,"progress");
        SharedDashboard.wireColumnForUserList(targetsCompletedTableColumn,"targetsCompleted");
        SharedDashboard.wireColumnForUserList(moneyCollectedTableColumn,"moneyCollected");

        Consumer<Boolean> refresherConsumer = new Consumer() {
            @Override
            public void accept(Object o) {
                updateSubscribedTaskPanel();
            }
        };

        targetTableTimer = new Timer();
        targetTableRefresher = new TargetTableRefresher(refresherConsumer);
        targetTableTimer.schedule(targetTableRefresher, TARGET_REFRESH_RATE, TARGET_REFRESH_RATE);
    }

    private void updateSubscribedTaskPanel(){
        updateTargetTable();
        updateLabels();
        updateTaskTable();
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
        final int[] test = {0,0};
        List<TaskDTO> taskDTOS = SharedDashboard.getAllTasksDTOS();

        workerTargets.forEach(row-> {
            for (TaskDTO taskDTO : taskDTOS) {
                if(row.getTaskName().compareToIgnoreCase(taskDTO.getTaskName())==0 && row.getStatus().compareToIgnoreCase("In Process") != 0) {
                    test[0] += taskDTO.getMoney();
                    test[1] = taskDTO.getMoney();
                }
            }
            rows.add(new WorkerTargetTableViewRow(row.getTargetName(), row.getTaskName(), row.getTaskType(), row.getStatus(),test[1]));
            test[1]=0;
        });

        Platform.runLater(() -> {
            final ObservableList<WorkerTargetTableViewRow> data = FXCollections.observableArrayList(rows);
            this.targetsTableView.setItems(data);
            targetsTableView.getColumns().clear();
            targetsTableView.getColumns().addAll(targetTableColumn,leftTaskTableColumn,taskTypeTableColumn,statusTableColumn,priceTableColumn);
            this.totalMoneyEarned.set(test[0]);
        });
    }

    private void updateTaskTable() {
        List<WorkerTargetDTO> workerTargets = mainAppControllerW.getAllWorkerTargets();
        Map<String, WorkerTaskTableViewRow> rows = new HashMap<>();
        List<TaskDTO> list = SharedDashboard.getAllTasksDTOS();
        final int[] targetsCompleted = new int[1];

        workerTargets.forEach(row -> {
            double progress = 0;
            TaskDTO dtoOfRow = null;
            for (TaskDTO dto : list) {
                if (dto.getTaskName().compareToIgnoreCase(row.getTaskName()) == 0) {
                    progress = dto.getProgress();
                    dtoOfRow = dto;
                    break;
                }
            }

            if (row.getStatus().compareToIgnoreCase("In Process") == 0) {
                targetsCompleted[0] = 0;
                for (WorkerTargetDTO workerTarget : workerTargets) {
                    if ((workerTarget.getTaskName().compareTo(row.getTaskName()) == 0) && (workerTarget.getStatus().compareToIgnoreCase("In Process") != 0))
                        targetsCompleted[0]++;
                }
                rows.put(row.getTaskName(), new WorkerTaskTableViewRow(row.getTaskName(), row.getWorkersAmount(), progress, targetsCompleted[0], (dtoOfRow.getMoney() * targetsCompleted[0])));
            }

            Platform.runLater(() -> {
                final ObservableList<WorkerTaskTableViewRow> data = FXCollections.observableArrayList(rows.values());
                if(rows.isEmpty()){
                    selectedTaskLabel.setText("Not Selected");
                    this.isTaskSelected.set(false);
                }
                this.tasksTableView.setItems(data);
                tasksTableView.getColumns().clear();
                tasksTableView.getColumns().addAll(rightTaskTableColumn, workersAmountTableColumn, progressTableColumn, targetsCompletedTableColumn, moneyCollectedTableColumn);
                //tasksTableView.refresh();
            });
        });
    }

    @FXML
    void targetsTableViewClicked(MouseEvent event) {

    }

    @FXML
    void tasksTableViewClicked(MouseEvent event) {
        String temp = event.getPickResult().toString();
        String name = SharedDashboard.getNameFromClick(temp);
        if(name==null)
            return;

        if(name.compareTo("null") != 0 && name.compareTo("") != 0) {
            selectedTask.set(name);
        }

        TaskDTO task = SharedDashboard.getSelectedTask(selectedTask);
        if(task==null) {
            selectedTaskLabel.setText("Not Selected");
            this.isTaskSelected.set(false);
            return;
        }
        else {
            selectedTaskLabel.setText(task.getTaskName());
            this.isTaskSelected.set(true);
        }
    }

    @FXML
    void pauseResumeButtonAction(ActionEvent event) {

    }

    @FXML
    void unregisterButtonAction(ActionEvent event) {
        Alert areYouSure = new Alert(Alert.AlertType.CONFIRMATION);
        areYouSure.setContentText("Are you sure you wish to unsubscribed from " + selectedTask.get() + "?");
        areYouSure.setHeaderText("Unregister from task?");
        areYouSure.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                TaskDTO task = SharedDashboard.getSelectedTask(selectedTask);

                String body =
                        "status=unregister" + LINE_SEPARATOR +
                                "taskName=" + task.getTaskName() + LINE_SEPARATOR +
                                "userName=" + mainAppControllerW.getLoggedInUserName();
                postTaskSubscriberChange(body);
            }
            else
                return;
        });
    }

    private void postTaskSubscriberChange(String body){
        HttpClientUtil.postRequest(RequestBody.create(body.getBytes()), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //graphsListConsumer.accept(failed);//todo
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                            String jsonArrayOfUsersNames = response.body().string();
//                            //httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Response: " + jsonArrayOfUsersNames);
//                            Type type = new TypeToken<List<TaskDTOForWorker>>(){}.getType();
//                            List<TaskDTOForWorker> taskDTOForWorkers = GSON_INSTANCE.fromJson(jsonArrayOfUsersNames, type);
//                            System.out.println("----------------------------------" + taskDTOForWorkers.size() + "-------------------------");
                response.close();
//                            //List<String> temp = new LinkedList<>();
//                            //taskDTOForWorkers.forEach(taskDTOForWorker -> temp.add(taskDTOForWorker.getTargetDTO().getTargetName()));
//                            workerManager.setThreadsOnWork(taskDTOForWorkers.size());
//                            taskDTOForWorkers.forEach(dto-> {
//                                try {
//                                    workerManager.addTargetToThreadPool(dto);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                            //System.out.println("RESULT: " + temp);


                //graphsListConsumer.accept(taskDTOForWorkers);*///todo
            }
        }, WORKER_TASK_STATUS_CHANGE);
    }
    //taskControlPanelController.refreshPanel(getSelectedTaskDTOFromDashboard());
}
