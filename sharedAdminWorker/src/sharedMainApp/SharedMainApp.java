package sharedMainApp;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
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



}
