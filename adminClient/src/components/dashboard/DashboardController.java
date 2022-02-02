package components.dashboard;

import DTO.GraphDTO;
import DTO.TaskDTO;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import sharedDashboard.SharedDashboard;
import sharedDashboard.UserTableViewRow;

import java.util.*;

public class DashboardController implements Controller {

    //Controllers
    private MainAppController mainAppController;
    private Node nodeController;


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
        SharedDashboard.startDashboardRefresher(
                usersTableView, userTableColumn, typeTableColumn,
                graphsTableView, graphTableColumn,
                tasksTableView, taskTableColumn);

        SharedDashboard.wireColumnForUserList(userTableColumn,"userName");
        SharedDashboard.wireColumnForUserList(typeTableColumn,"userType");
        graphTableColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue()));
        taskTableColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue()));
    }

    @FXML
    void graphsTableViewOnClicked(MouseEvent event) {
        //Get the pressed graph name from the tables
        String temp = event.getPickResult().toString();
        System.out.println(temp);
        if(!temp.contains("TableColumn") && !temp.contains("Text") || temp.contains("TableColumnHeader") || temp.contains("name=System Bold") || temp.contains("text=\"No content in table\""))
            return;
        int start = temp.indexOf("'");
        int last = temp.lastIndexOf("'");
        if(start == -1){
            start = temp.indexOf("\"");
            last = temp.lastIndexOf("\"");
        }

        String name = temp.substring(++start, last);
        if(name.compareTo("null") != 0) {
            this.selectedGraph.set(name);
            loadGraphInfoToListView();
            this.mainAppController.loadAllDetailsToSubComponents();
        }
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
        if(graphToShow.getSimulationPrice() != -1)
            this.graphsListView.getItems().add("Simulation Price: " + graphToShow.getSimulationPrice());
        if(graphToShow.getCompilationPrice() != -1)
            this.graphsListView.getItems().add("Compilation Price: " + graphToShow.getCompilationPrice());
    }

    @FXML
    void tasksTableViewOnClicked(MouseEvent event) {
//Get the pressed graph name from the tables
        String temp = event.getPickResult().toString();
        SharedDashboard.doWhenClickedOnTaskTable(temp, this.selectedTask, this.tasksListView );

        if(selectedTask.get() == null)
            return;
        if (SharedDashboard.getLoggedInUserName(loggedInLabel).compareTo(SharedDashboard.getSelectedTask(this.selectedTask).getUploaderName()) == 0)
            this.matchingUserName.set(true);
        else
            this.matchingUserName.set(false);
    }

    public void initializeDashboardController(SimpleStringProperty userNameProperty, SimpleStringProperty selectedGraph,
                                              SimpleStringProperty selectedTask, SimpleBooleanProperty matchingUserName) {
        loggedInLabel.textProperty().bind(userNameProperty);
        this.selectedGraph = selectedGraph;
        this.selectedTask = selectedTask;
        this.matchingUserName = matchingUserName;
        this.graphsListView.getItems().clear();
        this.tasksListView.getItems().clear();
        this.matchingUserName.set(false);
        this.selectedGraph.set("");
    }

    public String getLoggedInUserName(){
        return SharedDashboard.getLoggedInUserName(loggedInLabel);
    }

    public GraphDTO getSelectedGraph(){
        GraphDTO toReturn = null;
        List<GraphDTO> allGraphsDTOS = SharedDashboard.getAllGraphsDTOS();
        for (GraphDTO graph : allGraphsDTOS) {
            if (graph.getGraphName().compareTo(this.selectedGraph.get()) == 0)
                toReturn = graph;
        }
        return toReturn;
    }
}
