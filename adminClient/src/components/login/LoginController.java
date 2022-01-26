package components.login;

//import chat.client.component.main.ChatAppMainController;
//import chat.client.util.Constants;
//import chat.client.util.http.HttpClientUtil;
import Utils.Constants;
import Utils.HttpClientUtil;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
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
    private final StringProperty errorMessageProperty = new SimpleStringProperty(); //Not Happening
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
        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name"); //todo Not Happening
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

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() ->
                            errorMessageProperty.set("Something went wrong: " + responseBody)
                    );
                    System.out.println("NOT Logged in");
                } else {

                    Platform.runLater(() -> {
                        mainAppController.onLoggedIn();
                        userNameProperty.set("Logged as: [" + userNameTextField.getText() + "]      ;    Rank: Admin");
//                            chatAppMainController.updateUserName(userName);
//                            chatAppMainController.switchToChatRoom();
                    });
                    System.out.println("Logged in");
                }
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

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
