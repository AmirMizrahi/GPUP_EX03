package DTO;

import exceptions.TargetNotFoundException;
import graph.Graph;
import managers.Manager;
import targets.Target;

import java.util.LinkedList;
import java.util.List;

public class GraphDTO {
    private final Graph graph;

    public GraphDTO(Graph graph) {
        this.graph = graph;
    }

    public Integer getAmountOfTargets(){
        int[] arrayOfTargetTypes = graph.getTargetTypeArray();
        return arrayOfTargetTypes[0] + arrayOfTargetTypes[1] + arrayOfTargetTypes[2] + arrayOfTargetTypes[3];
    }

    public Integer getIndependentAmount(){return this.graph.getTargetTypeArray()[0];}

    public Integer getLeafAmount(){return this.graph.getTargetTypeArray()[1];}

    public Integer getMiddleAmount(){return this.graph.getTargetTypeArray()[2];}

    public Integer getRootAmount(){return this.graph.getTargetTypeArray()[3];}

    public List<TargetDTO> getAllTargets(){
        List<TargetDTO> targetDTOS = new LinkedList<>();
        List<Target> targets = this.graph.getTargetsList();
        for (Target t: targets) {
            targetDTOS.add(new TargetDTO(t));
        }
        return targetDTOS;
    }

    public String getGraphName() {
        return this.graph.getGraphName();
    }

    public List<TargetDTO> getAllEffected(String targetName, String relationAsString) {
        List<TargetDTO> toReturn = new LinkedList<>();
        if (relationAsString.contains("Depends"))
            relationAsString = "DEPENDS_ON";
        else
            relationAsString = "REQUIRED_FOR";
        try {
            List<Target> targets = this.graph.findAllEffectedTargets(this.graph.getTargetByName(targetName), Manager.DependencyRelation.valueOf(relationAsString));
            targets.forEach(target -> toReturn.add(new TargetDTO(target)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public List<String> findAllPathsBetweenTwoTargets(String s, String t, String relation){
        List<String> toReturn = new LinkedList<>();
        try {
            toReturn = this.graph.findAllPathsBetweenTwoTargets(this.graph.getTargetByName(s), this.graph.getTargetByName(t), relation);
        } catch (TargetNotFoundException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public List<String> checkIfTargetIsPartOfCycle(String targetName) {
        List<String> toReturn = new LinkedList<>();
        try {
            toReturn = this.graph.checkIfTargetIsPartOfCycle(this.graph.getTargetByName(targetName));
        } catch (TargetNotFoundException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public String getUploaderName() {
        return this.graph.getUploaderName();
    }
}
