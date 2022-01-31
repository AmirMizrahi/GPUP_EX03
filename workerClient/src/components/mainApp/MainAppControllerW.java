package components.mainApp;

import components.dashboard.DashboardControllerW;
import components.login.LoginControllerW;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import sharedControllers.sharedMainAppController;

import java.io.IOException;

import static sharedMainApp.SharedMainApp.sharedOnLoggedIn;

public class MainAppControllerW implements sharedMainAppController {

    private Stage primaryStage;
    private BooleanProperty isLoggedIn;
    // Controllers
    private LoginControllerW loginControllerW;
    private DashboardControllerW dashboardControllerW;
    //UI
    @FXML private ColumnConstraints asd;
    @FXML private Button dashboardButton;
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
        isLoggedIn = new SimpleBooleanProperty(false);
        this.dashboardButton.disableProperty().bind(isLoggedIn.not());

        loginControllerW = (LoginControllerW) genericControllersInit("/components/login/login.fxml");
        dashboardControllerW = (DashboardControllerW) genericControllersInit("/components/dashboard/dashboard.fxml");

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
//        this.isLoggedIn.set(true);
//        gridPaneMainAppRight.getChildren().remove(0); //move to property
//        gridPaneMainAppRight.getChildren().add(this.dashboardControllerW.getNodeController());
        sharedOnLoggedIn(gridPaneMainAppRight,isLoggedIn,this.dashboardControllerW.getNodeController());
        //this.dashboardControllerW.initializeDashboardController(this.loginController.userNamePropertyProperty(), this.selectedGraph, this.selectedTask, this.matchingUserName, isServerOn);
        //startTaskControlPanelRefresher();
    }

}


