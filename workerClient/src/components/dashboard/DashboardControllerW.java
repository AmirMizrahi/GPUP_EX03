package components.dashboard;

import DTO.TaskDTO;
import Utils.HttpClientUtil;
import components.mainApp.ControllerW;
import components.mainApp.MainAppControllerW;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
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

import static Utils.Constants.*;

public class DashboardControllerW implements ControllerW {
    //Controllers
    private MainAppControllerW mainAppControllerW;
    private Node nodeController;
    private SimpleStringProperty selectedTask;

    //UI
    @FXML private Button subscribeButton;
    @FXML private TableView<String> tasksTableView;
    @FXML private TableColumn<String, String> taskTableColumn;
    @FXML private ListView<String> tasksListView;
    @FXML private TableView<UserTableViewRow> usersTableView;
    @FXML private TableColumn<UserTableViewRow, String> userTableColumn;
    @FXML private TableColumn<UserTableViewRow, String> typeTableColumn;
    @FXML private Label loggedInLabel;
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
        SharedDashboard.startDashboardRefresher(
                usersTableView, userTableColumn, typeTableColumn,
                tasksTableView, taskTableColumn);

        SharedDashboard.wireColumnForUserList(userTableColumn,"userName");
        SharedDashboard.wireColumnForUserList(typeTableColumn,"userType");
        taskTableColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue()));
    }

    public void initializeDashboardController(SimpleStringProperty userNameProperty, SimpleStringProperty selectedTask){
        loggedInLabel.textProperty().bind(userNameProperty);
        this.selectedTask = selectedTask;
    }

    @FXML void tasksTableViewOnClicked(MouseEvent event) {
        String temp = event.getPickResult().toString();
        SharedDashboard.doWhenClickedOnTaskTable(temp, this.selectedTask, this.tasksListView );
    }

    @FXML
    void subscribeButtonAction(ActionEvent event) {
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
                    //update client somehow?
                }
            }
        }, UPDATE_TASK_SUBSCRIBER);

    }
}
