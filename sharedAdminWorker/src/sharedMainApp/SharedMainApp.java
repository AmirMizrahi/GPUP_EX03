package sharedMainApp;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

public class SharedMainApp {
    private static BooleanProperty isServerOn = new SimpleBooleanProperty(false);


    public static void sharedOnLoggedIn(GridPane gridPaneMainAppRight, BooleanProperty isLoggedIn, Node node){
        isLoggedIn.set(true);
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        gridPaneMainAppRight.getChildren().add(node);
    }

    public static BooleanProperty getServerOnProperty() {
        return isServerOn;
    }

    public static void changeSkin(ComboBox<String> changeSkinComboBox, Class classs, Scene scene){
        String skinName = changeSkinComboBox.getSelectionModel().getSelectedItem();
        scene.getStylesheets().clear();

        switch (skinName) {
            case "No Skin":
                //todo . for backwards is pitfall?
                break;
            case "Light Mode":
                scene.getStylesheets().add(classs.getResource("/cssSkins/LightTheme.css").toExternalForm());
                break;
            case "Dark Mode":
                scene.getStylesheets().add(classs.getResource("/cssSkins/DarkTheme.css").toExternalForm());
                break;
            case "Old School Mode":
                scene.getStylesheets().add(classs.getResource("/cssSkins/OldSchoolTheme.css").toExternalForm());
                break;
        }
    }
}