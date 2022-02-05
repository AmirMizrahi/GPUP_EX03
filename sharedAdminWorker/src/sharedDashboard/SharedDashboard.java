package sharedDashboard;

import DTO.GraphDTO;
import DTO.TaskDTO;
import DTO.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.*;

import static Utils.Constants.DASHBOARD_REFRESH_RATE;
import static sharedMainApp.SharedMainApp.getServerOnProperty;

public class SharedDashboard {
    private static Timer userTimer;
    private static Timer graphTimer;
    private static Timer taskTimer;
    private static TimerTask userRefresher;
    private static TimerTask graphRefresher;
    private static TimerTask taskRefresher;
    private static TableView usersTableView;
    private static TableColumn userTableColumn;
    private static TableColumn typeTableColumn;
    private static TableView graphsTableView;
    private static TableColumn graphTableColumn;
    private static TableView tasksTableView;
    private static TableColumn taskTableColumn;
    private static List<GraphDTO> allGraphsDTOS = new LinkedList<>();
    private static List<TaskDTO> allTasksDTOS = new LinkedList<>();

    public static void startDashboardRefresher(TableView usersTableView, TableColumn userTableColumn, TableColumn typeTableColumn,
                                               TableView graphsTableView, TableColumn graphTableColumn,
                                               TableView tasksTableView, TableColumn taskTableColumn){
        startDashboardRefresher(usersTableView, userTableColumn, typeTableColumn,
                tasksTableView, taskTableColumn);
        SharedDashboard.graphsTableView = graphsTableView;
        SharedDashboard.graphTableColumn = graphTableColumn;
        graphTimer = new Timer();
        graphRefresher = new GraphListRefresher(SharedDashboard::updateGraphList);

        graphTimer.schedule(graphRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
    }

    public static void startDashboardRefresher(TableView usersTableView, TableColumn userTableColumn, TableColumn typeTableColumn,
                                               TableView tasksTableView, TableColumn taskTableColumn){
        SharedDashboard.usersTableView = usersTableView;
        SharedDashboard.userTableColumn = userTableColumn;
        SharedDashboard.typeTableColumn = typeTableColumn;
        SharedDashboard.tasksTableView = tasksTableView;
        SharedDashboard.taskTableColumn = taskTableColumn;
        userTimer = new Timer();
        taskTimer = new Timer();
        userRefresher = new UserListRefresher(getServerOnProperty(),SharedDashboard::updateUsersList);
        taskRefresher = new TaskListRefresher(SharedDashboard::updateTaskList);

        userTimer.schedule(userRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
        taskTimer.schedule(taskRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
    }

    private static void updateUsersList(Map<String, UserDTO> usersNames) {
        List<UserTableViewRow> rows = new LinkedList<>();

        for (Map.Entry<String, UserDTO> entry : usersNames.entrySet()) {
            rows.add(new UserTableViewRow(entry.getKey(), entry.getValue().getType()));
        }

        Platform.runLater(() -> {
            final ObservableList<UserTableViewRow> data = FXCollections.observableArrayList(rows);
            usersTableView.setItems(data);
            usersTableView.getColumns().clear();
            usersTableView.getColumns().addAll(userTableColumn,typeTableColumn);
        });
    }

    private static void updateGraphList(List<GraphDTO> graphDTOS) {
        List<String> rows = new LinkedList<>();
        allGraphsDTOS = graphDTOS;

        graphDTOS.forEach(graph -> rows.add(graph.getGraphName()));

        Platform.runLater(() -> {
            final ObservableList<String> data = FXCollections.observableArrayList(rows);
            graphsTableView.setItems(data);
            graphsTableView.getColumns().clear();
            graphsTableView.getColumns().addAll(graphTableColumn);
        });
    }

    private static void updateTaskList(List<TaskDTO> taskDTOS) {
        List<String> rows = new LinkedList<>();
        allTasksDTOS = taskDTOS;

        taskDTOS.forEach(task -> rows.add(task.getTaskName()));

        Platform.runLater(() -> {
            final ObservableList<String> data = FXCollections.observableArrayList(rows);
            tasksTableView.setItems(data);
            tasksTableView.getColumns().clear();
            tasksTableView.getColumns().addAll(taskTableColumn);
            customiseFactory(taskTableColumn);
            tasksTableView.refresh();
        });
    }

    private static void customiseFactory(TableColumn<String, String> calltypel) {
        calltypel.setCellFactory(column -> {
            return new TableCell<String, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    setText(empty ? "" : getItem().toString());
                    setGraphic(null);

                    TableRow<String> currentRow = getTableRow();
                    try {
                        TaskDTO dto = searchForRefreshedTask(item);
                        String statusAfter = dto.getTaskStatus();

                        if (!isEmpty()) {
                            if(statusAfter.compareToIgnoreCase("STOP") == 0)
                                currentRow.setStyle("-fx-background-color:lightcoral");
                            else if(statusAfter.compareToIgnoreCase("PLAY") == 0)
                                currentRow.setStyle("-fx-background-color:lightblue");
                            else if (statusAfter.compareToIgnoreCase("PAUSE") == 0)
                                currentRow.setStyle("-fx-background-color:#e3e329");
                            else if (statusAfter.compareToIgnoreCase("FINISHED") == 0)
                                currentRow.setStyle("-fx-background-color:lightgreen");
                        }
                    } catch (NullPointerException ignore) {};
                }
            };
        });
    }

    private static TaskDTO searchForRefreshedTask(String taskName){
        TaskDTO dto = null;
        for (TaskDTO taskDTO : allTasksDTOS) {
            if (taskName.compareTo(taskDTO.getTaskName()) == 0){
                dto = taskDTO;
            }
        }
        return dto;
    }

    public static void wireColumnForUserList(TableColumn column, String property) {
        column.setCellValueFactory(
                new PropertyValueFactory<>(property));
    }

    public static List<GraphDTO> getAllGraphsDTOS() {return allGraphsDTOS;}

    public static List<TaskDTO> getAllTasksDTOS() {return allTasksDTOS;}

    public static void doWhenClickedOnTaskTable(String temp, SimpleStringProperty selectedTask, ListView tasksListView) {
        if(!temp.contains("TableColumn") && !temp.contains("Text") || temp.contains("TableColumnHeader") || temp.contains("name=System Bold") || temp.contains("text=\"No content in table\""))
            return;
        System.out.println(temp);//todo from here
        int start = temp.indexOf("'");
        int last = temp.lastIndexOf("'");
        if(start == -1){
            start = temp.indexOf("\"");
            last = temp.lastIndexOf("\"");
        }

        String name = temp.substring(++start, last);
        if(name.compareTo("null") != 0 && name.compareTo("") != 0) {
            selectedTask.set(name);
            loadTaskInfoToListView(tasksListView, selectedTask);
        }
    }

    private static void loadTaskInfoToListView(ListView tasksListView ,SimpleStringProperty selectedTaskProp) {
        TaskDTO taskToShow = getSelectedTask(selectedTaskProp);
        tasksListView.getItems().clear();
        tasksListView.getItems().add("Task Name: " + taskToShow.getTaskName());
        tasksListView.getItems().add("Task Upload By: " + taskToShow.getUploaderName());
        tasksListView.getItems().add("Total Targets Amount: " + taskToShow.getAmountOfTargets());
        tasksListView.getItems().add("Total Root Targets Amount: " + taskToShow.getRootAmount());
        tasksListView.getItems().add("Total Leaf Targets Amount: " + taskToShow.getLeafAmount());
        tasksListView.getItems().add("Total Middle Targets Amount: " + taskToShow.getMiddleAmount());
        tasksListView.getItems().add("Total Independent Targets Amount: " + taskToShow.getIndependentAmount());
        List<String> usersNames = new LinkedList<>();
        taskToShow.getSubscribers().forEach(userDTO -> usersNames.add(userDTO.getName()));
        tasksListView.getItems().add("Subscribers Names: " + usersNames);
        //this.tasksListView.getItems().add("Total Price For Task: " + taskToShow.getIndependentAmount()); //todo add
        //this.tasksListView.getItems().add("Current Number Of Workers: " + taskToShow.getIndependentAmount());
    }

    public static TaskDTO getSelectedTask(SimpleStringProperty selectedTask) {
        TaskDTO toReturn = null;
        List<TaskDTO> allTasksDTOS = SharedDashboard.getAllTasksDTOS();
        if(selectedTask.get() != null) {
            for (TaskDTO task : allTasksDTOS) {
                if (task.getTaskName().compareTo(selectedTask.get()) == 0)
                    toReturn = task;
            }
        }
        return toReturn;
    }


    public static String getLoggedInUserName(Label loggedInLabel){
        String str = loggedInLabel.getText();
        String tmp = str.substring(str.indexOf("[") +1, str.indexOf("]"));
        return tmp;
    }
}
