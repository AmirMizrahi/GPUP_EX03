package components.login;

//import chat.client.component.main.ChatAppMainController;
//import chat.client.util.Utils.Constants;
//import chat.client.util.http.HttpClientUtil;
import Utils.Constants;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import okhttp3.*;
import sharedLogin.SharedLogin;


public class LoginController implements Controller {

    private MainAppController mainAppController;
    private Node nodeController;
    //UI
    @FXML private TextField userNameTextField;
    //
    //Properties
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
        userNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\sa-zA-Z*\\d*")) {
                userNameTextField.setText(newValue.replaceAll("[^\\sa-zA-Z\\d]", ""));
            }
        });
//        errorMessageLabel.textProperty().bind(errorMessageProperty);
//        HttpClientUtil.setCookieManagerLoggingFacility(line ->
//                Platform.runLater(() ->
//                        updateHttpStatusLine(line)));
    }

    @FXML
    private void loginButtonAction(ActionEvent event) {
        String userName = userNameTextField.getText();

        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_ADDRESS)
                .newBuilder()
                .addQueryParameter("username", userName)
                .addQueryParameter("type", "Admin")
                .build()
                .toString();

        SharedLogin.login(finalUrl, "Admin", userName,mainAppController);
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

}
