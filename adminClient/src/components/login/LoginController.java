package components.login;

//import chat.client.component.main.ChatAppMainController;
//import chat.client.util.Utils.Constants;
//import chat.client.util.http.HttpClientUtil;
import Utils.Constants;
import Utils.HttpClientUtil;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;

public class LoginController implements Controller {

    private MainAppController mainAppController;
    private Node nodeController;
    //UI
    @FXML private TextField userNameTextField;
    //
    //Properties
    private final SimpleStringProperty userNameProperty = new SimpleStringProperty();
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
    public void initialize() {
//        errorMessageLabel.textProperty().bind(errorMessageProperty);
//        HttpClientUtil.setCookieManagerLoggingFacility(line ->
//                Platform.runLater(() ->
//                        updateHttpStatusLine(line)));
    }

    @FXML
    private void loginButtonAction(ActionEvent event) {
        String userName = userNameTextField.getText();
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
                        .addQueryParameter("type", "Admin")
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
                        mainAppController.onLoggedIn();
                        userNameProperty.set("Logged as: [" + userNameTextField.getText() + "]      ;    Rank: Admin");
//                            chatAppMainController.updateUserName(userName);
//                            chatAppMainController.switchToChatRoom();
                    });
                }
                Platform.runLater(() -> loginPopup.show() );
            }
        });
    }

//    @FXML
//    private void userNameKeyTyped(KeyEvent event) {
//        errorMessageProperty.set("");
//    }

    @FXML
    private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

//    private void updateHttpStatusLine(String data) {
//        chatAppMainController.updateHttpLine(data);
//    }
//
//    public void setChatAppMainController(ChatAppMainController chatAppMainController) {
//        this.chatAppMainController = chatAppMainController;
//    }


    public String getUserNameProperty() {
        return userNameProperty.get();
    }

    public SimpleStringProperty userNamePropertyProperty() {
        return userNameProperty;
    }
}
