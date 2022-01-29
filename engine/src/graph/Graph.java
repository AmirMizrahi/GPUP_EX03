package graph;

import exceptions.TargetNotFoundException;
import managers.Manager;
import targets.*;
import java.util.*;

public class Graph {
    private final String graphName;
    private final String uploaderName;
    private final Map<String, Target> targetNameToTarget;
    private final int[] targetTypeArray;
    private boolean needToCalculate;

    public Graph(String graphName, String uploaderName) {
        this.graphName = graphName;
        this.uploaderName = uploaderName;
        this.targetNameToTarget = new HashMap<>();
        this.targetTypeArray = new int[] {0,0,0,0};
        this.needToCalculate = true;
    }

    public Boolean addTarget(Target t){
        boolean res = true;
        if(this.targetNameToTarget.containsKey(t.getName().toUpperCase()))
            res = false;
        this.targetNameToTarget.put(t.getName().toUpperCase(),t);
        this.needToCalculate = true;
        //updateAmountForEachTargetType(t.getCategory());
        return res;
    }

    private void calculateTargetsTypes(){
        for (Target currentTarget: this.targetNameToTarget.values()) {
            if(currentTarget.getCategory() == Target.TargetType.INDEPENDENT)
                targetTypeArray[0]++;
            else if(currentTarget.getCategory()== Target.TargetType.LEAF)
                targetTypeArray[1]++;
            else if(currentTarget.getCategory() == Target.TargetType.MIDDLE)
                targetTypeArray[2]++;
            else if(currentTarget.getCategory() == Target.TargetType.ROOT)
                targetTypeArray[3]++;
        }
    }

    //The method returns int[], int[0] = INDEPENDENT , int[1] = LEAF , int[2] = MIDDLE , int[3] = ROOT
    public int[] getTargetTypeArray() {
        if(needToCalculate) {
            calculateTargetsTypes();
            needToCalculate = false;
        }
        return targetTypeArray;
    } //Need to change

    public Target getTargetByName(String targetName) throws TargetNotFoundException {
        Target toReturn = targetNameToTarget.get(targetName.toUpperCase());
        if(toReturn == null)
            throw new TargetNotFoundException(targetName);
        return toReturn;
    }

    public List<Target> getTargetsList(){
        List<Target> resTargetList = new ArrayList<>();
        targetNameToTarget.values().forEach(resTargetList::add);
        return resTargetList;
    }

    public String getGraphName() {
        return graphName;
    }

    public List<String> findAllPathsBetweenTwoTargets(Target s, Target t, String relation)
    {
        Map<String,Boolean> targetNameToVisitStatus = new HashMap<>();
        ArrayList currentPath = new ArrayList<>();
        List<String> toReturn = new ArrayList<>();

        if(relation.compareTo("REQUIRED_FOR") == 0){
            Target temp = t;
            t = s;
            s = temp;
        }

        for (Target tt: targetNameToTarget.values())        {
            targetNameToVisitStatus.put(tt.getName(),false);
        }

        currentPath.add(s);
        dfs(s, t, targetNameToVisitStatus, currentPath, toReturn,true);
        List<String> tmp = new ArrayList<>();
        tmp.addAll(toReturn);


        if(relation.compareTo("REQUIRED_FOR") == 0){
            toReturn.clear();
            for (String str:tmp){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.delete(0,stringBuilder.capacity());
                stringBuilder.append(str,1,str.length()-1);
                stringBuilder.reverse();
                stringBuilder.insert(stringBuilder.length(),"]");
                stringBuilder.insert(0,"[");
                toReturn.add(stringBuilder.toString());
            }
        }
        return toReturn;
    }

    private void dfs(Target u, Target t, Map<String,Boolean> beingVisited, List currentPath, List<String> output, boolean firstTime)
    {
        beingVisited.replace(u.getName(),true); // to avoid cycle

        if (u.equals(t) && !firstTime)
        {
            output.add((currentPath).toString());
            beingVisited.replace(u.getName(),false);
            return;
        }

        for (String targetName : u.getOutTargets())
        {
            Target i = this.targetNameToTarget.get(targetName);
            if (!beingVisited.get(i.getName()))
            {
                currentPath.add(i);
                dfs(i, t, beingVisited, currentPath, output,false);
                currentPath.remove(i);
            }
        }
        beingVisited.replace(u.getName(),false);
    }

    //DFS Based (aka realDFS)
    public List<String> checkIfTargetIsPartOfCycle(Target TargetToLookFor){
        Map<Target,Integer> targetToColor = new HashMap<>();
        ArrayList currentPath = new ArrayList<>();
        List<String> toReturn = new ArrayList<>();

        for (Target target: this.targetNameToTarget.values()){
            targetToColor.put(target,0);
        }
        visit(targetToColor,TargetToLookFor,TargetToLookFor, currentPath,toReturn);
        if (toReturn.size() > 0){
            toReturn.add(0,TargetToLookFor.getName());
            toReturn.add(toReturn.size(),TargetToLookFor.getName());
        }
        return toReturn;
    }

    private void visit(Map<Target,Integer> targetToColor, Target current, Target TargetToLookFor, List<Target> currentPath, List<String> output){
        targetToColor.put(current,1);
        for (String targetName: current.getOutTargets()) {
            Target t = this.targetNameToTarget.get(targetName);
            if(targetToColor.get(t) == 0){
                currentPath.add(t);
                visit(targetToColor,t,TargetToLookFor,currentPath,  output);
                currentPath.remove(t);
            }

            else if(targetToColor.get(t) == 1 && t.equals(TargetToLookFor)){
                currentPath.forEach(partOfList-> output.add(partOfList.getName()));
                return;
            }
        }
    }

    //BFS based
    public List<Target> findAllEffectedTargets(Target target, Manager.DependencyRelation relation) {
        Queue<Target> queue = new LinkedList<>();
        List<String> inOrOutList;
        List<Target> toReturn = new LinkedList<>();
        Map<Target,Boolean> targetToVisitStatus = new HashMap<>();
        targetNameToTarget.values().forEach(x->targetToVisitStatus.put(x,false));

        queue.add(target);
        targetToVisitStatus.put(target,true);
        while (!queue.isEmpty()){
            Target current = queue.remove();
            if (relation == Manager.DependencyRelation.DEPENDS_ON)
                inOrOutList = current.getOutTargets();
            else
                inOrOutList = current.getInTargets();
            inOrOutList.forEach(neighborOfCurrent ->{
                try {
                    if(!targetToVisitStatus.get(this.getTargetByName(neighborOfCurrent))) {
                        queue.add(this.targetNameToTarget.get(neighborOfCurrent));
                        targetToVisitStatus.put(this.targetNameToTarget.get(neighborOfCurrent),true);
                        toReturn.add(this.targetNameToTarget.get(neighborOfCurrent));
                    }
                } catch (TargetNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
        toReturn.remove(target);

        return toReturn;
    }

    public String getUploaderName() {
        return this.uploaderName;
    }
}
