package components.mainApp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Test extends Application{
        @Override
        public void start(Stage primaryStage) throws Exception {
            FXMLLoader loader = new FXMLLoader();

            // load main fxml
            URL mainFXML = getClass().getResource("components/mainApp/info.fxml");
            loader.setLocation(mainFXML);
            Parent rootContainer = loader.load();
            // wire up controller
            //  mainAppController = loader.getController();
            //  mainAppController.setPrimaryStage(primaryStage);



            // set stage
            primaryStage.setTitle("G.P.U.P - Info");
            Scene scene = new Scene(rootContainer);

            primaryStage.setMinHeight(400f);
            primaryStage.setMinWidth(600f);
            primaryStage.setHeight(800f);
            primaryStage.setWidth(1500f);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
}
