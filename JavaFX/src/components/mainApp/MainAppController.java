package components.mainApp;

import DTO.GraphDTO;
import DTO.PathsBetweenTwoTargetsDTO;
import DTO.SerialSetsDTO;
import DTO.TargetDTO;

import components.activateTask.mainActivateTask.MainActivateTaskController;
import components.basicInformation.BasicInformationController;
import components.basicInformation.TargetsTableViewRow;
import components.cycle.CycleController;
import components.findPath.FindPathController;
import components.gifAnimation.gifAnimationController;
import components.graphviz.GraphvizController;
import components.login.LoginController;
import components.welcomeAnimation.welcomeAnimationController;
import components.welcomeToGPUP.welcomeToGPUPController;
import components.whatIf.WhatIfController;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import managers.Manager;
import tasks.AbstractTask;
import tasks.SimulationTask;

import javax.xml.bind.JAXBException;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;

import static Utils.Constants.REFRESH_RATE;

public class MainAppController implements Closeable {

    //
    private final Manager manager;
    private Stage primaryStage;
    private int maxParallelTaskAmount;
    private Timer timer;
    //
    //UI
    @FXML private GridPane gridPaneMainAppRight;
    @FXML private Button loadXMLButton;
    @FXML private Button showGraphInformationButton;
    @FXML private Button findPathBetweenTwoTargetsButton;
    @FXML private Button checkIfTargetInCycleButton;
    @FXML private Button activateTaskButton;
    @FXML private Button whatIfButton;
    @FXML private Button animationButton;
    @FXML private Button graphvizButton;
    @FXML private ComboBox<String> changeSkinComboBox;
    //Properties
    private SimpleBooleanProperty isFileSelected;
    // Controllers
    @FXML private LoginController loginController;
    @FXML private welcomeToGPUPController welcomeToGpupController;
    @FXML private welcomeAnimationController welcomeAnimationController;
    @FXML private BasicInformationController basicInformationController;
    @FXML private FindPathController findPathController;
    @FXML private CycleController cycleController;
    @FXML private WhatIfController whatIfController;
    @FXML private MainActivateTaskController mainActivateTaskController;
    @FXML private gifAnimationController gifAnimationController;
    @FXML private GraphvizController graphvizController;
    //
    //Consts

    //

    public MainAppController(){
        this.manager = new Manager();
        isFileSelected = new SimpleBooleanProperty(false);
    }

    @FXML
    private void initialize() throws IOException {
        this.showGraphInformationButton.disableProperty().bind(isFileSelected.not());
        this.findPathBetweenTwoTargetsButton.disableProperty().bind(isFileSelected.not());
        this.checkIfTargetInCycleButton.disableProperty().bind(isFileSelected.not());
        this.activateTaskButton.disableProperty().bind(isFileSelected.not());
        this.whatIfButton.disableProperty().bind(isFileSelected.not());
        this.animationButton.disableProperty().bind(isFileSelected.not());
        this.graphvizButton.disableProperty().bind(isFileSelected.not());

        loginController = (LoginController) genericControllersInit("/components/login/login.fxml");
        welcomeToGpupController = (welcomeToGPUPController) genericControllersInit("/components/welcomeToGPUP/welcomeToGPUP.fxml");
        welcomeAnimationController = (welcomeAnimationController) genericControllersInit("/components/welcomeAnimation/welcomeAnimation.fxml");
        basicInformationController = (BasicInformationController) genericControllersInit("/components/basicInformation/basicInformation.fxml");
        findPathController = (FindPathController) genericControllersInit("/components/findPath/findPath.fxml");
        cycleController = (CycleController) genericControllersInit("/components/cycle/cycle.fxml");
        whatIfController = (WhatIfController) genericControllersInit("/components/whatIf/whatIf.fxml");
        mainActivateTaskController = (MainActivateTaskController) genericControllersInit("/components/activateTask/mainActivateTask/mainActivateTask.fxml");
        gifAnimationController = (gifAnimationController) genericControllersInit("/components/gifAnimation/gifAnimation.fxml");
        graphvizController = (GraphvizController) genericControllersInit("/components/graphviz/graphviz.fxml");

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

        // Default folder for all examples.
//        String currentPath = Paths.get("engine/src/resources/XMLTestsEx02").toAbsolutePath().normalize().toString();
//        fileChooser.setInitialDirectory(new File(currentPath));
        //

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML File", "*.xml"));
        File file = fileChooser.showOpenDialog(this.primaryStage);

        if (file == null)
            return;

        try {
            p.setContentText("XML loaded successfully!");
            manager.loadXMLFileMG(file.getAbsolutePath());
            this.maxParallelTaskAmount = manager.getParallelTaskAmount();
            loadAllDetailsToSubComponents();
            p.setAlertType(Alert.AlertType.INFORMATION);
            isFileSelected.set(true);
            //switchToBasicInformation();
        } catch (TargetNotFoundException | JAXBException | XMLException | IOException e) {
            p.setContentText(e.getMessage());
        }
        finally {
            p.show();
        }
    }

    private void loadAllDetailsToSubComponents() throws XMLException, IOException {
        this.gridPaneMainAppRight.getChildren().remove(0);
        //gridPaneMainAppRight.getChildren().add(this.welcomeToGpupController.getNodeController());
        gridPaneMainAppRight.getChildren().add(this.welcomeAnimationController.getNodeController());
        //this.welcomeAnimationController.initialize(null,null);

        this.basicInformationController.initializeBasicInformationController();
        this.findPathController.initializeFindPath();
        this.cycleController.initializeCycle();
        this.whatIfController.initializeWhatIfController();
        this.mainActivateTaskController.initializeMainActivateTask();
    }

