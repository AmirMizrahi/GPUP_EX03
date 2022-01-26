package DTO;

import graph.Graph;
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
}
