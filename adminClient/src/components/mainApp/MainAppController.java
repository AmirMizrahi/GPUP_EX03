package components.mainApp;
//In order to change "Actions On Graph" info per graph, please press the wanted graph from the dashboard list
import DTO.GraphDTO;
import DTO.TargetDTO;

import DTO.TaskDTO;
import Utils.HttpClientUtil;
import components.createNewTask.createTask.CreateTaskController;
import components.basicInformation.TargetsTableViewRow;
import components.dashboard.DashboardController;
import components.gifAnimation.gifAnimationController;
import components.graphMainComponent.GraphMainComponentController;
import components.graphviz.GraphvizController;
import components.login.LoginController;
import components.taskControlPanel.TaskControlPanelController;
import components.welcomeAnimation.welcomeAnimationController;
import components.welcomeToGPUP.welcomeToGPUPController;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import managers.Manager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import sharedControllers.sharedMainAppController;
import sharedDashboard.SharedDashboard;
import sharedLogin.SharedLogin;
import sharedMainApp.SharedMainApp;
import tasks.AbstractTask;
import tasks.SimulationTask;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;

import static Utils.Constants.TARGETS_REFRESH_RATE;
import static sharedMainApp.SharedMainApp.sharedOnLoggedIn;

public class MainAppController implements Closeable, sharedMainAppController {

    //
    private final Manager manager;
    private Stage primaryStage;
    private int maxParallelTaskAmount;
    private Timer timer;
    //
    //UI
    @FXML private GridPane gridPaneMainAppRight;
    @FXML private Button dashboardButton;
    @FXML private Button loadXMLButton;
    @FXML private Button actionsOnGraphButton;
    @FXML private Button createNewTaskButton;
    @FXML private Button taskControlPanelButton;
    @FXML private Button animationButton;
    @FXML private Button graphvizButton;
    @FXML private ComboBox<String> changeSkinComboBox;
    @FXML private Label serverStatusLabel;
    //Properties
    private SimpleBooleanProperty isFileSelected;
    private final SimpleBooleanProperty matchingUserName;
    private BooleanProperty isLoggedIn; //changed from SimpleBooleanProperty - does it matter?
    private SimpleStringProperty selectedGraph;
    private SimpleStringProperty selectedTask;
    // Controllers
    private LoginController loginController;
    private GraphMainComponentController graphMainComponentController;
    private welcomeToGPUPController welcomeToGpupController;
    private welcomeAnimationController welcomeAnimationController;
    private CreateTaskController createTaskController;
    private TaskControlPanelController taskControlPanelController;
    private gifAnimationController gifAnimationController;
    private GraphvizController graphvizController;
    private DashboardController dashboardController;
    //

    public MainAppController(){
        this.manager = new Manager();
        isFileSelected = new SimpleBooleanProperty(false);
        isLoggedIn = new SimpleBooleanProperty(false);
        selectedGraph = new SimpleStringProperty();
        selectedTask = new SimpleStringProperty();
        matchingUserName = new SimpleBooleanProperty(false);
    }


    @FXML private Label actionsOnGraphLabelDummy;
    @FXML private Label createNewTaskLabelDummy;
    @FXML private Label taskControlPanelLabelDummy;

