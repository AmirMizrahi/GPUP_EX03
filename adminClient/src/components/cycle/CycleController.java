package components.cycle;

import components.mainApp.Controller;
import components.mainApp.MainAppController;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

import java.util.List;

public class CycleController implements Controller {

    //Controllers
    private MainAppController mainAppController;
    private Node nodeController;
    //
    //UI
    @FXML private Button startButton;
    @FXML private ComboBox<String> targetComboBox;
    @FXML private ListView<String> resultListView;
    //Properties
    private SimpleBooleanProperty isTargetSelected;
    //

    public CycleController(){
        isTargetSelected = new SimpleBooleanProperty(false);
    }

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

    @FXML public void initialize(){
        startButton.setDisable(true);
        startButton.disableProperty().bind(isTargetSelected.not());
    }

    @FXML
    void startButtonAction(ActionEvent event) throws XMLException, TargetNotFoundException {
        List<String> path = this.mainAppController.getCyclePath(this.targetComboBox.getValue());
        this.resultListView.getItems().clear();
        if (path.size() > 0){
            this.resultListView.getItems().add(0,String.format("Target %s is part of the following cycle:", this.targetComboBox.getValue()));
            this.resultListView.getItems().addAll(path);
        }
        else
            this.resultListView.getItems().add(0,String.format("Target %s isn't part of a cycle.", this.targetComboBox.getValue()));
    }

    @FXML
    void targetComboBoxAction(ActionEvent event) {
        this.isTargetSelected.set(true);
    }

    public void initializeCycle() throws XMLException {
        List<String> targetsNames = this.mainAppController.getTargetsNames();
        this.targetComboBox.getItems().clear();
        this.targetComboBox.getItems().addAll(targetsNames);
        this.resultListView.getItems().clear();
        this.isTargetSelected.set(false);
    }
}
