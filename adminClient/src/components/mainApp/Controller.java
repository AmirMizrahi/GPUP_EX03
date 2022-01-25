package components.mainApp;

import javafx.scene.Node;

public interface Controller {
    Node getNodeController();

    void setNodeController(Node node);

    void setMainAppController(MainAppController newMainAppController);
}
