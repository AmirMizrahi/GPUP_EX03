package managers;

import DTO.*;
import User.User;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import graph.*;
import resources.jaxb.schema.generated.GPUPConfiguration;
import resources.jaxb.schema.generated.GPUPDescriptor;
import resources.jaxb.schema.generated.GPUPTarget;
import resources.jaxb.schema.generated.GPUPTargetDependencies;
import targets.Target;
import tasks.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static Utilss.Constants.DEFAULT_WORKING_DIR;

public class Manager implements Serializable {
    private GraphManager graphManager;
    private TaskManager taskManager;
    private Graph tempGraph;
    private final static String JAXB_XML_GAME_PACKAGE_NAME = "resources.jaxb.schema.generated";
    private String mainSimulationTaskFolderPath = DEFAULT_WORKING_DIR; //
    private Boolean fileLoaded = false;
    private final List<String> taskNames;
    private static boolean pauseOccurred;
    private Object isPaused;
    private ExecutorManager executorManager;

    public Manager() {
        this.taskNames = new LinkedList<>();
        this.taskNames.add("Simulation");
        this.taskNames.add("Compilation");
        this.graphManager = new GraphManager();
        this.taskManager = new TaskManager();
        this.tempGraph = new Graph("aaa", "aaa");//todo
        pauseOccurred = false;
        isPaused = new Object();
    }

    private void setTargetsType(GPUPDescriptor descriptor, Graph newGraph) throws TargetNotFoundException {
        for (GPUPTarget gpupTarget: descriptor.getGPUPTargets().getGPUPTarget()) {
            int outTargetAmount =  newGraph.getTargetByName(gpupTarget.getName().trim()).getOutTargets().size();
            int inTargetAmount =  newGraph.getTargetByName(gpupTarget.getName().trim()).getInTargets().size();

            if(outTargetAmount > 0 && inTargetAmount > 0){
                newGraph.getTargetByName(gpupTarget.getName().trim()).setCategory(Target.TargetType.MIDDLE);
                newGraph.getTargetByName(gpupTarget.getName().trim()).setStatus(Target.TargetStatus.FROZEN);
            }

            else if(outTargetAmount > 0) {
                newGraph.getTargetByName(gpupTarget.getName().trim()).setCategory(Target.TargetType.ROOT);
                newGraph.getTargetByName(gpupTarget.getName().trim()).setStatus(Target.TargetStatus.FROZEN);
            }

            else if(inTargetAmount > 0)
                newGraph.getTargetByName(gpupTarget.getName().trim()).setCategory(Target.TargetType.LEAF);
        }
    }

    public void setTargetsInAndOutLists(GPUPDescriptor descriptor, Graph newGraph) throws XMLException, TargetNotFoundException {
        for (GPUPTarget gpupTarget: descriptor.getGPUPTargets().getGPUPTarget()) {
            try {
                List<GPUPTargetDependencies.GPUGDependency> currentTargetDependencies = gpupTarget.getGPUPTargetDependencies().getGPUGDependency();
                for (GPUPTargetDependencies.GPUGDependency dependency : currentTargetDependencies) {
                    Target currentTarget = newGraph.getTargetByName(gpupTarget.getName().trim());
                    Target subTarget;
                    try {
                        subTarget = newGraph.getTargetByName(dependency.getValue());
                    } catch (TargetNotFoundException e) {
                        throw new XMLException(String.format("%s %s %s but %s not found.", currentTarget.getName(), dependency.getType(), dependency.getValue(), dependency.getValue()));
                    }
                    if (dependency.getType().compareTo("requiredFor") == 0 && (!this.checkIfTargetInlist(currentTarget.getInTargets(), subTarget))) {
                        currentTarget.addToInTargetsList(subTarget);
                        subTarget.addToOutTargetsList(currentTarget);
                    } else if (dependency.getType().compareTo("dependsOn") == 0 && (!this.checkIfTargetInlist(currentTarget.getOutTargets(), subTarget))) {
                        currentTarget.addToOutTargetsList(subTarget);
                        subTarget.addToInTargetsList(currentTarget);
                    }
                }
            }
            catch(NullPointerException ignored){}
        }
    }

