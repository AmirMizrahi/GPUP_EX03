package components.basicInformation;

import DTO.SerialSetsDTO;
import DTO.TargetDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;

public class TargetsTableViewRow {

    //Properties
    private final SimpleIntegerProperty targetNumber;
    private final SimpleStringProperty targetName;
    private final SimpleStringProperty targetType;
    private final SimpleIntegerProperty targetDirectDep;
    private final SimpleIntegerProperty targetTotalDep;
    private final SimpleIntegerProperty targetDirectReq;
    private final SimpleIntegerProperty targetTotalReq;
    private final SimpleStringProperty targetData;
    private final SimpleIntegerProperty targetSSAmount;
    private final BooleanProperty deselect = new SimpleBooleanProperty();
    //
    //UI
    private CheckBox checkBox;
    //

    public TargetsTableViewRow(TargetDTO targetDTO, SerialSetsDTO serialSetsDTO, int numberOfTarget, int numberOfTransitiveDep, int numberOfTransitiveReq, boolean del) {
        this.targetNumber = new SimpleIntegerProperty(numberOfTarget);
        this.targetName = new SimpleStringProperty(targetDTO.getTargetName());
        this.targetType = new SimpleStringProperty(targetDTO.getTargetCategory().toString());
        this.targetDirectDep = new SimpleIntegerProperty(targetDTO.getOutList().size());
        this.targetTotalDep = new SimpleIntegerProperty(numberOfTransitiveDep);
        this.targetDirectReq = new SimpleIntegerProperty(targetDTO.getInList().size());
        this.targetTotalReq = new SimpleIntegerProperty(numberOfTransitiveReq);
        this.targetData = new SimpleStringProperty(targetDTO.getTargetData());
        this.targetSSAmount = new SimpleIntegerProperty(serialSetsDTO.getSerialSetForTarget(targetDTO.getTargetName()).size());
        deselect.set(del);
        this.checkBox = new CheckBox();
        this.checkBox.setAccessibleText(targetDTO.getTargetName());
    }

    public CheckBox getCheckBox(){
        return this.checkBox;
    }

    public void setTemp(CheckBox checkBox){
        this.checkBox = checkBox;
    }

    public BooleanProperty deselectProperty() { return deselect; }

    public int getTargetNumber() {
        return targetNumber.get();
    }

    public void setTargetName(String a){ this.targetName.set(a);}

    public String getTargetName() {
        return this.targetName.get();
    }

    public SimpleIntegerProperty targetNumberProperty() {
        return targetNumber;
    }

    public SimpleStringProperty targetNameProperty() {
        return targetName;
    }

    public int getTargetDirectDep() {
        return targetDirectDep.get();
    }

    public SimpleIntegerProperty targetDirectDepProperty() {
        return targetDirectDep;
    }

    public int getTargetDirectReq() {
        return targetDirectReq.get();
    }

    public SimpleIntegerProperty targetDirectReqProperty() {
        return targetDirectReq;
    }

    public String getTargetData() {
        return targetData.get();
    }

    public SimpleStringProperty targetDataProperty() {
        return targetData;
    }

    public String getTargetType() {
        return targetType.get();
    }

    public SimpleStringProperty targetTypeProperty() {
        return targetType;
    }

    public int getTargetSSAmount() {
        return targetSSAmount.get();
    }

    public SimpleIntegerProperty targetSSAmountProperty() {
        return targetSSAmount;
    }

    public int getTargetTotalDep() {
        return targetTotalDep.get();
    }

    public SimpleIntegerProperty targetTotalDepProperty() {
        return targetTotalDep;
    }

    public int getTargetTotalReq() {
        return targetTotalReq.get();
    }

    public SimpleIntegerProperty targetTotalReqProperty() {
        return targetTotalReq;
    }
}