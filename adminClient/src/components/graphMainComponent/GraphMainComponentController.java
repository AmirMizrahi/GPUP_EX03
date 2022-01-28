package components.graphMainComponent;


import components.basicInformation.BasicInformationController;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
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

//    @FXML private TabPane test;
//    @FXML private Tab test2;
//    @FXML private GridPane test3;
    @FXML private ScrollPane showGraphInformationComponent;
    @FXML private BasicInformationController showGraphInformationComponentController;

    @FXML
    private void initialize(){
        showGraphInformationComponentController.setMainAppControllerTest(this);
    }

    public void anotherTest(){
        showGraphInformationComponentController.setMainAppController(this.mainAppController);
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

    public void initTest() {
        //this.showGraphInformationComponentController = basicInformationController;
        try {
            this.showGraphInformationComponentController.initializeBasicInformationController();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public String test()
    {
        return "TESSSSSSSSSSSSSSSSYYYYYTTTTTTTTTTTTTTTt";
    }

}
