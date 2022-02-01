package components.createNewTask.createTask;

import DTO.TargetDTO;
import Utils.HttpClientUtil;
import com.google.gson.Gson;
import components.createNewTask.compilationTask.CompilationTaskController;
import components.createNewTask.processLogs.ProcessLogController;
import components.createNewTask.simulationTask.SimulationTaskController;
import components.basicInformation.TargetsTableViewRow;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static Utils.Constants.LINE_SEPARATOR;
import static Utils.Constants.UPLOAD_TASK;

public class CreateTaskController implements Controller {

    // Controllers
    private SimulationTaskController simulationTaskController;
    private CompilationTaskController compilationTaskController;
    private MainAppController mainAppController;
    private ProcessLogController processLogController;
    private Node nodeController;
    private boolean parentsAndChildrenIndicator = false;
    //UI
    @FXML private ComboBox<String> taskTypeCombo;
    @FXML private GridPane taskSettingsGridPane;
    @FXML private GridPane targetListGridPane;
    @FXML private CheckBox withParentsCheckBox;
    @FXML private CheckBox withChildrenCheckBox;
    @FXML private Button deselectAllTargetsButton; //new
    @FXML private Button selectAllTargetsButton; //new
    @FXML private Button uploadTaskButton;
    @FXML private TextField taskNameTextField;
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
        this.simulationTaskController = (SimulationTaskController) genericControllersInit("/components/createNewTask/simulationTask/simulationTask.fxml");
        this.compilationTaskController = (CompilationTaskController) genericControllersInit("/components/createNewTask/compilationTask/compilationTask.fxml");
        this.processLogController = (ProcessLogController) genericControllersInit("/components/createNewTask/processLogs/processLogs.fxml");
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
        isCompilationSelected.set(false);
        this.processLogController.initializeProcessLog();
        this.compilationTaskController.initializeCompilationTask();
        bindTargetsToButton();
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

                        } catch (TargetNotFoundException | XMLException ignore) {}
                    }

                    if (isWithParentsSelected.get()) {
                        //      call whatif method for x name?
                        try {
                            List<String> listOfEffected = mainAppController.getAllEffectedTargetsAdapter(x.getTargetName(), "Required For");
                            selectAllInvolved(listOfEffected);
                        } catch (TargetNotFoundException | XMLException ignore) {}
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
    }

    private void bindTargetsToButton(){
        ObservableList<CheckBox> checkBoxes = getCheckBoxes();
        SimpleBooleanProperty isCompilation = getIsCompilationSelectedProperty();
        SimpleBooleanProperty isSourceSelected = getIsSourceSelectedProperty();
        SimpleBooleanProperty isDestinationSelected = getIsDestinationSelectedProperty();
        this.uploadTaskButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        ()->!checkBoxes.stream().anyMatch(CheckBox::isSelected),
                        checkBoxes.stream().map(x->x.selectedProperty()).toArray(Observable[]::new)
                ).or(this.taskNameTextField.textProperty().isEmpty()).or(isCompilation.and(isSourceSelected.not().or(isDestinationSelected.not())))
        );
    }

    private TaskController genericControllersInit(String str) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(str));
        Node node = loader.load();
        TaskController ctr = loader.getController();
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

    }

    public void updateProcessLog(List<TargetDTO> newTargetStatus) {
        this.processLogController.updateProcess(newTargetStatus);
    }

    public TargetDTO getTargetInformationWhenTableClicked(String targetName) throws XMLException, TargetNotFoundException {
        return this.mainAppController.getInformationOnTarget(targetName);
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

    public List<String> getAllEffectedTargets(String targetName, String depends_on) throws TargetNotFoundException, XMLException {
        return this.mainAppController.getAllEffectedTargets(targetName,depends_on);
    }

    public Stage getPrimaryStage() {
        return this.mainAppController.getPrimaryStage();
    }

    @FXML
    void uploadTaskButtonAction(ActionEvent event) {
        Alert p = new Alert(Alert.AlertType.ERROR);
        List<String> selectedTargets = new LinkedList<>();
        for (TargetsTableViewRow row : table.getItems()) {
            if (row.getCheckBox().isSelected())
                selectedTargets.add(row.getTargetName());
        }

        String body =
                "targets=" + new Gson().toJson(selectedTargets) + LINE_SEPARATOR +
                        "userName=" + this.mainAppController.getCurrentUserName() + LINE_SEPARATOR +
                        "graphName=" + this.mainAppController.getSelectedGraphDTOFromDashboard().getGraphName() + LINE_SEPARATOR +
                        "taskName=" + this.taskNameTextField.getText() + LINE_SEPARATOR;

        if(this.taskTypeCombo.getSelectionModel().getSelectedItem().compareTo("Simulation") == 0) {
            Integer time = (int) this.simulationTaskController.getProcessTime();
            String time_option = simulationTaskController.getTimeOption().toUpperCase();
            Double successRates = this.simulationTaskController.getSuccessRates();
            Double warningRates = this.simulationTaskController.getWarningRates();

            body +=
                    "time=" + time + LINE_SEPARATOR +
                    "time_option=" + time_option + LINE_SEPARATOR +
                    "successRates=" + successRates + LINE_SEPARATOR +
                    "warningRates=" + warningRates + LINE_SEPARATOR +
                    "taskType=simulation";

        }
        else { //Comp
            String sourceFolder = this.compilationTaskController.getSourceFolder();
            String destinationFolder = this.compilationTaskController.getDestinationFolder();

            body += "source="+ sourceFolder + LINE_SEPARATOR +
                    "destination=" + destinationFolder + LINE_SEPARATOR +
                    "taskType=compilation";
        }

            HttpClientUtil.postRequest(RequestBody.create(body.getBytes()) , new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> {
                        Alert failLoginPopup = new Alert(Alert.AlertType.ERROR);
                        failLoginPopup.setHeaderText("Upload Task Error!");
                        failLoginPopup.setContentText("Something went wrong: " + e.getMessage());
                        failLoginPopup.show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() != 200) {
                        String responseBody = response.body().string();
                        p.setHeaderText("Upload Task Error!");
                        p.setAlertType(Alert.AlertType.ERROR);
                        p.setContentText("Something went wrong: " + responseBody);
                    }
                    else {
                        p.setContentText(response.body().string());
                        Platform.runLater(() -> {
                            p.setAlertType(Alert.AlertType.INFORMATION);
                            p.setHeaderText("Success!");

                            try { //Go back to dashboard when succeeded.
                                Method privateMethod = MainAppController.class.getDeclaredMethod("dashboardButtonAction", ActionEvent.class);
                                privateMethod.setAccessible(true);
                                privateMethod.invoke(mainAppController , new ActionEvent());
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    Platform.runLater(() -> p.show() );
                }
            }, UPLOAD_TASK);
        }
        //   String source = this.compilationTaskController.getSourceFolder();
        //   String destination = this.compilationTaskController.getDestinationFolder();
        //   this.mainAppController.activateCompTask(selectedTargets, source, destination, AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH ,consumeWhenFinished);
 }