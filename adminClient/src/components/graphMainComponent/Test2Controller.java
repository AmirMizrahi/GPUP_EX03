package components.graphMainComponent;


import components.basicInformation.BasicInformationController;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;

public class Test2Controller {
    //Controllers
    private MainAppController mainAppController;
    private Node nodeController;

    @FXML private Tab showGraphInformationComponent;
    @FXML private BasicInformationController showGraphInformationComponentController;

    @FXML
    private void initialize(){
        showGraphInformationComponentController.setMainAppControllerTest(this);
    }

    //@Override
    public void setMainAppController(MainAppController newMainAppController) {
        this.mainAppController = newMainAppController;
    }
    //@Override
    public Node getNodeController(){
        return this.nodeController;
    }
    //@Override
    public void setNodeController(Node node){
        this.nodeController = node;
    }

}
