package components.dashboard;

import DTO.GraphDTO;
import DTO.TaskDTO;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.util.*;

public class DashboardController implements Controller {

    //Controllers
    private MainAppController mainAppController;
    private Node nodeController;
    private Timer userTimer;
    private Timer graphTimer;
    private Timer taskTimer;
    private TimerTask userRefresher;
    private TimerTask graphRefresher;
    private TimerTask taskRefresher;
    private List<GraphDTO> allGraphsDTOS;
    private List<TaskDTO> allTasksDTOS;
    //
    //UI
    @FXML private TableView<String> tasksTableView;
    @FXML private TableColumn<String, String> taskTableColumn;
    @FXML private ListView<String> tasksListView;
    @FXML private TableView<String> graphsTableView;
    @FXML private ListView<String> graphsListView;
    @FXML private TableColumn<String, String> graphTableColumn;
    @FXML private TableView<UserTableViewRow> usersTableView;
    @FXML private TableColumn<UserTableViewRow, String> userTableColumn;
    @FXML private TableColumn<UserTableViewRow, String> typeTableColumn;
    @FXML private Label loggedInLabel;
   //
    //Properties
    private SimpleStringProperty selectedGraph;
    private SimpleStringProperty selectedTask;
    private SimpleBooleanProperty matchingUserName;


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

