package components.gifAnimation;

import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class gifAnimationController implements Controller {

    private MainAppController mainAppController;
    private Node nodeController;

    @FXML private Button startAnimation;
    @FXML private Button stopAnimation;
    @FXML private ImageView myImage;

    private SequentialTransition seqT;
    private SimpleBooleanProperty isAnimationPlay;

    final Duration SEC_500 = Duration.millis(500);
    final Duration SEC_250 = Duration.millis(250);

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
    private void initialize(){

        TranslateTransition translateTransition = new TranslateTransition(SEC_250);
        translateTransition.setFromX(-100f);
        translateTransition.setToX(100f);
        translateTransition.setCycleCount(2);
        translateTransition.setAutoReverse(true);

        FadeTransition fadeTransition = new FadeTransition(SEC_250);
        fadeTransition.setFromValue(1.0f);
        fadeTransition.setToValue(0.3f);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true);

        RotateTransition rotateTransition = new RotateTransition(SEC_500);
        rotateTransition.setByAngle(360f);
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(true);

        ScaleTransition scaleTransition = new ScaleTransition(SEC_250);
        scaleTransition.setByX(1.5f);
        scaleTransition.setByY(1.5f);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);

        seqT = new SequentialTransition (this.myImage,translateTransition,fadeTransition,rotateTransition,scaleTransition);

        this.isAnimationPlay = new SimpleBooleanProperty(false);
        this.startAnimation.disableProperty().bind(this.isAnimationPlay);
    }

    @FXML
    void startAnimationAction(ActionEvent event) {
        this.isAnimationPlay.set(true);
        this.seqT.setOnFinished(e ->{
                    this.startAnimation.setText("Start Animation");
                    this.isAnimationPlay.set(false);
                }
        );

        this.startAnimation.setText("Resume");
        Platform.runLater(()->this.seqT.play());
    }

    @FXML
    void stopAnimationAction(ActionEvent event) {
        this.isAnimationPlay.set(false);
        Platform.runLater(()->this.seqT.pause());
    }

}
