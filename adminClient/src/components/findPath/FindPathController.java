package components.findPath;

import components.graphMainComponent.GraphMainComponentController;
import components.mainApp.Controller;
import components.mainApp.MainAppController;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.List;

public class FindPathController implements Controller {

    //Controllers
    private MainAppController mainAppController;
    private GraphMainComponentController mainAppControllerTest;
    private Node nodeController;
    //
    //UI
    @FXML private ComboBox<String> destinationComboBox;
    @FXML private ToggleGroup relation;
    @FXML private RadioButton dependsOnRadio;
    @FXML private RadioButton requiredForRadio;
    @FXML private ComboBox<String> sourceComboBox;
    @FXML private ListView<String> resultListView;
    @FXML private Button startButton;
    //
    //Properties
    private SimpleBooleanProperty isSourceSelected;
    private SimpleBooleanProperty isDestinationSelected;
    private SimpleBooleanProperty isDependsOnSelected;
    private SimpleBooleanProperty isRequiredForSelected;
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

    public void setMainAppControllerTest(GraphMainComponentController newMainAppController) {
        this.mainAppControllerTest = newMainAppController;
    }

    @FXML
    private void initialize(){
        isSourceSelected = new SimpleBooleanProperty(false);
        isDestinationSelected = new SimpleBooleanProperty(false);
        isDependsOnSelected = new SimpleBooleanProperty(false);
        isRequiredForSelected = new SimpleBooleanProperty(false);

        BooleanBinding booleanBinding = isSourceSelected.not().or(isDestinationSelected.not()).
                or(isDependsOnSelected.not().and(isRequiredForSelected.not()));
        startButton.setDisable(true);
        startButton.disableProperty().bind(booleanBinding);
    }

    public void initializeFindPath() {
        List<String> targetsNames = this.mainAppController.getTargetsNames();
        this.sourceComboBox.getItems().clear();
        this.sourceComboBox.getItems().addAll(targetsNames);
        this.destinationComboBox.getItems().clear();
        this.destinationComboBox.getItems().addAll(targetsNames);
        this.dependsOnRadio.setSelected(false);
        this.requiredForRadio.setSelected(false);
        this.resultListView.getItems().clear();
        this.isSourceSelected.set(false);
        this.isDestinationSelected.set(false);
        this.isDependsOnSelected.set(false);
        this.isRequiredForSelected.set(false);
    }

    @FXML
    void startButtonAction(ActionEvent event) {
        List<String> allPaths;
        allPaths = this.mainAppController.getAllPathsBetweenTwoTargets(this.sourceComboBox.getValue(), this.destinationComboBox.getValue(), (RadioButton)relation.getSelectedToggle());
        this.resultListView.getItems().clear();
        this.resultListView.getItems().addAll(allPaths);
    }

    @FXML
    void sourceComboBoxAction(ActionEvent event) {
        this.isSourceSelected.set(true);
    }

    @FXML
    void destinationComboBoxAction(ActionEvent event) {
        this.isDestinationSelected.set(true);
    }

    @FXML
    void dependsOnRadioAction(ActionEvent event) {
        this.isDependsOnSelected.set(true);
    }

    @FXML
    void requiredForRadioAction(ActionEvent event) {
        this.isRequiredForSelected.set(true);
    }

}