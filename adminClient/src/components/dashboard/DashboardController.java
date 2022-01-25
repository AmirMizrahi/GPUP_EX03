package components.dashboard;

import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.*;

import static Utils.Constants.DASHBOARD_REFRESH_RATE;

public class DashboardController implements Controller {

    //Controllers
    private MainAppController mainAppController;
    private Node nodeController;
    private Timer timer;
    private TimerTask listRefresher;
    //
    //UI
    @FXML private TableView<?> tasksTableView;
    @FXML private ListView<?> tasksListView;
    @FXML private TableView<?> graphsTableView;
    @FXML private ListView<?> graphsListView;
    @FXML private TableView<UserTableViewRow> usersTableView;
    @FXML private TableColumn<UserTableViewRow, String> userTableColumn;
    @FXML private TableColumn<UserTableViewRow, String> typeTableColumn;
    @FXML private Label loggedInLabel;
   //

    @Override
    public void setMainAppController(MainAppController newMainAppController) {
        this.mainAppController = newMainAppController;
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
    private void initialize() throws IOException {
        startDashboardRefresher();
        wireColumn(userTableColumn,"userName");
        wireColumn(typeTableColumn,"userType");
    }

    private void startDashboardRefresher(){
        listRefresher = new UserListRefresher(
                this::updateUsersList);
        timer = new Timer();
        timer.schedule(listRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
    }

    private void updateUsersList(Map<String,String> usersNames) {
        List<UserTableViewRow> rows = new LinkedList<>();

        for (Map.Entry<String, String> entry : usersNames.entrySet()) {
            rows.add(new UserTableViewRow(entry.getKey(), entry.getValue()));
        }

        Platform.runLater(() -> {
            final ObservableList<UserTableViewRow> data = FXCollections.observableArrayList(rows);
            usersTableView.setItems(data);
            usersTableView.getColumns().clear();
            usersTableView.getColumns().addAll(userTableColumn,typeTableColumn);
        });
    }

    private void wireColumn(TableColumn column, String property) {
        column.setCellValueFactory(
                new PropertyValueFactory<>(property)
        );
    }

    @FXML
    void graphsTableViewOnClicked(MouseEvent event) {

    }

    @FXML
    void tasksTableViewOnClicked(MouseEvent event) {

    }

    public void initializeDashboardController(SimpleStringProperty userNameProperty) {
        loggedInLabel.textProperty().bind(userNameProperty);
    }
}