    private boolean checkIfTargetInlist(LinkedList<String> list,Target targetToSearch){
        boolean res = false;
        for (String current : list) {
            if (current.compareToIgnoreCase(targetToSearch.getName()) == 0) {
                res = true;
                break;
            }
        }
        return res;
    }

    public void loadXMLFileMG(InputStream inputStream, String uploaderName) throws TargetNotFoundException, JAXBException, XMLException {
        ArrayList<String> duplicatedTargets = new ArrayList<>();
        Boolean flag;
        //checkIfFileEndsWithXML(fullPathFileName.trim()); //todo
        GPUPDescriptor descriptor = deserializeFrom(inputStream);
        String graphName = descriptor.getGPUPConfiguration().getGPUPGraphName().trim();
        if(graphManager.getGraphs().keySet().contains(graphName)){ //check if graph already uploaded
            throw new XMLException(graphName + " already uploaded.");
        }
        Graph newGraph = new Graph(graphName, uploaderName);
        //Create target by name only
        for (GPUPTarget gpupTarget: descriptor.getGPUPTargets().getGPUPTarget()) {
            flag =  newGraph.addTarget(new Target(gpupTarget.getName().trim(), gpupTarget.getGPUPUserData()));
            if(!flag)
                duplicatedTargets.add(gpupTarget.getName().trim());
        }
        if(duplicatedTargets.size() > 0)
            throw new XMLException("The following Targets are duplicate: " + duplicatedTargets);

        updateTaskPrices(descriptor,newGraph);

        this.setTargetsInAndOutLists(descriptor,newGraph);
        checkForTwoTargetsCycle(descriptor, newGraph);
        this.setTargetsType(descriptor, newGraph);

        //Check if name already exits //todo do me

        //if all ok
        this.fileLoaded = true;
        this.graphManager.addGraph(graphName ,newGraph);
        System.out.println("Graph Name :" + graphName );
        //this.graphManager.getTargetsList().forEach(target -> this.currentStatusOfTargets.put(target.getName(),target));
    }

    private void updateTaskPrices(GPUPDescriptor descriptor, Graph newGraph) throws XMLException{
        for (GPUPConfiguration.GPUPPricing.GPUPTask gpupTask:descriptor.getGPUPConfiguration().getGPUPPricing().getGPUPTask()) {
            if(gpupTask.getName().compareTo("Simulation") == 0) {
                if(gpupTask.getPricePerTarget() <= 0)
                    throw new XMLException("Simulation price must be positive value");
                newGraph.setSimulationPrice(gpupTask.getPricePerTarget());
            }
            else if(gpupTask.getName().compareTo("Compilation") == 0) {
                if(gpupTask.getPricePerTarget() <= 0)
                    throw new XMLException("Compilation price must be positive value");
                newGraph.setCompilationPrice(gpupTask.getPricePerTarget());
            }
        }

    }

    private void checkIfFileEndsWithXML(String fullPathFileName) throws FileNotFoundException {
        if(!fullPathFileName.endsWith(".xml") && !fullPathFileName.endsWith(".XML"))
            throw new FileNotFoundException("Error, fullPathFileName needs to end with .xml or .XML\nXML not loaded!");
    }

    private void checkForTwoTargetsCycle(GPUPDescriptor descriptor, Graph graph) throws XMLException, TargetNotFoundException {
        Target x;
        for (GPUPTarget gpupTarget: descriptor.getGPUPTargets().getGPUPTarget()) {
            x = graph.getTargetByName(gpupTarget.getName().trim());
            for (String yName: x.getOutTargets()) {
                Target y = graph.getTargetByName(yName);
                if(y.getOutTargets().contains(x.getName()))
                    throw new XMLException("Can't load file as there is a 2 targets cycle contains: " + x.getName() + " & " + y.getName());
            }
            for (String yName: x.getInTargets()) {
                Target y = graph.getTargetByName(yName);
                if(y.getInTargets().contains(x.getName()))
                    throw new XMLException("Can't load file as there is a 2 targets cycle contains: " + x.getName() + " & " + y.getName());
            }
        }
    }