    @FXML
    private void initialize() throws IOException {
        this.dashboardButton.disableProperty().bind(isLoggedIn.not());
        this.loadXMLButton.disableProperty().bind(isLoggedIn.not());
        this.actionsOnGraphButton.disableProperty().bind(selectedGraph.isEmpty());
        this.actionsOnGraphLabelDummy.setTooltip(new Tooltip("In order to use this option, click graph from Dashboard table"));
        this.actionsOnGraphButton.setTooltip(new Tooltip("In order to use this option, click graph from Dashboard table"));
        this.createNewTaskButton.disableProperty().bind(selectedGraph.isEmpty());
        this.createNewTaskLabelDummy.setTooltip(new Tooltip("In order to use this option, click graph from Dashboard table"));
        this.createNewTaskButton.setTooltip(new Tooltip("In order to use this option, click graph from Dashboard table"));
        this.taskControlPanelButton.disableProperty().bind(this.matchingUserName.not());
        this.taskControlPanelLabelDummy.setTooltip(new Tooltip("In order to use this option, click task (only ones you created) from Dashboard table"));
        this.taskControlPanelButton.setTooltip(new Tooltip("In order to use this option, click task (only ones you created) from Dashboard table"));
        this.animationButton.disableProperty().bind(selectedGraph.isEmpty());
        this.graphvizButton.disableProperty().bind(selectedGraph.isEmpty());
        this.serverStatusLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            String str;
            if (SharedMainApp.getServerOnProperty().get())
                str = "Server is ON";
            else
                str = "Server is OFF";
            return str;
        }, SharedMainApp.getServerOnProperty()));

        graphMainComponentController = (GraphMainComponentController) genericControllersInit("/components/graphMainComponent/graphMainComponent.fxml");
        graphMainComponentController.initializeGraphMainComponent(); //todo change this? this is setting mainapp controller to all SUB COMPONENTS
        loginController = (LoginController) genericControllersInit("/components/login/login.fxml");
        welcomeToGpupController = (welcomeToGPUPController) genericControllersInit("/components/welcomeToGPUP/welcomeToGPUP.fxml");
        welcomeAnimationController = (welcomeAnimationController) genericControllersInit("/components/welcomeAnimation/welcomeAnimation.fxml");
        createTaskController = (CreateTaskController) genericControllersInit("/components/createNewTask/createTask/createTask.fxml");
        taskControlPanelController = (TaskControlPanelController) genericControllersInit("/components/taskControlPanel/taskControlPanel.fxml");
        gifAnimationController = (gifAnimationController) genericControllersInit("/components/gifAnimation/gifAnimation.fxml");
        graphvizController = (GraphvizController) genericControllersInit("/components/graphviz/graphviz.fxml");
        dashboardController = (DashboardController) genericControllersInit("/components/dashboard/dashboard.fxml");

        this.changeSkinComboBox.getItems().addAll("No Skin", "Light Mode","Dark Mode", "Old School Mode");
        this.changeSkinComboBox.getSelectionModel().select(0);

        this.gridPaneMainAppRight.getChildren().remove(0);
        gridPaneMainAppRight.getChildren().add(this.loginController.getNodeController());
    }

    private Controller genericControllersInit(String str) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(str));
        Node node = loader.load();
        Controller ctr = loader.getController();
        ctr.setNodeController(node);
        ctr.setMainAppController(this);

        return ctr;
    }

    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @FXML
    private void loadXMLAction(ActionEvent event) {
        Alert p = new Alert(Alert.AlertType.ERROR);
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML File", "*.xml"));
        File file = fileChooser.showOpenDialog(this.primaryStage);

        if (file == null)
            return;
        HttpClientUtil.uploadFile(file, this.dashboardController.getLoggedInUserName(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    Alert failLoginPopup = new Alert(Alert.AlertType.ERROR);
                    failLoginPopup.setHeaderText("XML Error");
                    failLoginPopup.setContentText("Something went wrong: " + e.getMessage());
                    failLoginPopup.show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    p.setHeaderText("XML Error");
                    p.setAlertType(Alert.AlertType.ERROR);
                    p.setContentText("Something went wrong: " + responseBody);
                }
                else {
                    p.setContentText(response.body().string());
                    Platform.runLater(() -> {
                        p.setAlertType(Alert.AlertType.INFORMATION);
                        p.setHeaderText("Success!");
                    });
                }
                Platform.runLater(() -> p.show() );
            }
        });

        maxParallelTaskAmount = manager.getParallelTaskAmount(); //todo remove this
        isFileSelected.set(true);

    }

    public void loadAllDetailsToSubComponents() {
        this.graphMainComponentController.initializeGraphMainSubComponent(); //todo change this? --> this is calling the init of basicInfoGraph ( the sub-component that inside the main one)
        this.createTaskController.initializeMainActivateTask();
    }

    @FXML
    private void createNewTaskAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        //this.mainActivateTaskController.setParallelTaskAmount(parallelTaskAmount);
        gridPaneMainAppRight.getChildren().add(this.createTaskController.getNodeController());
    }

    @FXML
    void taskControlPanelButtonAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        //this.mainActivateTaskController.setParallelTaskAmount(parallelTaskAmount);
        gridPaneMainAppRight.getChildren().add(this.taskControlPanelController.getNodeController());
    }

    @FXML
    void animationAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        gridPaneMainAppRight.getChildren().add(this.gifAnimationController.getNodeController());
    }

    @FXML
    void graphvizAction(ActionEvent event) throws XMLException, IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose 'Graphviz' Location To Save");
        //fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showSaveDialog(this.primaryStage);

        if(file == null)
            return;

        this.graphvizController.initializeGraphvizController(file.getAbsolutePath());

        gridPaneMainAppRight.getChildren().remove(0); //move to property
        gridPaneMainAppRight.getChildren().add(this.graphvizController.getNodeController());
    }

    public GraphDTO getSelectedGraphDTOFromDashboard() {
        return dashboardController.getSelectedGraph();
    }

    public TaskDTO getSelectedTaskDTOFromDashboard() {
        return SharedDashboard.getSelectedTask(selectedTask);
    }

    public List<String> getTargetsNames() {
        List<String> targetsNames = new LinkedList<>();
        this.getSelectedGraphDTOFromDashboard().getAllTargets().forEach(targetDTO -> targetsNames.add(targetDTO.getTargetName()));
        return targetsNames;
    }

    public List<String> getAllPathsBetweenTwoTargets(String source, String destination, RadioButton selectedRadioButton) {
        String relation;
        if (selectedRadioButton.getText().equals("Depends On"))
            relation = Manager.DependencyRelation.DEPENDS_ON.name();
        else
            relation = Manager.DependencyRelation.REQUIRED_FOR.name();

        List<String> path = this.getSelectedGraphDTOFromDashboard().findAllPathsBetweenTwoTargets(source, destination, relation);

        return path;
    }

    public List<String> getCyclePath(String value) {
        return getSelectedGraphDTOFromDashboard().checkIfTargetIsPartOfCycle(value);
    }

    //Used by What-if
    public List<String> getAllEffectedTargets(String targetName, String selectedRadioButton) {
        List<String> returnedList =  new LinkedList<>();

        List<TargetDTO> dtos = getSelectedGraphDTOFromDashboard().getAllEffected(targetName,selectedRadioButton);
        dtos.forEach(targetDTO ->returnedList.add(targetDTO.getTargetName()));
        return returnedList;
    }

    public List<String> getAllEffectedTargetsAdapter(String targetName, String textInRadioButton) throws TargetNotFoundException, XMLException {
        return getAllEffectedTargets(targetName, textInRadioButton);
    }

    public int getMaxParallelTaskAmount() {
        return this.maxParallelTaskAmount;
    }

    public List<String> getTasksNames() {
        return this.manager.getTasksNames();
    }

    public TableView<TargetsTableViewRow> getTargetsTableView(){
        return this.graphMainComponentController.getTargetTable();
    }

    public void activateSimTask(List<String> selectedTargets, Integer time, SimulationTask.TIME_OPTION time_option, Double successRates,
                                Double warningRates, AbstractTask.WAYS_TO_START_SIM_TASK way, Consumer<File> consumeWhenFinished) {
        List<Consumer<String>> consumerList = new LinkedList<>();
        Consumer<String> consumer1 = System.out::println;
        consumerList.add(consumer1);
        try {
            this.startTaskControlPanelRefresher();
            this.manager.activateSimulationTask(selectedTargets, time,time_option, successRates,warningRates, way,consumerList, consumeWhenFinished, "emptyString"); //todo enter current selected graph name
        } catch (XMLException | IOException | InterruptedException | TargetNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void activateCompTask(List<String> selectedTargets, String source, String destination, AbstractTask.WAYS_TO_START_SIM_TASK way,
                                 Consumer<File> consumeWhenFinished) {
        List<Consumer<String>> consumerList = new LinkedList<>();
        Consumer<String> consumer1 = System.out::println;
        consumerList.add(consumer1);
        try {
            this.startTaskControlPanelRefresher();
            this.manager.activateCompilationTask(selectedTargets, source, destination, way,consumerList,consumeWhenFinished, "emptyString");//todo enter current selected graph name
        } catch (XMLException | IOException | InterruptedException | TargetNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startTaskControlPanelRefresher(){
        //Consumer =>
                            /// List<TargetDTO> a = getFromEngine
                            /// UpdateAllTables(a)

        Consumer<Boolean> refresherConsumer = new Consumer() {
            @Override
            public void accept(Object o) {
                //List<TargetDTO> newTargetStatus = manager.getCurrentTargetsStatus();
                //createTaskController.updateProcessLog(newTargetStatus);
                taskControlPanelController.refreshPanel(getSelectedTaskDTOFromDashboard());
            }
        };

        TargetRefresher targetRefresher = new TargetRefresher(refresherConsumer);
        timer = new Timer();
        timer.schedule(targetRefresher, TARGETS_REFRESH_RATE, TARGETS_REFRESH_RATE);
    }

    public TargetDTO getInformationOnTarget(String targetName) throws XMLException, TargetNotFoundException {
        return this.manager.showInformationAboutSpecificTargetMG("",targetName);//todo
    }

    @Override
    public void close() {
        if(timer!= null){
            timer.cancel();
        }
    }

    public ObservableList<CheckBox> getCheckBoxesFromMainAppController() {
        return this.graphMainComponentController.getCheckBoxes();
    }

    public List<TargetDTO> getAllTargetsThatWereActivated() {
        return this.manager.getAllTargetsThatWereActivatedMG();
    }

    @FXML
    void changeSkinComboBoxAction(ActionEvent event) {
        String skinName = this.changeSkinComboBox.getSelectionModel().getSelectedItem();
        Scene sceneToChange = this.primaryStage.getScene();
        sceneToChange.getStylesheets().clear();

        switch (skinName) {
            case "No Skin":
                //todo . for backwards is pitfall?
                break;
            case "Light Mode":
                sceneToChange.getStylesheets().add(getClass().getResource("/cssSkins/LightTheme.css").toExternalForm());
                break;
            case "Dark Mode":
                sceneToChange.getStylesheets().add(getClass().getResource("/cssSkins/DarkTheme.css").toExternalForm());
                break;
            case "Old School Mode":
                sceneToChange.getStylesheets().add(getClass().getResource("/cssSkins/OldSchoolTheme.css").toExternalForm());
                break;
        }
    }

    public void informPauseToMainAppController(boolean pause) {
        this.manager.informPauseToManager(pause);
    }

    public void setNewThreadAmount(Integer value) {
        this.manager.setNewThreadAmountMG(value);
    }

    @Override
    public void onLoggedIn() {
//        this.isLoggedIn.set(true);
//        gridPaneMainAppRight.getChildren().remove(0); //move to property
//        gridPaneMainAppRight.getChildren().add(this.dashboardController.getNodeController());
        sharedOnLoggedIn(gridPaneMainAppRight,isLoggedIn,this.dashboardController.getNodeController());
        this.dashboardController.initializeDashboardController(SharedLogin.userNamePropertyProperty(), this.selectedGraph, this.selectedTask, this.matchingUserName);
        startTaskControlPanelRefresher();
    }

    @FXML
    void dashboardButtonAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        gridPaneMainAppRight.getChildren().add(this.dashboardController.getNodeController());
    }

    @FXML
    void ActionsOnGraphAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0);
        gridPaneMainAppRight.getChildren().add(this.graphMainComponentController.getNodeController());
    }

    public String getCurrentUserName() {
        return this.dashboardController.getLoggedInUserName();
    }
}