    @FXML
    private void showGraphInformationAction(ActionEvent event) throws XMLException {
        gridPaneMainAppRight.getChildren().remove(0);
        gridPaneMainAppRight.getChildren().add(this.basicInformationController.getNodeController());
    }

    @FXML
    private void findPathBetweenTwoTargetsAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        gridPaneMainAppRight.getChildren().add(this.findPathController.getNodeController());
    }

    @FXML
    private void checkIfTargetInCycleAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        gridPaneMainAppRight.getChildren().add(this.cycleController.getNodeController());
    }

    @FXML
    private void whatIfAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        gridPaneMainAppRight.getChildren().add(this.whatIfController.getNodeController());
    }

    @FXML
    private void activateTaskAction(ActionEvent event) {
        gridPaneMainAppRight.getChildren().remove(0); //move to property
        //this.mainActivateTaskController.setParallelTaskAmount(parallelTaskAmount);
        gridPaneMainAppRight.getChildren().add(this.mainActivateTaskController.getNodeController());
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

    public GraphDTO getGraphDTOFromEngine() throws XMLException {
        return this.manager.showBasicInformationAboutEntireGraphMG();
    }

    public SerialSetsDTO getSerialSetDTOFromEngine() throws XMLException {
        return this.manager.showBasicInformationAboutSerialSetsMG();
    }

    public List<String> getTargetsNames() throws XMLException {
        List<String> targetsNames = new LinkedList<>();
        this.manager.showBasicInformationAboutEntireGraphMG().getAllTargets().forEach(targetDTO -> targetsNames.add(targetDTO.getTargetName()));
        return targetsNames;
    }

    public List<String> getAllPathsBetweenTwoTargets(String source, String destination, RadioButton selectedRadioButton) throws XMLException, TargetNotFoundException {
        Manager.DependencyRelation relation;
        if (selectedRadioButton.getText().equals("Depends On"))
            relation = Manager.DependencyRelation.DEPENDS_ON;
        else
            relation = Manager.DependencyRelation.REQUIRED_FOR;

        PathsBetweenTwoTargetsDTO dto = manager.findALlPathsBetweenTwoTargetsMG(source, destination, relation);

        return dto.getPathsBetweenTwoTargets();
    }

    public List<String> getCyclePath(String value) throws XMLException, TargetNotFoundException {
        return this.manager.checkIfTargetIsPartOfCycleMG(value).getPathsBetweenTwoTargets();
    }

    public List<String> getAllEffectedTargets(String targetName, String selectedRadioButton) throws TargetNotFoundException {
        Manager.DependencyRelation relation;
        List<String> returnedList =  new LinkedList<>();

        if (selectedRadioButton.contains("Depends On"))
            relation = Manager.DependencyRelation.DEPENDS_ON;
        else
            relation = Manager.DependencyRelation.REQUIRED_FOR;

        List<TargetDTO> dtos = this.manager.getAllEffectedTargets(targetName,relation);
        dtos.forEach(targetDTO ->returnedList.add(targetDTO.getTargetName()));
        return returnedList;
    }

    public List<String> getAllEffectedTargetsAdapter(String targetName, String textInRadioButton) throws TargetNotFoundException {
        return getAllEffectedTargets(targetName, textInRadioButton);
    }

    public int getMaxParallelTaskAmount() {
        return this.maxParallelTaskAmount;
    }

    public List<String> getTasksNames() {
        return this.manager.getTasksNames();
    }

    public TableView<TargetsTableViewRow> getTargetsTableView(){
        return this.basicInformationController.getTargetTable();
    }

    public void activateSimTask(List<String> selectedTargets, Integer time, SimulationTask.TIME_OPTION time_option, Double successRates,
                                Double warningRates, AbstractTask.WAYS_TO_START_SIM_TASK way, int tasksNumber, Consumer<File> consumeWhenFinished) {
        List<Consumer<String>> consumerList = new LinkedList<>();
        Consumer<String> consumer1 = System.out::println;
        consumerList.add(consumer1);
        try {
            this.startTargetRefresher();
            this.manager.activateSimulationTask(selectedTargets, time,time_option, successRates,warningRates, way,consumerList, consumeWhenFinished, tasksNumber);
        } catch (XMLException | IOException | InterruptedException | TargetNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void activateCompTask(List<String> selectedTargets, String source, String destination, AbstractTask.WAYS_TO_START_SIM_TASK way,
                                 int tasksNumber, Consumer<File> consumeWhenFinished) {
        List<Consumer<String>> consumerList = new LinkedList<>();
        Consumer<String> consumer1 = System.out::println;
        consumerList.add(consumer1);
        try {
            this.startTargetRefresher();
            this.manager.activateCompilationTask(selectedTargets, source, destination, way,consumerList,consumeWhenFinished, tasksNumber);
        } catch (XMLException | IOException | InterruptedException | TargetNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startTargetRefresher(){
        //Consumer =>
                            /// List<TargetDTO> a = getFromEngine
                            /// UpdateAllTables(a)

        Consumer<Boolean> refresherConsumer = new Consumer() {
            @Override
            public void accept(Object o) {
                List<TargetDTO> newTargetStatus = manager.getCurrentTargetsStatus();
                mainActivateTaskController.updateProcessLog(newTargetStatus);
            }
        };

        TargetRefresher targetRefresher = new TargetRefresher(refresherConsumer);
        timer = new Timer();
        timer.schedule(targetRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    public TargetDTO getInformationOnTarget(String targetName) throws XMLException, TargetNotFoundException {
        return this.manager.showInformationAboutSpecificTargetMG(targetName);
    }

    @Override
    public void close() {
        if(timer!= null){
            timer.cancel();
        }
    }

    public ObservableList<CheckBox> getCheckBoxesFromMainAppController() {
        return this.basicInformationController.getCheckBoxes();
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
}