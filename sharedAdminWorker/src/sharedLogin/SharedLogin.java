package sharedLogin;

import Utils.Constants;
import Utils.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import sharedControllers.sharedMainAppController;

import java.io.IOException;

public class SharedLogin {
    private static final SimpleStringProperty userNameProperty = new SimpleStringProperty();

    public static void login(String finalUrl, String clientType, String userName, sharedMainAppController mainAppController){
        {
            Alert loginPopup = new Alert(Alert.AlertType.ERROR);
            loginPopup.setHeaderText("Login Error");
            if (userName.isEmpty()) {
                loginPopup.setContentText("User name is empty. You can't login with empty user name.");
                loginPopup.show();
                return;
            }

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
                        loginPopup.setContentText("Logged In Successfully as " + userName);
                        Platform.runLater(() -> {
                            loginPopup.setAlertType(Alert.AlertType.INFORMATION);
                            loginPopup.setHeaderText("Success!");
                            mainAppController.onLoggedIn();
                            userNameProperty.set("Logged as: [" + userName + "]      ;    Rank: "+ clientType);
//                            chatAppMainController.updateUserName(userName);
//                            chatAppMainController.switchToChatRoom();
                        });
                    }
                    Platform.runLater(() -> loginPopup.show() );
                }
            });
        }
    }

    public static SimpleStringProperty userNamePropertyProperty() {
        return userNameProperty;
    }

}
