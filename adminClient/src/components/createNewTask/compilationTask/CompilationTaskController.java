package components.createNewTask.compilationTask;

import components.createNewTask.createTask.TaskController;
import components.createNewTask.createTask.CreateTaskController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class CompilationTaskController implements TaskController {

    //Controllers
    private CreateTaskController createTaskController;
    private Node nodeController;
    //
    @FXML private Button selectSourceButton;
    @FXML private Label selectSourceLabel;
    @FXML private Button selectDestinationButton;
    @FXML private Label selectDestinationLabel;
    //
    private String sourcePath;
    private String destinationPath;
    private SimpleBooleanProperty isSourceFolderSelected;
    private SimpleBooleanProperty isDestinationFolderSelected;
    //

    public CompilationTaskController(){
        isSourceFolderSelected = new SimpleBooleanProperty(false);
        isDestinationFolderSelected = new SimpleBooleanProperty(false);

    }

    @FXML
    private void initialize() {
        this.selectSourceLabel.visibleProperty().bind(isSourceFolderSelected);
        this.selectDestinationLabel.visibleProperty().bind(isDestinationFolderSelected);
    }

    @Override
    public void setMainActivateTaskController(CreateTaskController createTaskController) {
        this.createTaskController = createTaskController;
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
    void selectSourceButtonAction(ActionEvent event) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        //String currentPath = Paths.get("engine/src/resources/XOO").toAbsolutePath().normalize().toString();
        //fileChooser.setInitialDirectory(new File(currentPath));
        //
        File file = fileChooser.showDialog(this.createTaskController.getPrimaryStage());
        if (file == null)
            return;

        this.isSourceFolderSelected.set(true);
        this.sourcePath = file.getAbsolutePath();
        selectSourceButton.setTooltip(new Tooltip(this.sourcePath));

    }

    @FXML
    void selectDestinationButtonAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        //String currentPath = Paths.get("engine/src/resources/XOO/src").toAbsolutePath().normalize().toString();
        //directoryChooser.setInitialDirectory(new File(currentPath));

        File file = directoryChooser.showDialog(this.createTaskController.getPrimaryStage());
        if (file == null)
            return;

        this.isDestinationFolderSelected.set(true);
        this.destinationPath = file.getAbsolutePath();
        selectDestinationButton.setTooltip(new Tooltip(this.destinationPath));
    }

    public String getSourceFolder() {
        return this.sourcePath;
    }

    public String getDestinationFolder() {
        return this.destinationPath;
    }

    public void initializeCompilationTask() {
        this.isSourceFolderSelected.set(false);
        this.isDestinationFolderSelected.set(false);
        this.sourcePath = null;
        this.destinationPath = null;
    }

    public SimpleBooleanProperty getIsSourceFolderSelectedProperty() {
        return isSourceFolderSelected;
    }

    public SimpleBooleanProperty getIsDestinationFolderSelectedProperty() {
        return isDestinationFolderSelected;
    }
}