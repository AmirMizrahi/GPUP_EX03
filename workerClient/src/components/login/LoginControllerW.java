package components.login;

import Utils.Constants;
import Utils.HttpClientUtil;
import components.mainApp.ControllerW;
import components.mainApp.MainAppControllerW;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginControllerW implements ControllerW {

    private MainAppControllerW mainAppControllerW;
    private Node nodeController;
    private final SimpleStringProperty userNameProperty = new SimpleStringProperty();
    @FXML private Spinner<Integer> WorkerThreadSpinner;
    @FXML private TextField userNameTextField;
    @FXML private Button loginButton;
    @FXML void loginButtonAction(ActionEvent event) {}

    @FXML
    private void initialize() {
        SpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,5,1);
        this.WorkerThreadSpinner.setValueFactory(factory);
    }

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

}
