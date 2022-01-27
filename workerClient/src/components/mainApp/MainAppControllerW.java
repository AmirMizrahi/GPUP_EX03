package components.mainApp;

import components.login.LoginControllerW;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainAppControllerW {

    private Stage primaryStage;
    // Controllers
    @FXML private LoginControllerW loginControllerW;



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
        loginControllerW = (LoginControllerW) genericControllersInit("/components/login/login.fxml");


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

}


