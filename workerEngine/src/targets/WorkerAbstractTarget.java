package targets;

import DTO.TaskDTOForWorker;

import java.util.HashMap;
import java.util.Map;

public abstract class WorkerAbstractTarget implements WorkerTarget {
    protected Map<String, String> results = new HashMap<>();
    protected boolean isRunFinished;


    protected void updateResults(TaskDTOForWorker dto, String totalTime, String status) {
        results.put("taskName", dto.getTaskName());
        results.put("targetName", dto.getTargetDTO().getTargetName());
        results.put("totalTime", totalTime);
        results.put("status", status);
    }

    @Override
    public boolean isRunFinished() {
        return isRunFinished;
    }

    @Override
    public Map<String, String> getResult(){return this.results;}


}
