package DTO;

import graph.SerialSet;
import java.util.LinkedList;
import java.util.List;

public class SerialSetDTO {
    private SerialSet set;

    public SerialSetDTO (SerialSet set) {
        this.set = set;
    }

    public String getName(){
        return set.getName();
    }

    public List<String> getAllTargetsName(){
        List<String> returnedList = new LinkedList<>();
        set.getTargetsInSerialSet().forEach(serialSet->returnedList.add(serialSet.getName()));

        return returnedList;
    }
}
