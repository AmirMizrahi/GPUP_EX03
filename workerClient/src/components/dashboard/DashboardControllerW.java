package components.dashboard;

import components.mainApp.ControllerW;
import components.mainApp.MainAppControllerW;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

public class DashboardControllerW implements ControllerW {
    //Controllers
    private MainAppControllerW mainAppControllerW;
    private Node nodeController;

    @FXML private TableView<?> tasksTableView;
    @FXML private ListView<?> tasksListView;
    @FXML private TableView<?> usersTableView;
    @FXML private TableColumn<?, ?> userTableColumn;
    @FXML private TableColumn<?, ?> typeTableColumn;
    @FXML private Label loggedInLabel;
    @FXML void tasksTableViewOnClicked(MouseEvent event) {}


    @Override
    public void setMainAppControllerW(MainAppControllerW newMainAppControllerW) {
        this.mainAppControllerW = newMainAppControllerW;
    }
    @Override
    public Node getNodeController(){
        return this.nodeController;
    }
    @Override
    public void setNodeController(Node node){
        this.nodeController = node;
    }

}
