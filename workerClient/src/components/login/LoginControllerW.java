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
import javafx.scene.control.*;
import okhttp3.*;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import sharedLogin.SharedLogin;

import java.io.IOException;

public class LoginControllerW implements ControllerW {

    private MainAppControllerW mainAppControllerW;
    private Node nodeController;
    private final SimpleStringProperty userNameProperty = new SimpleStringProperty();
    @FXML private Spinner<Integer> WorkerThreadSpinner;
    @FXML private TextField userNameTextField;
    @FXML private Button loginButton;

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

    @FXML
    void loginButtonAction(ActionEvent event) {
        String userName = userNameTextField.getText();
        Integer threads = WorkerThreadSpinner.getValue();

        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_ADDRESS)
                .newBuilder()
                .addQueryParameter("username", userName)
                .addQueryParameter("type", "Worker")
                .addQueryParameter("threads", threads.toString())
                .build()
                .toString();

        SharedLogin.login(finalUrl, "Worker", userName,mainAppControllerW);
    }


}
