package components.graphMainComponent;


import components.basicInformation.BasicInformationController;
import components.cycle.CycleController;
import components.findPath.FindPathController;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import components.whatIf.WhatIfController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;

public class GraphMainComponentController implements Controller {
    //Controllers
    private MainAppController mainAppController;
    private Node nodeController;

    @FXML private ScrollPane showGraphInformationComponent;
    @FXML private BasicInformationController showGraphInformationComponentController;

    @FXML private ScrollPane checkForCycleComponent;
    @FXML private CycleController checkForCycleComponentController;

    @FXML private ScrollPane findPathComponent;
    @FXML private FindPathController findPathComponentController;

    @FXML private ScrollPane whatIfComponent;
    @FXML private WhatIfController whatIfComponentController;

    @FXML
    private void initialize(){
        showGraphInformationComponentController.setMainAppControllerTest(this);
        checkForCycleComponentController.setMainAppControllerTest(this);
        findPathComponentController.setMainAppControllerTest(this);
        whatIfComponentController.setMainAppControllerTest(this);
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

    public void initializeGraphMainComponent(){
        showGraphInformationComponentController.setMainAppController(this.mainAppController);
        checkForCycleComponentController.setMainAppController(this.mainAppController);
        findPathComponentController.setMainAppController(this.mainAppController);
        whatIfComponentController.setMainAppController(this.mainAppController);
    }

    public void initializeGraphMainSubComponent() {
        //this.showGraphInformationComponentController = basicInformationController;
        try {
            this.showGraphInformationComponentController.initializeBasicInformationController();
            this.checkForCycleComponentController.initializeCycle();
            this.findPathComponentController.initializeFindPath();
            this.whatIfComponentController.initializeWhatIfController();

        }
        catch (Exception e){ //todo delete when done
            System.out.println(e);
        }
    }

    public String test()
    {
        return "TESSSSSSSSSSSSSSSSYYYYYTTTTTTTTTTTTTTTt";
    } //todo delete when done

}