    private static GPUPDescriptor deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (GPUPDescriptor) u.unmarshal(in);
    }

    public GraphDTO showBasicInformationAboutEntireGraphMG(String graphName) throws XMLException {//INDEPENDENT, LEAF, MIDDLE, ROOT
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");
        return new GraphDTO(this.graphManager.getGraphs().get(graphName));
    }

    public TargetDTO showInformationAboutSpecificTargetMG(String graphName, String targetName) throws XMLException, TargetNotFoundException {
        // //chk if target in graphManager
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");
        Target t = this.graphManager.getGraphs().get(graphName).getTargetByName(targetName);
        return new TargetDTO(t);
    }

    private List<Target> createTaskTargetsByUserSelection(String graphName, List<String> selectedTargets) throws TargetNotFoundException {
        List<String> oldInTargetList, oldOutTargetList;
        List<Target> res = new LinkedList<>();

        for (String targetName : selectedTargets) { //Create new targets in the new graph (without in/out list)
            Target originalTarget = this.graphManager.getGraphs().get(graphName).getTargetByName(targetName);
            res.add(new Target(targetName, originalTarget.getData()));
        }

        //for every target in the new graph:                                            V
        //      1.get the InList of the target in the graphManager                     V
        //      2.On every target in the (1) that does appear in the tempGraph
        //      3.add the target from the new graph to the new Inlist of the target

        for (Target currentTargetInTempGraph : res) {
            oldInTargetList = graphManager.getGraphs().get(graphName).getTargetByName(currentTargetInTempGraph.getName()).getInTargets();
            oldOutTargetList = graphManager.getGraphs().get(graphName).getTargetByName(currentTargetInTempGraph.getName()).getOutTargets();

            for (String oldTargetName : oldInTargetList) {
                Target oldTarget = this.graphManager.getGraphs().get(graphName).getTargetByName(oldTargetName);
                if (selectedTargets.contains(oldTarget.getName())) {
                    res.forEach(x->{
                        if(x.getName().compareTo(oldTarget.getName()) == 0)
                            currentTargetInTempGraph.addToInTargetsList(x);
                    });
                }
            }
            for (String oldTargetName : oldOutTargetList) {
                Target oldTarget = this.graphManager.getGraphs().get(graphName).getTargetByName(oldTargetName);
                if (selectedTargets.contains(oldTarget.getName())) {
                    res.forEach(x->{
                        if(x.getName().compareTo(oldTarget.getName()) == 0)
                            currentTargetInTempGraph.addToOutTargetsList(x);
                    });
                }
            }
        }
        setStatusOfTargetsInTempGraph(res);
        return res;
    }

    private void setStatusOfTargetsInTempGraph(List<Target> list) {

        for (Target current : list) {
            int outTargetAmount = current.getOutTargets().size();
            int inTargetAmount = current.getInTargets().size();

            if (outTargetAmount > 0 && inTargetAmount > 0) {
               current.setCategory(Target.TargetType.MIDDLE);
               current.setStatus(Target.TargetStatus.FROZEN);
            } else if (outTargetAmount > 0) {
                current.setCategory(Target.TargetType.ROOT);
                current.setStatus(Target.TargetStatus.FROZEN);
            } else if (inTargetAmount > 0)
                current.setCategory(Target.TargetType.LEAF);
        }
    }

    public void activateSimulationTask(List<String> selectedTargets, Integer taskTime, SimulationTask.TIME_OPTION op, Double ChanceToSucceed, Double SucceedWithWarning,
                                       AbstractTask.WAYS_TO_START_SIM_TASK way, List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished, String graphName) throws TargetNotFoundException, XMLException, IOException, InterruptedException {
        if(way == AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH);
            //createGraphByUserSelection(selectedTargets); //todo

        List<Target> targetList = runTaskInFirstTime(way);
       // Task simulationTask = new SimulationTask(taskTime,op,ChanceToSucceed, SucceedWithWarning, way, targetList,
       //         this.mainSimulationTaskFolderPath, checkIfTaskIsActivatedInFirstTime(), "bla bla change me", "changeMeTOoooooo");

        //activateTaskMG(simulationTask, consumerList,consumeWhenFinished, graphName);
    }

    public void activateCompilationTask(List<String> selectedTargets, String source, String destination, AbstractTask.WAYS_TO_START_SIM_TASK way, List<Consumer<String>> consumerList,
                                        Consumer<File> consumeWhenFinished, String graphName) throws TargetNotFoundException, XMLException, IOException, InterruptedException {
        if(way == AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH);
            //createGraphByUserSelection(selectedTargets); //todo

        List<Target> targetList = runTaskInFirstTime(way);
       // Task compilationTask = new CompilationTask(way, targetList, this.mainSimulationTaskFolderPath, source, destination, checkIfTaskIsActivatedInFirstTime(), "asdf", "xczv");

        //activateTaskMG(compilationTask, consumerList,consumeWhenFinished,graphName);
    }
    //activateComp =>Task comp = new comp

    private void activateTaskMG(Task task, List<Consumer<String>> consumerList,Consumer<File> consumeWhenFinished, String graphName) throws XMLException, IOException, InterruptedException, IllegalArgumentException, TargetNotFoundException {
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");

        Runnable r = () -> {
            Map<String,List<SerialSet>> dummy = new HashMap<>();
            executorManager = new ExecutorManager(task,this.tempGraph.getTargetsList(),consumerList,consumeWhenFinished,dummy,1 /*todo change this*/, 100/*todo change this*/,
                    isPaused, this.graphManager.getGraphs().get(graphName));
            try {
                executorManager.execute();
            } catch (InterruptedException | IOException ignored) {}
        };
        Thread activateTaskThread = new Thread(r,"TaskThread");
        activateTaskThread.start();
    }

    private List<Target> runTaskInFirstTime(AbstractTask.WAYS_TO_START_SIM_TASK way) throws TargetNotFoundException {
        List<Target> targetList = new LinkedList<>();
        switch (way){
            case FROM_SCRATCH:
                //targetList = addTaskFromScratch(taskName);
                break;
            case INCREMENTAL:
               // targetList = addTaskIncremental(taskName);
                break;
        }
        return targetList;
    }

    private boolean checkIfTaskIsActivatedInFirstTime() {
        Predicate<Target> isThisFirstTime = (target) -> target.getStatusAfterTask() == Target.StatusAfterTask.SKIPPED;
        boolean firstTime = true;

        for (Target currentTarget : this.tempGraph.getTargetsList()) {
            if (!isThisFirstTime.test(currentTarget)) {
                firstTime = false;
            }
        }
        return firstTime;
    }

    public void addTaskFromExits(String taskName, AbstractTask.WAYS_TO_START_SIM_TASK wayToCreateFrom, String userName) throws Exception {
        if (wayToCreateFrom == AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH)
            addTaskFromScratch(taskName, userName);
        else
            addTaskIncremental(taskName, userName);
    }

    private void addTaskFromScratch(String taskName, String userName) throws Exception {
        List<String> selectedTargetsNames = new LinkedList<>();
        Task task = this.taskManager.getTasks().get(taskName);

        task.getTargets().forEach(target -> selectedTargetsNames.add(target.getName()));

        calculateAndSendParamsToAddNewTask(task, taskName, selectedTargetsNames,userName);
    }

    private void addTaskIncremental(String taskName, String userName) throws Exception {
        List<String> selectedTargetsNames = new LinkedList<>();
        Task task = this.taskManager.getTasks().get(taskName);

        task.getTargets().forEach(target -> {
            if (target.getStatusAfterTask() == Target.StatusAfterTask.SKIPPED || target.getStatusAfterTask() == Target.StatusAfterTask.FAILURE)
                selectedTargetsNames.add(target.getName());
        });

        calculateAndSendParamsToAddNewTask(task, taskName, selectedTargetsNames, userName);
    }

    private void calculateAndSendParamsToAddNewTask(Task task, String taskName, List<String> selectedTargetsNames, String uName) throws Exception {
        String userName = uName;
        String graphName = task.getGraphName();

        String newTaskName = taskName + "("+(task.getCounterName() + 1)+")";
        task.addOneToCounterName();

        if(task.getTaskType().compareToIgnoreCase("Simulation") == 0) {
            Integer time = Integer.parseInt(task.getTaskInfo().get("taskTime"));
            String time_option = task.getTaskInfo().get("op");
            Double successRates = Double.parseDouble(task.getTaskInfo().get("chanceToSucceed"));
            Double warningRates = Double.parseDouble(task.getTaskInfo().get("succeedWithWarning"));
            addNewTask(time,time_option,successRates,warningRates,userName,graphName,newTaskName,selectedTargetsNames);
        }

        else{
            String source = task.getTaskInfo().get("source");
            String dest = task.getTaskInfo().get("destination");
            addNewTask(source,dest,userName,graphName,newTaskName,selectedTargetsNames);
        }
    }

    public PathsBetweenTwoTargetsDTO checkIfTargetIsPartOfCycleMG(String graphName, String targetName) throws TargetNotFoundException, XMLException {
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");

       List<String> res = this.graphManager.getGraphs().get(graphName).checkIfTargetIsPartOfCycle(this.graphManager.getGraphs().get(graphName).getTargetByName(targetName));

        return new PathsBetweenTwoTargetsDTO(res);
    }

    public List<TargetDTO> getAllTargetsThatWereActivatedMG() {
        List<TargetDTO> toReturn = new LinkedList<>();
        tempGraph.getTargetsList().forEach(target -> toReturn.add(new TargetDTO(target)));
        return toReturn;
    }

    public void informPauseToManager(boolean pause) {
        pauseOccurred = pause;
        if(!pause){
            synchronized (isPaused) {
                isPaused.notifyAll();
            }
        }
    }

    public static boolean isPauseOccurred() {
        return pauseOccurred;
    }

    public void setNewThreadAmountMG(Integer value) {
        this.executorManager.setNewThreadPoolAmount(value);
    }

    public List<GraphDTO> getAllGraphs() {
        List<GraphDTO> toReturn = new LinkedList<>();

        for (Map.Entry<String, Graph> entry : this.graphManager.getGraphs().entrySet()) {
            toReturn.add(new GraphDTO(entry.getValue()));
        }

        return toReturn;
    }

    public List<TaskDTO> getAllTasks() {
        List<TaskDTO> toReturn = new LinkedList<>();

        for (Map.Entry<String, Task> entry : this.taskManager.getTasks().entrySet()) {
            String taskName = entry.getKey();
            String uploaderName = entry.getValue().getUploaderName();
            String graphName = entry.getValue().getGraphName();
            Set<Target> allTargets = entry.getValue().getTargets();
            String taskStatus = entry.getValue().getStatus().toString();
            List<User> users = entry.getValue().getSubscribers();
            toReturn.add(new TaskDTO(taskName,uploaderName, graphName, allTargets, taskStatus, users));
        }

        return toReturn;
    }

    public enum DependencyRelation
    {
        DEPENDS_ON, REQUIRED_FOR, KA
    }

    //What If
    public List<TargetDTO> getAllEffectedTargets(String graphName, String targetName, DependencyRelation relation) throws TargetNotFoundException {
        List<TargetDTO> targetDTOS = new LinkedList<>();
        List<Target> targetsAsString = this.graphManager.getGraphs().get(graphName).findAllEffectedTargets(this.graphManager.getGraphs().get(graphName).getTargetByName(targetName),relation);

        targetsAsString.forEach(t->targetDTOS.add(new TargetDTO(t)));
        return targetDTOS;
    }


    public List<String> getTasksNames(){
        return this.taskNames;
    }

    public void saveEngineMG(String fileToSavePath) throws IOException{
        try (ObjectOutputStream out =
                     new ObjectOutputStream(
                             new FileOutputStream(fileToSavePath + ".bin"))) {
            out.writeObject(this);
            out.flush();
        }
    }

    public void loadToEngineMG(String fileToLoadPath) throws IOException, ClassNotFoundException {
        try(ObjectInputStream in =      new ObjectInputStream(
                             new FileInputStream(fileToLoadPath + ".bin"))) {
            // we know that we read array list of Persons
            Manager temp = (Manager) in.readObject();
            //System.out.println("survivorsFromFile = " +
                   // survivorsFromFile);
            this.graphManager = temp.graphManager;
            this.mainSimulationTaskFolderPath = temp.mainSimulationTaskFolderPath;
            this.fileLoaded = temp.fileLoaded;
        }
    }

    public List<TargetDTO> getCurrentTargetsStatus() {
        List<TargetDTO> status = new LinkedList<>();
        this.tempGraph.getTargetsList().forEach(target -> status.add(new TargetDTO(target)));
        return status;
    }

