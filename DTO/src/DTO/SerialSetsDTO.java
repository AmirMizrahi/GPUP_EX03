package DTO;

import graph.SerialSet;
import targets.Target;

import java.util.*;

public class SerialSetsDTO {
    private Set<SerialSet> set;
    private Map<String,List<SerialSet>> targetNameToListOfSerialSets;

    public SerialSetsDTO(Set<SerialSet> set, Map<String,List<SerialSet>> map){
        this.set = set;
        this.targetNameToListOfSerialSets = new HashMap<>();
        map.keySet().forEach(key->this.targetNameToListOfSerialSets.put(key, map.get(key)));
    }

    public List<SerialSetDTO> getSetList() {
        List<SerialSetDTO> dto = new LinkedList<>();
        set.forEach(x->dto.add(new SerialSetDTO(x)));
        return dto;
    }

    public List<SerialSetDTO> getSerialSetForTarget(String targetName){
        List<SerialSetDTO> dto = new LinkedList<>();
        this.targetNameToListOfSerialSets.get(targetName).forEach(x-> dto.add(new SerialSetDTO(x)));
        return dto;
    }
}
