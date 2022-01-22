package components.activateTask.mainActivateTask;

import DTO.SerialSetDTO;
import DTO.TargetDTO;
import components.activateTask.compilationTask.CompilationTaskController;
import components.activateTask.processLogs.ProcessLogController;
import components.activateTask.simulationTask.SimulationTaskController;
import components.basicInformation.TargetsTableViewRow;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import tasks.AbstractTask;
import tasks.SimulationTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class MainActivateTaskController implements Controller {

    // Controllers
    private SimulationTaskController simulationTaskController;
    private CompilationTaskController compilationTaskController;
    private MainAppController mainAppController;
    private ProcessLogController processLogController;
    private Node nodeController;
    private boolean parentsAndChildrenIndicator = false;
    //UI
    @FXML private Spinner<Integer> parallelTaskAmountSpinner;
    @FXML private ToggleGroup performTaskToggle;
    @FXML private RadioButton fromScratchRadio;
    @FXML private RadioButton incrementalRadio;
    @FXML private ComboBox<String> taskTypeCombo;
    @FXML private GridPane gridPaneMainActivateTaskRight;
    @FXML private GridPane taskSettingsGridPane;
    @FXML private GridPane targetListGridPane;
    @FXML private CheckBox withParentsCheckBox;
    @FXML private CheckBox withChildrenCheckBox;
    @FXML private Button deselectAllTargetsButton; //new
    @FXML private Button selectAllTargetsButton; //new
    private TableView<TargetsTableViewRow> table;
    //Properties
    private SimpleBooleanProperty isWithChildrenSelected;
    private SimpleBooleanProperty isWithParentsSelected;
    private SimpleBooleanProperty isCompilationSelected;
    //
    private ObservableList<CheckBox> checkBoxes= FXCollections.observableArrayList();

    @FXML
    private void initialize() throws IOException {
        isCompilationSelected = new SimpleBooleanProperty(false);
        this.simulationTaskController = (SimulationTaskController) genericControllersInit("/components/activateTask/simulationTask/simulationTask.fxml");
        this.compilationTaskController = (CompilationTaskController) genericControllersInit("/components/activateTask/compilationTask/compilationTask.fxml");
        this.processLogController = (ProcessLogController) genericControllersInit("/components/activateTask/processLogs/processLogs.fxml");
        isWithChildrenSelected = new SimpleBooleanProperty(false);
        isWithParentsSelected = new SimpleBooleanProperty(false);

        //new!
        this.deselectAllTargetsButton.setOnAction(e->{
            for (TargetsTableViewRow x : table.getItems()) {
                if (x.getCheckBox().isSelected() && !x.getCheckBox().isDisable())
                    x.getCheckBox().setSelected(false);
            }
        });

        //new!
        this.selectAllTargetsButton.setOnAction(e->{
            for (TargetsTableViewRow x : table.getItems()) {
                if (!x.getCheckBox().isSelected() && !x.getCheckBox().isDisable())
                    x.getCheckBox().setSelected(true);
            }
        });
    }

    public void initializeMainActivateTask(){
        int parallelTaskAmount = mainAppController.getMaxParallelTaskAmount();
        isCompilationSelected.set(false);
        SpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,parallelTaskAmount,1);
        this.parallelTaskAmountSpinner.setValueFactory(factory);
        this.processLogController.initializeProcessLog();
        this.compilationTaskController.initializeCompilationTask();
        this.processLogController.bindTargetsToButton();
        //this.taskTypeCombo.getItems().clear();
        if (this.taskTypeCombo.getItems().size() == 0) {
            List<String> TasksNames = this.mainAppController.getTasksNames();
            this.taskTypeCombo.getItems().addAll(TasksNames);
        }
        this.taskTypeCombo.getSelectionModel().select(0);
        taskSettingsGridPane.getChildren().remove(0); //move to property
        taskSettingsGridPane.getChildren().add(getSimulationTaskController().getNodeController());

        this.taskTypeCombo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue.equals("Simulation")){
                    taskSettingsGridPane.getChildren().remove(0); //move to property
                    taskSettingsGridPane.getChildren().add(simulationTaskController.getNodeController());
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            taskSettingsGridPane.getChildren().remove(0); //move to property
                            taskSettingsGridPane.getChildren().add(simulationTaskController.getNodeController());
                        }
                        });
                }
                else if(newValue.equals("Compilation")){
                    taskSettingsGridPane.getChildren().remove(0); //move to property
                    taskSettingsGridPane.getChildren().add(compilationTaskController.getNodeController());
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            taskSettingsGridPane.getChildren().remove(0); //move to property
                            taskSettingsGridPane.getChildren().add(compilationTaskController.getNodeController());
                        }
                    });};
            }
        });

        this.parallelTaskAmountSpinner.disableProperty().bind(this.processLogController.getEnableThreadsChangeProperty().not());
        updateTargetTable();
        doWhenWhatIfsAreSelected();
    }

    private void doWhenWhatIfsAreSelected() {
        for (TargetsTableViewRow x : table.getItems()) {
            x.getCheckBox().selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!x.getCheckBox().isSelected() || parentsAndChildrenIndicator)
                        return;
                    parentsAndChildrenIndicator = true;
                    //If with children is marked:
                    if (isWithChildrenSelected.get()) {
                        //      call whatif method for x name?
                        try {
                            List<String> listOfEffected = mainAppController.getAllEffectedTargetsAdapter(x.getTargetName(), "Depends On");
                            selectAllInvolved(listOfEffected);

                        } catch (TargetNotFoundException ignore) {}
                    }

                    if (isWithParentsSelected.get()) {
                        //      call whatif method for x name?
                        try {
                            List<String> listOfEffected = mainAppController.getAllEffectedTargetsAdapter(x.getTargetName(), "Required For");
                            selectAllInvolved(listOfEffected);
                        } catch (TargetNotFoundException ignore) {}
                    }
                    parentsAndChildrenIndicator = false;
                }
            });
        }
    }

    private void selectAllInvolved(List<String> listOfEffected){
        //  foreach on returned list
        //         mark his checkBox;
        listOfEffected.forEach(targetName -> {
            for (TargetsTableViewRow row : table.getItems()) {
                if (row.getTargetName().compareTo(targetName) == 0)
                    row.getCheckBox().setSelected(true);
            }
        });
    }

    private void updateTargetTable(){
        //https://www.youtube.com/watch?v=Z9sAuCQI5qo&ab_channel=CoolITHelp
        table = new TableView<>();
        table.getColumns().addAll(this.mainAppController.getTargetsTableView().getColumns());
        table.setItems(this.mainAppController.getTargetsTableView().getItems());

        final TableColumn<TargetsTableViewRow, Boolean> loadedColumn = new TableColumn<>("Selection");
        loadedColumn.setCellValueFactory(new PropertyValueFactory<>("checkBox"));

        //loadedColumn.setCellFactory(tc -> new CheckBoxTableCell<>());
        loadedColumn.setStyle("-fx-alignment: CENTER"); //Aligment!
        table.getColumns().add(0,loadedColumn);
        table.setEditable(true);

        targetListGridPane.add(table,0,1);
        gridPaneMainActivateTaskRight.getChildren().clear();
        gridPaneMainActivateTaskRight.getChildren().add(this.processLogController.getNodeController());
    }

    private ActivateTaskController genericControllersInit(String str) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(str));
        Node node = loader.load();
        ActivateTaskController ctr = loader.getController();
        ctr.setNodeController(node);
        ctr.setMainActivateTaskController(this);

        return ctr;
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

    private SimulationTaskController getSimulationTaskController(){
        return this.simulationTaskController;
    }

    private CompilationTaskController getCompilationTaskController(){
        return this.compilationTaskController;
    }

    @FXML
    void withChildrenCheckBoxAction(ActionEvent event) {
        isWithChildrenSelected.set(!isWithChildrenSelected.get());
    }

    @FXML
    void withParentsCheckBoxAction(ActionEvent event) {
        isWithParentsSelected.set(!isWithParentsSelected.get());
    }

    public void activateTask(Consumer<File> consumeWhenFinished) {
        String temp = ((RadioButton)this.performTaskToggle.getSelectedToggle()).getText().toUpperCase();
        int tasksNumber = this.parallelTaskAmountSpinner.getValue();
        AbstractTask.WAYS_TO_START_SIM_TASK way = AbstractTask.WAYS_TO_START_SIM_TASK.valueOf(temp.replace(" ","_"));
        List<String> selectedTargets = new LinkedList<>();
        for (TargetsTableViewRow row : table.getItems()) {
            if (row.getCheckBox().isSelected())
                selectedTargets.add(row.getTargetName());
        }

        if(this.taskTypeCombo.getSelectionModel().getSelectedItem().compareTo("Simulation") == 0){
            Integer time = (int) this.simulationTaskController.getProcessTime();
            SimulationTask.TIME_OPTION time_option = SimulationTask.TIME_OPTION.valueOf(simulationTaskController.getTimeOption().toUpperCase());
            Double successRates = this.simulationTaskController.getSuccessRates();
            Double warningRates = this.simulationTaskController.getWarningRates();
            this.mainAppController.activateSimTask(selectedTargets, time,time_option,successRates,warningRates,way, tasksNumber, consumeWhenFinished);
        }
        else{
            String source = this.compilationTaskController.getSourceFolder();
            String destination = this.compilationTaskController.getDestinationFolder();
            this.mainAppController.activateCompTask(selectedTargets, source, destination, way , tasksNumber,consumeWhenFinished);

        }
    }

    @FXML
    void incrementalRadioAction(ActionEvent event) {
        ObservableList<CheckBox> checkBoxes = this.mainAppController.getCheckBoxesFromMainAppController();
        List<TargetDTO> targetsThatWereActivated = this.mainAppController.getAllTargetsThatWereActivated();
        List<String> targetsThatWereActivatedByName = new LinkedList<>();
        targetsThatWereActivated.forEach(targetDTO -> targetsThatWereActivatedByName.add(targetDTO.getTargetName()));

        checkBoxes.forEach(checkBox -> {
            checkBox.setDisable(true);
            if (targetsThatWereActivatedByName.contains(checkBox.getAccessibleText()))
                checkBox.setSelected(true);
            else
                checkBox.setSelected(false);
        });
    }

    @FXML
    void fromScratchRadioAction(ActionEvent event) {
        ObservableList<CheckBox> checkBoxes = this.mainAppController.getCheckBoxesFromMainAppController();
        checkBoxes.forEach(checkBox -> checkBox.setDisable(false));
    }

    public void updateProcessLog(List<TargetDTO> newTargetStatus) {
        this.processLogController.updateProcess(newTargetStatus);
    }

    public TargetDTO getTargetInformationWhenTableClicked(String targetName) throws XMLException, TargetNotFoundException {
        return this.mainAppController.getInformationOnTarget(targetName);
    }

    public List<SerialSetDTO> getSerialSetsForTarget(String targetName) throws XMLException {
        return this.mainAppController.getSerialSetDTOFromEngine().getSerialSetForTarget(targetName);
    }

    public ObservableList<CheckBox> getCheckBoxes() {
        return this.mainAppController.getCheckBoxesFromMainAppController();
    }

    @FXML
    void taskTypeComboAction(ActionEvent event) {
        if(this.taskTypeCombo.getSelectionModel().getSelectedItem().compareTo("Compilation") == 0)
            this.isCompilationSelected.set(true);
        else
            this.isCompilationSelected.set(false);
    }

    public SimpleBooleanProperty getIsCompilationSelectedProperty() {
        return isCompilationSelected;
    }

    public SimpleBooleanProperty getIsSourceSelectedProperty() {
        return compilationTaskController.getIsSourceFolderSelectedProperty();
    }

    public SimpleBooleanProperty getIsDestinationSelectedProperty() {
        return compilationTaskController.getIsDestinationFolderSelectedProperty();
    }

    public void reportTaskFinished() {
        this.mainAppController.close();
    }

    public void informPauseToMainActivateTaskController(boolean pause) {
        this.mainAppController.informPauseToMainAppController(pause);
    }

    public List<String> getAllEffectedTargets(String targetName, String depends_on) throws TargetNotFoundException {
        return this.mainAppController.getAllEffectedTargets(targetName,depends_on);
    }

    public Stage getPrimaryStage() {
        return this.mainAppController.getPrimaryStage();
    }

    @FXML
    void parallelTaskAmountOnMouseClicked(MouseEvent event) {
        if(this.processLogController.isPause())
            this.mainAppController.setNewThreadAmount(this.parallelTaskAmountSpinner.getValue());
    }
}