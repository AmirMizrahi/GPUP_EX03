package components.mainApp;

import DTO.TaskDTO;
import components.dashboard.DashboardControllerW;
import components.login.LoginControllerW;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import sharedControllers.sharedMainAppController;
import sharedDashboard.SharedDashboard;
import sharedLogin.SharedLogin;
import sharedMainApp.SharedMainApp;

import java.io.IOException;

import static sharedMainApp.SharedMainApp.sharedOnLoggedIn;

public class MainAppControllerW implements sharedMainAppController {

    private Stage primaryStage;
    private BooleanProperty isLoggedIn;
    private SimpleStringProperty selectedTask;
    // Controllers
    private LoginControllerW loginControllerW;
    private DashboardControllerW dashboardControllerW;
    //UI
    @FXML private Label serverStatusLabel;
    @FXML private Button dashboardButton;
    @FXML private Button subscribedTasksPanelButton;
    @FXML private ComboBox<?> changeSkinComboBox;
    @FXML private GridPane gridPaneMainAppRight;
    @FXML void changeSkinComboBoxAction(ActionEvent event) {}
    @FXML void dashboardButtonAction(ActionEvent event) {}

    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @FXML
    private void initialize() throws IOException {
        selectedTask = new SimpleStringProperty();
        isLoggedIn = new SimpleBooleanProperty(false);
        this.dashboardButton.disableProperty().bind(isLoggedIn.not());

        loginControllerW = (LoginControllerW) genericControllersInit("/components/login/login.fxml");
        dashboardControllerW = (DashboardControllerW) genericControllersInit("/components/dashboard/dashboard.fxml");

        this.serverStatusLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            String str;
            if (SharedMainApp.getServerOnProperty().get())
                str = "Server is ON";
            else
                str = "Server is OFF";
            return str;
        }, SharedMainApp.getServerOnProperty()));

        this.gridPaneMainAppRight.getChildren().remove(0);
        gridPaneMainAppRight.getChildren().add(this.loginControllerW.getNodeController());
    }

    private ControllerW genericControllersInit(String str) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(str));
        Node node = loader.load();
        ControllerW ctr = loader.getController();
        ctr.setNodeController(node);
        ctr.setMainAppControllerW(this);

        return ctr;
    }

    @Override
    public void onLoggedIn() {
        sharedOnLoggedIn(gridPaneMainAppRight,isLoggedIn,this.dashboardControllerW.getNodeController());
        this.dashboardControllerW.initializeDashboardController(SharedLogin.userNamePropertyProperty(), this.selectedTask);
        //startTaskControlPanelRefresher();
    }

    public TaskDTO getSelectedTaskDTOFromDashboard() {
        return SharedDashboard.getSelectedTask(this.selectedTask);
    }

    @FXML
    void subscribedTasksPanelButtonAction(ActionEvent event) {

    }
}