//    private List<Target> createTargetsFromTargetsNames(List<String> selectedTargetsNames, String graphName){
//        List<Target> selectedTargets = new LinkedList<>();
//        selectedTargetsNames.forEach(targetName-> {
//            try {
//                selectedTargets.add(this.graphManager.getGraphs().get(graphName).getTargetByName(targetName));
//            } catch (TargetNotFoundException e) {
//                e.printStackTrace(); //todo remove
//            }
//        });
//
//        return selectedTargets;
//    }

    //Simulation creator
    public void addNewTask(Integer time, String time_option, Double successRates, Double warningRates, String userName,
                           String graphName,String taskName, List<String> selectedTargetsNames) throws Exception {

        if(taskManager.getTasks().keySet().contains(taskName)){ //check if task already uploaded
            throw new Exception(taskName + " already created.");
        }

        List<Target> selectedTargets = createTaskTargetsByUserSelection(graphName, selectedTargetsNames);


        Map<String,String> taskInfo = new HashMap<>();
        taskInfo.put("taskTime", time.toString());
        taskInfo.put("op", time_option);
        taskInfo.put("chanceToSucceed", successRates.toString());
        taskInfo.put("succeedWithWarning", warningRates.toString());

        Task task = new SimulationTask(taskInfo,time, SimulationTask.TIME_OPTION.valueOf(time_option.toUpperCase()), successRates,
                warningRates, AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH, selectedTargets, DEFAULT_WORKING_DIR ,
                true, userName, graphName); //todo change first time
        taskManager.addTask(taskName,task);
    }

    //Comp creator
    public void addNewTask(String source, String destination, String userName, String graphName, String taskName,
                           List<String> selectedTargetsNames) throws Exception {
        if(taskManager.getTasks().keySet().contains(taskName)){ //check if task already uploaded
            throw new Exception(taskName + " already created.");
        }

        List<Target> selectedTargets = createTaskTargetsByUserSelection(graphName, selectedTargetsNames);

        Map<String,String> taskInfo = new HashMap<>();
        taskInfo.put("source", source);
        taskInfo.put("destination", destination);

        Task task = new CompilationTask(taskInfo, AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH, selectedTargets, DEFAULT_WORKING_DIR ,
                source, destination, true, userName, graphName); //todo change first time
        taskManager.addTask(taskName,task);
    }

    public void updateTaskStatus(String taskName, AbstractTask.TASK_STATUS taskStatus) {
        this.taskManager.updateTaskStatus(taskName,taskStatus);
    }

    public void updateTaskSubscriber(String taskName, UserDTO userName) {
        User user = new User(userName.getName(),userName.getType(), userName.getThreadsAmount());

        for (Map.Entry<String, Task> entry : this.taskManager.getTasks().entrySet()) {
            if(entry.getKey().compareTo(taskName) == 0){
                entry.getValue().addSubscriber(user);
                break;
            }
        }
    }

    public List<TargetDTOForWorker> getTargetsForWorker(int availableThreads, String usernameFromParameter) {
        return this.taskManager.getTasksForWorker(usernameFromParameter, availableThreads);
    }

    public void updateTargetStatusAfterTask(String name, String taskName, String status) {
        for (Target target : taskManager.getTasks().get(taskName).getTargets()) {
            if (name.compareTo(target.getName()) == 0){
                target.setStatus(Target.TargetStatus.FINISHED);
                target.setStatusAfterTask(Target.StatusAfterTask.valueOf(status.toUpperCase()));
            }
        }
    }

    public void updateTargetsByTargetResult(String name, String taskName, String targetStatus) throws TargetNotFoundException {
        Target theOne = null;
        for (Target target : this.taskManager.getTasks().get(taskName).getTargets()) {
            if(name.compareTo(target.getName()) == 0){
                theOne = target;
                break;
            }
        }

        synchronized (taskManager.getTasks().get(taskName)) {
            if (theOne != null) {
                List<Target> targetList = new LinkedList<>();
                targetList.addAll(this.taskManager.getTasks().get(taskName).getTargets());
                onFinishedTarget(theOne, taskName, targetList, new LinkedList<>(), new Consumer<File>() { //todo temp
                    @Override
                    public void accept(File file) {

                    }
                });
            }
        }
    }

    public void onFinishedTarget(Target target, String taskName, List<Target> targetList, List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished) throws TargetNotFoundException {
        setStatusAfterTaskForAllEffected(target, taskName,targetList,consumerList, consumeWhenFinished);
        if (isJobDone(this.taskManager.getTasks().get(taskName).getTargets())){
            this.taskManager.getTasks().get(taskName).setStatus(AbstractTask.TASK_STATUS.FINISHED);
        }
    }

    private boolean isJobDone(Set<Target> targets) {
        boolean res = true;
        for (Target t: targets) {
            if (t.getStatus() != Target.TargetStatus.SKIPPED && t.getStatus() != Target.TargetStatus.FINISHED)
                res = false;
        }
        return res;
    }

    private void setStatusAfterTaskForAllEffected(Target target, String taskName, List<Target> targets,List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished) throws TargetNotFoundException {
        if(target.getStatusAfterTask() == Target.StatusAfterTask.FAILURE) {
            Map<Target, ThreadManager.DFS_COLOR> M = new HashMap<>();
            for (Target t: targets) {
                M.put(t, ThreadManager.DFS_COLOR.WHITE);
            }
            skippedDFS(target, taskName, target, M,consumerList);
            //Target B is now Skipped.
        }
        else//Success chain
            maybeWaitingChain(target, taskName, consumerList, consumeWhenFinished);
        //Target B is now Waiting.
    }

    //TargetStatus = WAITING, IN_PROCESS, SKIPPED, FROZEN, FINISHED --> NEVER DELETE ME!
    private void skippedDFS(Target target, String taskName, Target origin, Map<Target, ThreadManager.DFS_COLOR> M, List<Consumer<String>> consumerList) throws TargetNotFoundException {
        M.replace(target, ThreadManager.DFS_COLOR.GREY); // to avoid cycle

        if(!target.equals(origin)){
            target.setStatus(Target.TargetStatus.SKIPPED);
            //this.printerBridge.printStringToFileWithTimeStamp(String.format("Target %s is SKIPPED", target.getName()),consumerList);
        }
        for (String neighborName:target.getInTargets()) {
            Target neighbor = getTargetByNameFromSet(neighborName, this.taskManager.getTasks().get(taskName).getTargets());
            if (M.get(neighbor) == ThreadManager.DFS_COLOR.WHITE)
                skippedDFS(neighbor, taskName, origin, M,consumerList);
        }
        M.replace(target, ThreadManager.DFS_COLOR.BLACK);
    }

    private void maybeWaitingChain(Target target, String taskName, List<Consumer<String>> consumerList, Consumer<File> consumeWhenFinished) throws TargetNotFoundException {
        boolean allMyOutAreSuccess = true;
        for (String currentName : target.getInTargets()) {
            Target current = getTargetByNameFromSet(currentName, this.taskManager.getTasks().get(taskName).getTargets());
            if (current.getStatus() == Target.TargetStatus.FROZEN) {
                for (String outTargetName : current.getOutTargets()) {
                    Target outTarget = getTargetByNameFromSet(outTargetName, this.taskManager.getTasks().get(taskName).getTargets());
                    if (outTarget.getStatusAfterTask() != Target.StatusAfterTask.SUCCESS && outTarget.getStatusAfterTask() != Target.StatusAfterTask.WARNING) {
                        allMyOutAreSuccess = false;
                        break;
                    }
                }
                if (allMyOutAreSuccess) {
                    synchronized (current) {
                        current.setStatus(Target.TargetStatus.WAITING);
                        this.taskManager.getTasks().get(taskName).addToWaitingQueue(current);
                     //   this.printerBridge.printStringToFileWithTimeStamp(String.format("Target %s is WAITING", current.getName()), consumerList);
                     //   this.threadPool.execute(this.makeRunnable(current,isPaused, consumeWhenFinished));
                    }
                }

                allMyOutAreSuccess = true;
            }
        }
    }

    private Target getTargetByNameFromSet(String targetName, Set<Target> set){
        Target t = null;
        for (Target target : set) {
            if (targetName.compareTo(target.getName()) == 0) {
                t = target;
                break;
            }
        }
        return t;
    }
}
