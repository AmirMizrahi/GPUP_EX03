package DTO;

import targets.Target;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

public class TargetDTO {
    private Target target;

    public TargetDTO(Target targetToInsert){
        this.target = targetToInsert;
    }

    public String getTargetName(){return this.target.getName();}

    public String getTargetCategory(){return this.target.getCategory().toString();}

    public List<String> getOutList(){
        //List<String> res = new LinkedList<>();
        //this.target.getOutTargets().forEach(t->res.add(t.getName()));
        //return res;
        return target.getOutTargets();
    }

    public List<String> getInList(){
        //List<String> res = new LinkedList<>();
        //this.target.getInTargets().forEach(t->res.add(t.getName()));
        //return res;
        return target.getInTargets();
    }

    public Instant getTargetStartingTime(){
        return target.getStartingTime();
    }

//    public List<String> getTransitiveOutList(){
//        List<String> res = new LinkedList<>();
//        this.target.getOutTargets().forEach(t->res.add(t.getName()));
//        return res;
//    }
//
//    public List<String> getTransitiveInList(){
//        List<String> res = new LinkedList<>();
//        this.target.getInTargets().forEach(t->res.add(t.getName()));
//        return res;
//    }

    public String getTargetData(){return this.target.getData();}

    public String getTargetStatus(){return this.target.getStatus().toString();}

    public String getTargetStatusAfterTask(){return this.target.getStatusAfterTask().toString();}

    public List<String> getLogs(){return this.target.getLogs();}
}
