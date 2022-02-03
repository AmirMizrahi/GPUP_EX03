package components.mainApp;

import DTO.GraphDTO;
import DTO.TargetDTO;
import DTO.TaskDTO;
import DTO.TaskDTOForWorker;
import Utils.Constants;
import Utils.HttpClientUtil;
import com.google.gson.reflect.TypeToken;
import components.dashboard.DashboardControllerW;
import components.login.LoginControllerW;
import components.subscribedTasksPanel.SubscribedTasksPanelController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import managers.WorkerManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import sharedControllers.sharedMainAppController;
import sharedDashboard.SharedDashboard;
import sharedLogin.SharedLogin;
import sharedMainApp.SharedMainApp;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;

import static Utils.Constants.GSON_INSTANCE;
import static Utils.Constants.TARGET_REFRESH_RATE;
import static sharedMainApp.SharedMainApp.sharedOnLoggedIn;

public class MainAppControllerW implements sharedMainAppController {

    //
    private WorkerManager workerManager;
    private Stage primaryStage;
    private Timer timer;
    //Properties
    private BooleanProperty isLoggedIn;
    private SimpleStringProperty selectedTask;
    // Controllers
    private LoginControllerW loginControllerW;
    private DashboardControllerW dashboardControllerW;
    private SubscribedTasksPanelController subscribedTasksPanelController;
    //UI
    @FXML private Label serverStatusLabel;
    @FXML private Button dashboardButton;
    @FXML private Button subscribedTasksPanelButton;
    @FXML private ComboBox<?> changeSkinComboBox;
    @FXML private GridPane gridPaneMainAppRight;
    @FXML void changeSkinComboBoxAction(ActionEvent event) {}


    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @FXML
    private void initialize() throws IOException {
        workerManager = new WorkerManager(/*this.loginControllerW.getThreadsAmount()*/);
        selectedTask = new SimpleStringProperty();
        isLoggedIn = new SimpleBooleanProperty(false);
        this.dashboardButton.disableProperty().bind(isLoggedIn.not().or(SharedMainApp.getServerOnProperty().not()));
        this.subscribedTasksPanelButton.disableProperty().bind(isLoggedIn.not().or(SharedMainApp.getServerOnProperty().not()));

        loginControllerW = (LoginControllerW) genericControllersInit("/components/login/login.fxml");
        dashboardControllerW = (DashboardControllerW) genericControllersInit("/components/dashboard/dashboard.fxml");
        dashboardControllerW = (DashboardControllerW) genericControllersInit("/components/dashboard/dashboard.fxml");
        subscribedTasksPanelController = (SubscribedTasksPanelController) genericControllersInit("/components/subscribedTasksPanel/subscribedTasksPanel.fxml");

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
        this.dashboardControllerW.initializeDashboardController(SharedLogin.userNamePropertyProperty(), this.selectedTask);
        startTaskControlPanelRefresher();
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
                            .addQueryParameter("availableThreads", workerManager.getAvailableThreadsAmount().toString())
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
                            String jsonArrayOfUsersNames = response.body().string();
                            //httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Response: " + jsonArrayOfUsersNames);
                            Type type = new TypeToken<List<TaskDTOForWorker>>(){}.getType();
                            List<TaskDTOForWorker> taskDTOForWorkers = GSON_INSTANCE.fromJson(jsonArrayOfUsersNames, type);
                            response.close();
                            //List<String> temp = new LinkedList<>();
                            //taskDTOForWorkers.forEach(taskDTOForWorker -> temp.add(taskDTOForWorker.getTargetDTO().getTargetName()));
                            workerManager.setThreadsOnWork(taskDTOForWorkers.size());
                            taskDTOForWorkers.forEach(dto-> workerManager.addTargetToThreadPool(dto));
                            //System.out.println("RESULT: " + temp);


                            //graphsListConsumer.accept(taskDTOForWorkers);*///todo
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

    public TaskDTO getSelectedTaskDTOFromDashboard() {
        return SharedDashboard.getSelectedTask(this.selectedTask);
    }

    public void addSubscriber(TaskDTO newSubscribedTask) {
        this.workerManager.addSubscriber(newSubscribedTask);
    }
}


