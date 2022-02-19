package components.whatIf;

import components.graphMainComponent.GraphMainComponentController;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.util.List;

public class WhatIfController implements Controller {

    //Controllers
    private MainAppController mainAppController;
    private GraphMainComponentController mainAppControllerTest;
    private Node nodeController;
    //
    //UI
    @FXML private Button startButton;
    @FXML private RadioButton dependsOnRadio;
    @FXML private ToggleGroup relation;
    @FXML private RadioButton requiredForRadio;
    @FXML private ComboBox<String> targetComboBox;
    @FXML private ListView<String> resultListView;
    //
    //Properties
    private SimpleBooleanProperty isTargetSelected;
    private SimpleBooleanProperty isDependsOnSelected;
    private SimpleBooleanProperty isRequiredForSelected;
    //

    public WhatIfController(){
        isTargetSelected = new SimpleBooleanProperty(false);
        isDependsOnSelected = new SimpleBooleanProperty(false);
        isRequiredForSelected = new SimpleBooleanProperty(false);
    }
    @Override
    public void setMainAppController(MainAppController newMainAppController) {
        this.mainAppController = newMainAppController;
    }

    public void setMainAppControllerTest(GraphMainComponentController newMainAppController) {
        this.mainAppControllerTest = newMainAppController;
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
        BooleanBinding booleanBinding = isTargetSelected.not().
                or(isDependsOnSelected.not().and(isRequiredForSelected.not()));
        startButton.setDisable(true);
        startButton.disableProperty().bind(booleanBinding);
    }

    public void initializeWhatIfController() {
        List<String> targetsNames = this.mainAppController.getTargetsNames();
        this.targetComboBox.getItems().clear();
        this.targetComboBox.getItems().addAll(targetsNames);
        this.dependsOnRadio.setSelected(false);
        this.requiredForRadio.setSelected(false);
        this.resultListView.getItems().clear();
        this.isTargetSelected.set(false);
        this.isDependsOnSelected.set(false);
        this.isRequiredForSelected.set(false);
    }

    @FXML
    void targetComboBoxAction(ActionEvent event) {
        this.isTargetSelected.set(true);
    }

    @FXML
    void dependsOnRadioAction(ActionEvent event) {
        this.isDependsOnSelected.set(true);
    }

    @FXML
    void requiredForRadioAction(ActionEvent event) {
        this.isRequiredForSelected.set(true);
    }

    @FXML
    void startButtonAction(ActionEvent event) {
        List<String> allEffectedTargets = this.mainAppController.getAllEffectedTargetsAdapter(this.targetComboBox.getValue(), ((RadioButton)relation.getSelectedToggle()).getText());
        this.resultListView.getItems().clear();
        this.resultListView.getItems().addAll(allEffectedTargets);
    }
}
