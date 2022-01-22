package components.basicInformation;

import DTO.SerialSetDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SerialSetTableViewRow {

    //Properties
    private final SimpleIntegerProperty serialSetHash;
    private final SimpleStringProperty serialSetName;
    private final SimpleStringProperty serialSetTargetList;
    //

    public SerialSetTableViewRow(SerialSetDTO dto, int numberOfSS) {
        this.serialSetHash = new SimpleIntegerProperty(numberOfSS);
        this.serialSetName = new SimpleStringProperty(dto.getName());
        this.serialSetTargetList = new SimpleStringProperty(dto.getAllTargetsName().toString()) {
        };
    }

    public String getSerialSetName() {
        return serialSetName.get();
    }

    public SimpleStringProperty serialSetNameProperty() {
        return serialSetName;
    }

    public String getSerialSetTargetList() {
        return serialSetTargetList.get();
    }

    public SimpleStringProperty serialSetTargetListProperty() {
        return serialSetTargetList;
    }

    public int getSerialSetHash() {
        return serialSetHash.get();
    }

    public SimpleIntegerProperty serialSetHashProperty() {
        return serialSetHash;
    }
}
