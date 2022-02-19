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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import sharedChat.SharedChat;
import sharedControllers.sharedMainAppController;
import sharedDashboard.SharedDashboard;
import sharedLogin.SharedLogin;
import sharedMainApp.SharedMainApp;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;

import static Utils.Constants.TARGET_REFRESH_RATE;
import static sharedMainApp.SharedMainApp.sharedOnLoggedIn;

public class MainAppController implements Closeable, sharedMainAppController {

    //
    private Stage primaryStage;
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
    @FXML private Button howToUseButton;
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
        this.dashboardButton.disableProperty().bind(isLoggedIn.not().or(SharedMainApp.getServerOnProperty().not()));
        this.loadXMLButton.disableProperty().bind(isLoggedIn.not().or(SharedMainApp.getServerOnProperty().not()));
        this.actionsOnGraphButton.disableProperty().bind(selectedGraph.isEmpty().or(SharedMainApp.getServerOnProperty().not()).or(this.isLoggedIn.not()));
        this.actionsOnGraphLabelDummy.setTooltip(new Tooltip("In order to use this option, click graph from Dashboard table"));
        this.actionsOnGraphButton.setTooltip(new Tooltip("In order to use this option, click graph from Dashboard table"));
        this.createNewTaskButton.disableProperty().bind(selectedGraph.isEmpty().or(SharedMainApp.getServerOnProperty().not()).or(this.isLoggedIn.not()));
        this.createNewTaskLabelDummy.setTooltip(new Tooltip("In order to use this option, click graph from Dashboard table"));
        this.createNewTaskButton.setTooltip(new Tooltip("In order to use this option, click graph from Dashboard table"));
        this.taskControlPanelButton.disableProperty().bind(this.matchingUserName.not().or(SharedMainApp.getServerOnProperty().not()).or(this.isLoggedIn.not()));
        this.taskControlPanelLabelDummy.setTooltip(new Tooltip("In order to use this option, click task (only ones you created) from Dashboard table"));
        this.taskControlPanelButton.setTooltip(new Tooltip("In order to use this option, click task (only ones you created) from Dashboard table"));
        this.animationButton.disableProperty().bind(selectedGraph.isEmpty().or(SharedMainApp.getServerOnProperty().not()).or(this.isLoggedIn.not()));
        this.graphvizButton.disableProperty().bind(selectedGraph.isEmpty().or(SharedMainApp.getServerOnProperty().not()).or(this.isLoggedIn.not()));
        this.serverStatusLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            String str;
            if (SharedMainApp.getServerOnProperty().get())
                str = "Server is ON";
            else {
                str = "Server is OFF";
                Platform.runLater(()->showLoginPage());
                this.isLoggedIn.set(false);
            }
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

    private void showLoginPage(){
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
                response.close();
            }
        });

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
        taskControlPanelController.refreshMap();
        taskControlPanelController.refreshButtons(getSelectedTaskDTOFromDashboard().getTaskStatus());
        taskControlPanelController.refreshLabels(getSelectedTaskDTOFromDashboard());
        gridPaneMainAppRight.getChildren().add(this.taskControlPanelController.getNodeController());
    }

    @FXML
    void animationAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        gridPaneMainAppRight.getChildren().add(this.gifAnimationController.getNodeController());
    }

    @FXML
    void graphvizAction(ActionEvent event) throws IOException {
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
            relation ="DEPENDS_ON";
        else
            relation = "REQUIRED_FOR";

        List<String> path = this.getSelectedGraphDTOFromDashboard().findAllPathsBetweenTwoTargets(source, destination, relation);

        return path;
    }

    public List<String> getCyclePath(String value) {
        return getSelectedGraphDTOFromDashboard().checkIfTargetIsPartOfCycle(value);
    }

    //Used by What-if
    public List<String> getAllEffectedTargets(String targetName, String selectedRadioButton, GraphDTO graphDTO) {
        List<String> returnedList =  new LinkedList<>();

        List<TargetDTO> dtos = graphDTO.getAllEffected(targetName,selectedRadioButton);
        dtos.forEach(targetDTO ->returnedList.add(targetDTO.getTargetName()));
        return returnedList;
    }

    public List<String> getAllEffectedTargetsAdapter(String targetName, String textInRadioButton) {
        return getAllEffectedTargets(targetName, textInRadioButton, getSelectedGraphDTOFromDashboard());
    }

    public TableView<TargetsTableViewRow> getTargetsTableView(){
        return this.graphMainComponentController.getTargetTable();
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
                taskControlPanelController.fillLogs(getSelectedTaskDTOFromDashboard());
            }
        };

        TargetRefresher targetRefresher = new TargetRefresher(refresherConsumer);
        timer = new Timer();
        timer.schedule(targetRefresher, TARGET_REFRESH_RATE, TARGET_REFRESH_RATE);
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

    @FXML
    void changeSkinComboBoxAction(ActionEvent event) {
        SharedMainApp.changeSkin(changeSkinComboBox,getClass(),this.primaryStage.getScene());
    }

    @FXML
    void howToUseAction(ActionEvent event) {
        howToUseButton.setOnMouseClicked((event2) -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL mainFXML = getClass().getResource("/sharedMainApp/info.fxml");
                fxmlLoader.setLocation(mainFXML);
                /*
                 * if "fx:controller" is not set in fxml
                 * fxmlLoader.setController(NewWindowController);
                 */
                Parent rootContainer = fxmlLoader.load();

                Scene scene = new Scene(rootContainer, 749, 528);
                Stage stage = new Stage();
                stage.setMinHeight(528);
                stage.setMinWidth(749);
                stage.setMaxHeight(528);
                stage.setMaxWidth(749);
                stage.setTitle("Info");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                //Logger logger = Logger.getLogger(getClass().getName());
                //logger.log(Level.SEVERE, "Failed to create new Window.", e);
            }
        });
    }

    @Override
    public void onLoggedIn() {
//        this.isLoggedIn.set(true);
//        gridPaneMainAppRight.getChildren().remove(0); //move to property
//        gridPaneMainAppRight.getChildren().add(this.dashboardController.getNodeController());
        sharedOnLoggedIn(gridPaneMainAppRight,isLoggedIn,this.dashboardController.getNodeController());
        this.dashboardController.initializeDashboardController(SharedLogin.userNamePropertyProperty(), this.selectedGraph, this.selectedTask, this.matchingUserName);
        startTaskControlPanelRefresher();
        SharedChat.startChatRefresher(this.dashboardController.getChatTextArea(), this.taskControlPanelController.getChatTextArea());
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

    public List<String> getTasksNames() {
        List<String> taskNames = new LinkedList<>();
        taskNames.add("Simulation");
        taskNames.add("Compilation");
        return taskNames;
    }
}