    @FXML
    private void initialize(){
        allGraphsDTOS = new LinkedList<>();
        allTasksDTOS = new LinkedList<>();
        userTimer = new Timer();
        graphTimer = new Timer();
        taskTimer = new Timer();
        startDashboardRefresher();
        wireColumnForUserList(userTableColumn,"userName");
        wireColumnForUserList(typeTableColumn,"userType");
        graphTableColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue()));
        taskTableColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue()));
    }

    private void startDashboardRefresher(){
        userRefresher = new UserListRefresher(this::updateUsersList);
        graphRefresher = new GraphListRefresher(this.mainAppController.getServerOnProperty(),this::updateGraphList);
        taskRefresher = new TaskListRefresher(this::updateTaskList);

        userTimer.schedule(userRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
        graphTimer.schedule(graphRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
        taskTimer.schedule(taskRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
    }

    private void updateUsersList(Map<String,String> usersNames) {
        List<UserTableViewRow> rows = new LinkedList<>();

        for (Map.Entry<String, String> entry : usersNames.entrySet()) {
            rows.add(new UserTableViewRow(entry.getKey(), entry.getValue()));
        }

        Platform.runLater(() -> {
            final ObservableList<UserTableViewRow> data = FXCollections.observableArrayList(rows);
            usersTableView.setItems(data);
            usersTableView.getColumns().clear();
            usersTableView.getColumns().addAll(userTableColumn,typeTableColumn);
        });
    }

    private void updateGraphList(List<GraphDTO> graphDTOS) {
        List<String> rows = new LinkedList<>();
        this.allGraphsDTOS = graphDTOS;

        graphDTOS.forEach(graph -> rows.add(graph.getGraphName()));

        Platform.runLater(() -> {
            final ObservableList<String> data = FXCollections.observableArrayList(rows);
            graphsTableView.setItems(data);
            graphsTableView.getColumns().clear();
            graphsTableView.getColumns().addAll(graphTableColumn);
        });
    }

    private void updateTaskList(List<TaskDTO> taskDTOS) {
        List<String> rows = new LinkedList<>();
        this.allTasksDTOS = taskDTOS;

        taskDTOS.forEach(task -> rows.add(task.getTaskName()));

        Platform.runLater(() -> {
            final ObservableList<String> data = FXCollections.observableArrayList(rows);
            tasksTableView.setItems(data);
            tasksTableView.getColumns().clear();
            tasksTableView.getColumns().addAll(taskTableColumn);
        });
    }

    private void wireColumnForUserList(TableColumn column, String property) {
        column.setCellValueFactory(
            new PropertyValueFactory<>(property));
    }

    @FXML
    void graphsTableViewOnClicked(MouseEvent event) {
        //Get the pressed graph name from the tables
        String temp = event.getPickResult().toString();
        System.out.println(temp);
        if(!temp.contains("TableColumn") && !temp.contains("Text"))
            return;
        int start = temp.indexOf("'");
        int last = temp.lastIndexOf("'");
        if(start == -1){
            start = temp.indexOf("\"");
            last = temp.lastIndexOf("\"");
        }
        try {
            String name = temp.substring(++start, last);
            if(name.compareTo("null") != 0) {
                this.selectedGraph.set(name);
                loadGraphInfoToListView();
                this.mainAppController.loadAllDetailsToSubComponents();
            }
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println("eeeeror");
        };
    }

    private void loadGraphInfoToListView() {
        GraphDTO graphToShow = getSelectedGraph();
        this.graphsListView.getItems().clear();
        this.graphsListView.getItems().add("Graph Name: " + graphToShow.getGraphName());
        this.graphsListView.getItems().add("Graph Upload By: " + graphToShow.getUploaderName());
        this.graphsListView.getItems().add("Total Targets Amount: " + graphToShow.getAllTargets().size());
        this.graphsListView.getItems().add("Total Root Targets Amount: " + graphToShow.getRootAmount());
        this.graphsListView.getItems().add("Total Leaf Targets Amount: " + graphToShow.getLeafAmount());
        this.graphsListView.getItems().add("Total Middle Targets Amount: " + graphToShow.getMiddleAmount());
        this.graphsListView.getItems().add("Total Independent Targets Amount: " + graphToShow.getIndependentAmount());
    }

    @FXML
    void tasksTableViewOnClicked(MouseEvent event) {
//Get the pressed graph name from the tables
        String temp = event.getPickResult().toString();
        System.out.println(temp);
        if(!temp.contains("TableColumn") && !temp.contains("Text"))
            return;
        int start = temp.indexOf("'");
        int last = temp.lastIndexOf("'");
        if(start == -1){
            start = temp.indexOf("\"");
            last = temp.lastIndexOf("\"");
        }
        try {
            String name = temp.substring(++start, last);
            if(name.compareTo("null") != 0) {
                this.selectedTask.set(name);
                loadTaskInfoToListView();
            }
            if (getLoggedInUserName().compareTo(getSelectedTask().getUploaderName()) == 0)
                this.matchingUserName.set(true);
            else
                this.matchingUserName.set(false);
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println("eeeeror");
        };
    }

    private void loadTaskInfoToListView() {
        TaskDTO taskToShow = getSelectedTask();
        this.tasksListView.getItems().clear();
        this.tasksListView.getItems().add("Task Name: " + taskToShow.getTaskName());
        this.tasksListView.getItems().add("Task Upload By: " + taskToShow.getUploaderName());
        this.tasksListView.getItems().add("Total Targets Amount: " + taskToShow.getAmountOfTargets());
        this.tasksListView.getItems().add("Total Root Targets Amount: " + taskToShow.getRootAmount());
        this.tasksListView.getItems().add("Total Leaf Targets Amount: " + taskToShow.getLeafAmount());
        this.tasksListView.getItems().add("Total Middle Targets Amount: " + taskToShow.getMiddleAmount());
        this.tasksListView.getItems().add("Total Independent Targets Amount: " + taskToShow.getIndependentAmount());
        //this.tasksListView.getItems().add("Total Price For Task: " + taskToShow.getIndependentAmount()); //todo add
        //this.tasksListView.getItems().add("Current Number Of Workers: " + taskToShow.getIndependentAmount());
    }

    public void initializeDashboardController(SimpleStringProperty userNameProperty, SimpleStringProperty selectedGraph,
                                              SimpleStringProperty selectedTask, SimpleBooleanProperty matchingUserName, BooleanProperty isServerOn) {
        loggedInLabel.textProperty().bind(userNameProperty);
        this.selectedGraph = selectedGraph;
        this.selectedTask = selectedTask;
        this.matchingUserName = matchingUserName;
    }

    public String getLoggedInUserName(){
        String str = loggedInLabel.getText();
        String tmp = str.substring(str.indexOf("[") +1, str.indexOf("]"));
        return tmp;
    }

    public TaskDTO getSelectedTask() {
        TaskDTO toReturn = null;
        if(selectedTask.get() != null) {
            for (TaskDTO task : allTasksDTOS) {
                if (task.getTaskName().compareTo(this.selectedTask.get()) == 0)
                    toReturn = task;
            }
        }
        return toReturn;
    }

    public GraphDTO getSelectedGraph(){
        GraphDTO toReturn = null;
        for (GraphDTO graph : allGraphsDTOS) {
            if (graph.getGraphName().compareTo(this.selectedGraph.get()) == 0)
                toReturn = graph;
        }
        return toReturn;
    }
}
