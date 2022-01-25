package components.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

public class DashboardController {

    @FXML
    private TableView<?> tasksTableView;

    @FXML
    private ListView<?> tasksListView;

    @FXML
    private TableView<?> graphsTableView;

    @FXML
    private ListView<?> graphsListView;

    @FXML
    private TableView<?> usersTableView;

    @FXML
    private Label loggedInLabel;

    @FXML
    void graphsTableViewOnClicked(MouseEvent event) {

    }

    @FXML
    void tasksTableViewOnClicked(MouseEvent event) {

    }

}
