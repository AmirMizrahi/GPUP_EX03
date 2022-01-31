package sharedDashboard;

import DTO.SerialSetsDTO;
import DTO.TargetDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class UserTableViewRow {
    //Properties
    private final SimpleStringProperty userName;
    private final SimpleStringProperty userType;

    public UserTableViewRow(String userName, String userType) {
        this.userName = new SimpleStringProperty(userName);
        this.userType = new SimpleStringProperty(userType);
    }

    public String getUserName() {
        return userName.get();
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public String getUserType() {
        return userType.get();
    }

    public SimpleStringProperty userTypeProperty() {
        return userType;
    }
}
