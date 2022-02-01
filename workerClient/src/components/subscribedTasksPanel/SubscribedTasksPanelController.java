package components.subscribedTasksPanel;

import components.mainApp.ControllerW;
import components.mainApp.MainAppControllerW;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;

public class SubscribedTasksPanelController implements ControllerW {

    //Controllers
    private MainAppControllerW mainAppControllerW;
    private Node nodeController;
    //
    //UI
    @FXML private Label threadsOnWorkLabel;
    @FXML private Label availableThreadsLabel;
    @FXML private TableColumn<?, ?> targetTableColumn;
    @FXML private TableColumn<?, ?> leftTaskTableColumn;
    @FXML private TableColumn<?, ?> taskTypeTableColumn;
    @FXML private TableColumn<?, ?> statusTableColumn;
    @FXML private TableColumn<?, ?> priceTableColumn;
    @FXML private TableColumn<?, ?> rightTaskTableColumn;
    @FXML private TableColumn<?, ?> workersAmountTableColumn;
    @FXML private TableColumn<?, ?> progressTableColumn;
    @FXML private TableColumn<?, ?> targetsCompletedTableColumn;
    @FXML private TableColumn<?, ?> moneyCollectedTableColumn;
    @FXML private TextArea logTextArea;
    @FXML private Button pauseResumeButton;
    @FXML private Button unregisterButton;
    @FXML private Label moneyLabel;

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

    @FXML
    void pauseResumeButtonAction(ActionEvent event) {

    }

    @FXML
    void unregisterButtonAction(ActionEvent event) {

    }
}
