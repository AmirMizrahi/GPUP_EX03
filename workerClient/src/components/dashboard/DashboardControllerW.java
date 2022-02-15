package components.dashboard;

import DTO.TaskDTO;
import DTO.TestDTO;
import DTO.UserDTO;
import Utils.HttpClientUtil;
import components.mainApp.ControllerW;
import components.mainApp.MainAppControllerW;
import javafx.application.Platform;
import javafx.beans.property.*;
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
import sharedDashboard.UserTableViewRow;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import static Utils.Constants.*;

public class DashboardControllerW implements ControllerW {
    //Controllers
    private MainAppControllerW mainAppControllerW;
    private Node nodeController;
    //Properties
    private SimpleStringProperty selectedTask;
    private SimpleBooleanProperty isAlreadySubscribed;
    private SimpleIntegerProperty totalMoneyEarned;
    //

    //UI
    @FXML private Button subscribeButton;
    @FXML private TableView<String> tasksTableView;
    @FXML private TableColumn<String, String> taskTableColumn;
    @FXML private ListView<String> tasksListView;
    @FXML private TableView<UserTableViewRow> usersTableView;
    @FXML private TableColumn<UserTableViewRow, String> userTableColumn;
    @FXML private TableColumn<UserTableViewRow, String> typeTableColumn;
    @FXML private Label loggedInLabel;
    @FXML private Label threadsAmountLabel;
    @FXML private Label moneyLabel;
    //

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

    @FXML
    private void initialize(){
        isAlreadySubscribed = new SimpleBooleanProperty(false);
        subscribeButton.disableProperty().bind(isAlreadySubscribed.not());
        SharedDashboard.startDashboardRefresher(
                usersTableView, userTableColumn, typeTableColumn,
                tasksTableView, taskTableColumn);

        SharedDashboard.wireColumnForUserList(userTableColumn,"userName");
        SharedDashboard.wireColumnForUserList(typeTableColumn,"userType");
        taskTableColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue()));
    }

    public void initializeDashboardController(SimpleStringProperty userNameProperty, SimpleStringProperty selectedTask, Integer threadsAmount, SimpleIntegerProperty totalMoneyEarned){
        loggedInLabel.textProperty().bind(userNameProperty);
        threadsAmountLabel.setText(threadsAmount.toString());
        this.moneyLabel.textProperty().bind(totalMoneyEarned.asString());
        this.selectedTask = selectedTask;
        this.totalMoneyEarned = totalMoneyEarned;
    }

    @FXML void tasksTableViewOnClicked(MouseEvent event) {
        boolean isSubscribed = false;

        Platform.runLater(()->isAlreadySubscribed.set(true));
        String temp = event.getPickResult().toString();
        SharedDashboard.doWhenClickedOnTaskTable(temp, this.selectedTask, this.tasksListView );

        TaskDTO selectedTaskAsDto = SharedDashboard.getSelectedTask(selectedTask);
        if(selectedTaskAsDto == null)
            return;
        tasksListView.getItems().add("Task Type: " + selectedTaskAsDto.getTaskType());
        tasksListView.getItems().add("Price Per Target: " + selectedTaskAsDto.getMoney());

        for (TestDTO subscriber : selectedTaskAsDto.getSubscribers()) {
            if(subscriber.getUserDTO().getName().compareTo(SharedDashboard.getLoggedInUserName(loggedInLabel)) == 0)
                isSubscribed = true;
        }
        tasksListView.getItems().add("Is Current Worker Registered?: " + isSubscribed);

        List<String> list = new LinkedList<>();
        //selectedTaskAsDto.getSubscribers().forEach(userDTO -> list.add(userDTO.getName()));//todo delete
        for (TestDTO testDTO : selectedTaskAsDto.getSubscribers())
            list.add(testDTO.getUserDTO().getName());
        if(list.contains(SharedDashboard.getLoggedInUserName(loggedInLabel)))
            Platform.runLater(()->isAlreadySubscribed.set(false));
        else
            Platform.runLater(()->isAlreadySubscribed.set(true));
    }

    @FXML
    void subscribeButtonAction(ActionEvent event) {
        Platform.runLater(()->isAlreadySubscribed.set(false));
        String body = "taskName=" +this.selectedTask.get() +  LINE_SEPARATOR +
                       "userName="+ SharedDashboard.getLoggedInUserName(loggedInLabel);

        HttpClientUtil.postRequest(RequestBody.create(body.getBytes()) , new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    HttpClientUtil.createErrorPopup("Server down!", e.getMessage()).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(()->HttpClientUtil.createErrorPopup("Add Task Subscriber Error!", responseBody).show());
                }
                else {
                    mainAppControllerW.addSubscriber(SharedDashboard.getSelectedTask(selectedTask));
                    //update client somehow?
                }
                response.close();
            }
        }, UPDATE_TASK_SUBSCRIBER);

    }

    public Label getUserNameLabel() {
        return this.loggedInLabel;
    }
}
