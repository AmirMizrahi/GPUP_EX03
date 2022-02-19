package components.mainApp;

import DTO.GraphDTO;
import DTO.TaskDTO;
import DTO.TargetDTOForWorker;
import DTO.WorkerTargetDTO;
import Utils.Constants;
import Utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import components.dashboard.DashboardControllerW;
import components.login.LoginControllerW;
import components.subscribedTasksPanel.SubscribedTasksPanelController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import managers.WorkerManager;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sharedChat.SharedChat;
import sharedControllers.sharedMainAppController;
import sharedDashboard.SharedDashboard;
import sharedLogin.SharedLogin;
import sharedMainApp.SharedMainApp;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;

import static Utils.Constants.*;
import static sharedMainApp.SharedMainApp.sharedOnLoggedIn;

public class MainAppControllerW implements sharedMainAppController {

    //
    private WorkerManager workerManager;
    private Stage primaryStage;
    private Timer timer;
    private Timer postTimer;
    //Properties
    private BooleanProperty isLoggedIn;
    private SimpleStringProperty selectedTask;
    private SimpleIntegerProperty totalMoneyEarned;
    // Controllers
    private LoginControllerW loginControllerW;
    private DashboardControllerW dashboardControllerW;
    private SubscribedTasksPanelController subscribedTasksPanelController;
    //UI
    @FXML private Label serverStatusLabel;
    @FXML private Button dashboardButton;
    @FXML private Button subscribedTasksPanelButton;
    @FXML private Button howToUseButton;
    @FXML private ComboBox<String> changeSkinComboBox;
    @FXML private GridPane gridPaneMainAppRight;

    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @FXML
    private void initialize() throws IOException {
        selectedTask = new SimpleStringProperty();
        isLoggedIn = new SimpleBooleanProperty(false);
        totalMoneyEarned = new SimpleIntegerProperty(0);
        this.dashboardButton.disableProperty().bind(isLoggedIn.not().or(SharedMainApp.getServerOnProperty().not()));
        this.subscribedTasksPanelButton.disableProperty().bind(isLoggedIn.not().or(SharedMainApp.getServerOnProperty().not()));

        loginControllerW = (LoginControllerW) genericControllersInit("/components/login/login.fxml");
        dashboardControllerW = (DashboardControllerW) genericControllersInit("/components/dashboard/dashboard.fxml");
        dashboardControllerW = (DashboardControllerW) genericControllersInit("/components/dashboard/dashboard.fxml");
        subscribedTasksPanelController = (SubscribedTasksPanelController) genericControllersInit("/components/subscribedTasksPanel/subscribedTasksPanel.fxml");

        this.changeSkinComboBox.getItems().addAll("No Skin", "Light Mode","Dark Mode", "Old School Mode");
        this.changeSkinComboBox.getSelectionModel().select(0);

        this.serverStatusLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            String str;
            if (SharedMainApp.getServerOnProperty().get())
                str = "Server is ON";
            else {
                str = "Server is OFF";
                this.gridPaneMainAppRight.getChildren().remove(0);
                gridPaneMainAppRight.getChildren().add(this.loginControllerW.getNodeController());
                this.isLoggedIn.set(false);
            }
            return str;
        }, SharedMainApp.getServerOnProperty()));

        this.gridPaneMainAppRight.getChildren().remove(0);
        gridPaneMainAppRight.getChildren().add(this.loginControllerW.getNodeController());
    }

    private ControllerW genericControllersInit(String str) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(str));
        Node node = loader.load();
        ControllerW ctr = loader.getController();
        ctr.setNodeController(node);
        ctr.setMainAppControllerW(this);

        return ctr;
    }

    @FXML void dashboardButtonAction(ActionEvent event) {
        this.gridPaneMainAppRight.getChildren().remove(0);
        gridPaneMainAppRight.getChildren().add(this.dashboardControllerW.getNodeController());
    }

    @FXML
    void subscribedTasksPanelButtonAction(ActionEvent event) {
        this.gridPaneMainAppRight.getChildren().remove(0);
        gridPaneMainAppRight.getChildren().add(this.subscribedTasksPanelController.getNodeController());
    }

    @Override
    public void onLoggedIn() {
        sharedOnLoggedIn(gridPaneMainAppRight,isLoggedIn,this.dashboardControllerW.getNodeController());
        workerManager = new WorkerManager(this.loginControllerW.getThreadsAmount());
        this.dashboardControllerW.initializeDashboardController(SharedLogin.userNamePropertyProperty(), this.selectedTask, this.loginControllerW.getThreadsAmount(), totalMoneyEarned);
        this.subscribedTasksPanelController.initializeSubscribedTasksPanelController(totalMoneyEarned);
        startTaskControlPanelRefresher();
        updateServerWithTargetsResults();
        SharedChat.startChatRefresher(this.dashboardControllerW.getChatTextArea(), this.subscribedTasksPanelController.getChatTextArea());
    }

    private void startTaskControlPanelRefresher() {
        Consumer<Boolean> refresherConsumer = new Consumer() {
            @Override
            public void accept(Object o) {
                List<TaskDTO> taskDTOS = SharedDashboard.getAllTasksDTOS();
                workerManager.updateSubscribedTask(taskDTOS);
                if(workerManager.isThereWorkToDo()){
                    String finalUrl = HttpUrl
                            .parse(Constants.GET_TARGETS)
                            .newBuilder()
                            .addQueryParameter("username", SharedDashboard.getLoggedInUserName(dashboardControllerW.getUserNameLabel()))
                            .addQueryParameter("numberOfTargets", workerManager.getAvailableThreadsAmount().toString())
                            .build()
                            .toString();

                    HttpClientUtil.runAsync(finalUrl, new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            List<GraphDTO> failed = new LinkedList<>();
                            //graphsListConsumer.accept(failed);//todo
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            synchronized (this) {
                                String jsonArrayOfUsersNames = response.body().string();
                                //httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Response: " + jsonArrayOfUsersNames);
                                Type type = new TypeToken<List<TargetDTOForWorker>>() {
                                }.getType();
                                List<TargetDTOForWorker> taskDTOForWorkers = GSON_INSTANCE.fromJson(jsonArrayOfUsersNames, type);
                                response.close();
                                //List<String> temp = new LinkedList<>();
                                //taskDTOForWorkers.forEach(taskDTOForWorker -> temp.add(taskDTOForWorker.getTargetDTO().getTargetName()));
                                //System.out.println("--------------------------------------------Got from server: " + taskDTOForWorkers.size());
                                workerManager.setThreadsOnWork(taskDTOForWorkers.size());
                                taskDTOForWorkers.forEach(dto -> {
                                    try {
                                        workerManager.addTargetToThreadPool(dto);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                });
                                //System.out.println("RESULT: " + temp);


                                //graphsListConsumer.accept(taskDTOForWorkers);*///todo
                            }
                        }
                    });
                }
                //taskControlPanelController.refreshPanel(getSelectedTaskDTOFromDashboard());
            }
        };

        TargetRequestRefresher targetRequestRefresher = new TargetRequestRefresher(refresherConsumer);
        timer = new Timer();
        timer.schedule(targetRequestRefresher, TARGET_REFRESH_RATE, TARGET_REFRESH_RATE);
    }

    private void updateServerWithTargetsResults() {
        Consumer<Boolean> refresherConsumer = new Consumer() {
            @Override
            public void accept(Object o) {
                if(workerManager.areThereTargetsToSend()){
                    String body =
                            "results=" + new Gson().toJson(workerManager.getUpdatedTargetsResults());

                    HttpClientUtil.postRequest(RequestBody.create(body.getBytes()), new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            List<GraphDTO> failed = new LinkedList<>();
                            //graphsListConsumer.accept(failed);//todo
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                            String jsonArrayOfUsersNames = response.body().string();
//                            //httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Response: " + jsonArrayOfUsersNames);
//                            Type type = new TypeToken<List<TaskDTOForWorker>>(){}.getType();
//                            List<TaskDTOForWorker> taskDTOForWorkers = GSON_INSTANCE.fromJson(jsonArrayOfUsersNames, type);
//                            System.out.println("----------------------------------" + taskDTOForWorkers.size() + "-------------------------");
                            response.close();
//                            //List<String> temp = new LinkedList<>();
//                            //taskDTOForWorkers.forEach(taskDTOForWorker -> temp.add(taskDTOForWorker.getTargetDTO().getTargetName()));
//                            workerManager.setThreadsOnWork(taskDTOForWorkers.size());
//                            taskDTOForWorkers.forEach(dto-> {
//                                try {
//                                    workerManager.addTargetToThreadPool(dto);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                            //System.out.println("RESULT: " + temp);


                            //graphsListConsumer.accept(taskDTOForWorkers);*///todo
                        }
                    }, UPDATE_RESULTS);
                }
                //taskControlPanelController.refreshPanel(getSelectedTaskDTOFromDashboard());
            }
        };

        TargetRequestRefresher targetRequestRefresher = new TargetRequestRefresher(refresherConsumer);
        postTimer = new Timer();
        postTimer.schedule(targetRequestRefresher, TARGET_REFRESH_RATE, TARGET_REFRESH_RATE);
    }

    public TaskDTO getSelectedTaskDTOFromDashboard() {
        return SharedDashboard.getSelectedTask(this.selectedTask);
    }

    public void addSubscriber(TaskDTO newSubscribedTask) {
        this.workerManager.addSubscriber(newSubscribedTask);
    }

    @FXML void changeSkinComboBoxAction(ActionEvent event) {
        SharedMainApp.changeSkin(changeSkinComboBox,getClass(),this.primaryStage.getScene());
    }

    public List<WorkerTargetDTO> getAllWorkerTargets() {
        return this.workerManager.getAllTargets();
    }

    public Integer getCurrentThreadsWorking() {
        return this.workerManager.getThreadsOnWork();
    }

    public Integer getMaxThreadsAmount() {
        return this.workerManager.getThreadsAmount();
    }

    public String getLoggedInUserName() {return SharedDashboard.getLoggedInUserName(dashboardControllerW.getUserNameLabel());}

    @FXML
    void howToUseButtonAction(ActionEvent event) {
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
}


