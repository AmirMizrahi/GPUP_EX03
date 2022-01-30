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
        Alert loginPopup = new Alert(Alert.AlertType.ERROR);
        loginPopup.setHeaderText("Login Error");
        if (userName.isEmpty()) {
            loginPopup.setContentText("User name is empty. You can't login with empty user name.");
            loginPopup.show();
            return;
        }

        //noinspection ConstantConditions
        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_ADDRESS)
                .newBuilder()
                .addQueryParameter("username", userName)
                .addQueryParameter("type", "Worker")
                .addQueryParameter("threads", threads.toString())
                .build()
                .toString();

        //updateHttpStatusLine("New request is launched for: " + finalUrl);

        HttpClientUtil.runSync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->  {
                    Alert failLoginPopup = new Alert(Alert.AlertType.ERROR);
                    failLoginPopup.setHeaderText("Login Error");
                    failLoginPopup.setContentText("Something went wrong: " + e.getMessage());
                    failLoginPopup.show();
                } );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    loginPopup.setContentText("Something went wrong: " + responseBody);
                }
                else {
                    loginPopup.setContentText("Logged In Successfully as " + userNameTextField.getText());
                    Platform.runLater(() -> {
                        loginPopup.setAlertType(Alert.AlertType.INFORMATION);
                        loginPopup.setHeaderText("Success!");
                      //  mainAppController.onLoggedIn();
                        userNameProperty.set("Logged as: [" + userNameTextField.getText() + "]      ;    Rank: Admin");
//                            chatAppMainController.updateUserName(userName);
//                            chatAppMainController.switchToChatRoom();
                    });
                }
                Platform.runLater(() -> loginPopup.show() );
            }
        });
    }


}
