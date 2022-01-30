package components.createNewTask.createTask;

import javafx.scene.Node;

public interface TaskController {

    void setMainActivateTaskController(CreateTaskController createTaskController);

    Node getNodeController();

    public void setNodeController(Node node);
}
