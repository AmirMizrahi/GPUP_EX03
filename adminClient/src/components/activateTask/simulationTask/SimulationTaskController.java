package components.activateTask.simulationTask;

import components.activateTask.mainActivateTask.ActivateTaskController;
import components.activateTask.mainActivateTask.MainActivateTaskController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

public class SimulationTaskController implements ActivateTaskController {

    //Controllers
    private MainActivateTaskController mainActivateTaskController;
    private Node nodeController;
    //
    //UI
    @FXML private Spinner<Integer> processTimeSpinner = new Spinner<>();
    @FXML private ToggleGroup timeOptionToggle;
    @FXML private Slider successRatesSlider;
    @FXML private Label successRatesLabel;
    @FXML private Slider warningRatesSlider;
    @FXML private Label warningRatesLabel;
    //

    @Override
    public void setMainActivateTaskController(MainActivateTaskController mainActivateTaskController) {
        this.mainActivateTaskController = mainActivateTaskController;
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
        SpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(100,Integer.MAX_VALUE,1,100);
        processTimeSpinner.setValueFactory(factory);

        successRatesSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                successRatesSlider.setValue(newValue.doubleValue());
                successRatesLabel.setText(String.format("%.2f",successRatesSlider.getValue()));
            }
        });
        warningRatesSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                warningRatesSlider.setValue(newValue.doubleValue());
                warningRatesLabel.setText(String.format("%.2f",warningRatesSlider.getValue()));
            }
        });
    }

    public double getProcessTime() { return processTimeSpinner.getValue(); }

    public String getTimeOption() {
        return ((RadioButton)timeOptionToggle.getSelectedToggle()).getText();
    }

    public double getSuccessRates() {
        return successRatesSlider.getValue();
    }

    public double getWarningRates() {
        return warningRatesSlider.getValue();
    }
}
