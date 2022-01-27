package components.mainApp;

import javafx.scene.Node;

public interface ControllerW {
    Node getNodeController();

    void setNodeController(Node node);

    void setMainAppControllerW(MainAppControllerW newMainAppControllerW);
}
