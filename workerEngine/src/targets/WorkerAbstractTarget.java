package targets;

import DTO.TargetDTOForWorker;

import java.util.HashMap;
import java.util.Map;

public abstract class WorkerAbstractTarget implements WorkerTarget {
    protected Map<String, String> results = new HashMap<>();
    protected boolean isRunFinished = false;
    protected Integer workersAmount;
    protected Double progress;

    protected WorkerAbstractTarget(TargetDTOForWorker dto){
        results.put("taskName", dto.getTaskName());
        results.put("targetName", dto.getTargetDTO().getTargetName());
        results.put("status", "In Process");
        results.put("taskType", dto.getTaskType());
        this.workersAmount = dto.getWorkersAmount();
        this.progress = dto.getProgress();
    }

    protected void updateResults(String totalTime, String status) {
        results.put("totalTime", totalTime);
        results.put("status", status);
    }

    @Override
    public boolean isRunFinished() {
        return isRunFinished;
    }

    @Override
    public Map<String, String> getResult(){return this.results;}

    public String getTaskName() {return this.results.get("taskName");}

    public String getTargetName() {return this.results.get("targetName");}

    public String getStatus() {return this.results.get("status");}

    public String getTaskType() {return this.results.get("taskType");}

    @Override
    public Integer getWorkersAmount() {
        return workersAmount;
    }

    @Override
    public Double getProgress() {
        return progress;
    }
}
