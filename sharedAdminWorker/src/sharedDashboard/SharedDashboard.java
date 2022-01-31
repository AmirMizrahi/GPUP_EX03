package sharedDashboard;

import DTO.GraphDTO;
import DTO.TaskDTO;
import DTO.UserDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import sharedControllers.sharedMainAppController;

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
        SharedDashboard.usersTableView = usersTableView;
        SharedDashboard.userTableColumn = userTableColumn;
        SharedDashboard.typeTableColumn = typeTableColumn;
        SharedDashboard.graphsTableView = graphsTableView;
        SharedDashboard.graphTableColumn = graphTableColumn;
        SharedDashboard.tasksTableView = tasksTableView;
        SharedDashboard.taskTableColumn = taskTableColumn;
        userTimer = new Timer();
        graphTimer = new Timer();
        taskTimer = new Timer();
        userRefresher = new UserListRefresher(SharedDashboard::updateUsersList);
        graphRefresher = new GraphListRefresher(getServerOnProperty(), SharedDashboard::updateGraphList);
        taskRefresher = new TaskListRefresher(SharedDashboard::updateTaskList);

        userTimer.schedule(userRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
        graphTimer.schedule(graphRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
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
        });
    }

    public static void wireColumnForUserList(TableColumn column, String property) {
        column.setCellValueFactory(
                new PropertyValueFactory<>(property));
    }

    public static List<GraphDTO> getAllGraphsDTOS() {return allGraphsDTOS;}

    public static List<TaskDTO> getAllTasksDTOS() {return allTasksDTOS;}

}
