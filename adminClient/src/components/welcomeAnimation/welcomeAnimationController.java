package components.welcomeAnimation;

import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class welcomeAnimationController implements Controller {

    @FXML private MainAppController mainAppController;
    private Node nodeController;

    @FXML private Button startAnimation;
    @FXML private Button stopAnimation;
    @FXML private ImageView myImage;

    private RotateTransition rotate;

    private SimpleBooleanProperty isAnimationPlay;

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
        this.rotate = new RotateTransition();
        rotate.setNode(myImage);
        rotate.setDuration(Duration.millis(2000));
        //rotate.setCycleCount(TranslateTransition.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setByAngle(360);
        rotate.setAxis(Rotate.Z_AXIS);

        this.isAnimationPlay = new SimpleBooleanProperty(false);
        this.startAnimation.disableProperty().bind(this.isAnimationPlay);
    }

    @FXML
    void startAnimationAction(ActionEvent event) {
        this.isAnimationPlay.set(true);
        this.rotate.setOnFinished(e ->{
            this.startAnimation.setText("Start Animation");
            this.isAnimationPlay.set(false);
        }
        );

        this.startAnimation.setText("Resume");
        Platform.runLater(()->this.rotate.play());
    }

    @FXML
    void stopAnimationAction(ActionEvent event) {
        this.isAnimationPlay.set(false);
        Platform.runLater(()->this.rotate.pause());
    }

    /*private void Animate() {
*//*

        //translate
        //translate*//*

        //rotate
        //rotate

        //fade
        //fade

*//*        //scale
        ScaleTransition scale = new ScaleTransition();
        scale.setNode(myImage);
        scale.setDuration(Duration.millis(2000));
        //scale.setCycleCount(TranslateTransition.INDEFINITE);
        scale.setInterpolator(Interpolator.LINEAR);
        scale.setByX(2.0);
        scale.setByY(2.0);
        scale.setAutoReverse(true);
        scale.play();
        //scale*//*
    }*/







}