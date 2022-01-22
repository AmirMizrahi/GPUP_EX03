package managers;

import DTO.*;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import graph.*;
import targets.Target;
import tasks.*;
import resources.jaxb.schema.generated.GPUPDescriptor;
import resources.jaxb.schema.generated.GPUPTarget;
import resources.jaxb.schema.generated.GPUPTargetDependencies;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Manager implements Serializable {
    private Graph originalGraph, tempGraph;
    private Map<String,List<SerialSet>> targetToListOfSerialSets;
    private Set<SerialSet> serialSets;
    private final static String JAXB_XML_GAME_PACKAGE_NAME = "resources.jaxb.schema.generated";
    private String mainSimulationTaskFolderPath;
    private Boolean fileLoaded = false;
    private int maxParallelism;
    private final List<String> taskNames;
    private static boolean pauseOccurred;
    private Object isPaused;
    private ExecutorManager executorManager;

    public Manager() {
        this.taskNames = new LinkedList<>();
        this.taskNames.add("Simulation");
        this.taskNames.add("Compilation");
        this.tempGraph = new Graph();
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

    private boolean checkIfTargetInlist(LinkedList<Target> list,Target targetToSearch){
        boolean res = false;
        for (Target current:list) {
            if (current.getName().compareToIgnoreCase(targetToSearch.getName()) == 0) {
                res = true;
                break;
            }
        }
        return res;
    }

    public void loadXMLFileMG(String fullPathFileName) throws TargetNotFoundException, JAXBException, FileNotFoundException, XMLException {
        Graph newGraph = new Graph();
        ArrayList<String> duplicatedTargets = new ArrayList<>();
        Boolean flag;
        checkIfFileEndsWithXML(fullPathFileName.trim());
        InputStream inputStream = new FileInputStream(new File(fullPathFileName.trim()));
        GPUPDescriptor descriptor = deserializeFrom(inputStream);
        //Create target by name only
        for (GPUPTarget gpupTarget: descriptor.getGPUPTargets().getGPUPTarget()) {
            flag =  newGraph.addTarget(new Target(gpupTarget.getName().trim(), gpupTarget.getGPUPUserData()));
            if(!flag)
                duplicatedTargets.add(gpupTarget.getName().trim());
        }
        if(duplicatedTargets.size() > 0)
            throw new XMLException("The following Targets are duplicate: " + duplicatedTargets);

        this.setTargetsInAndOutLists(descriptor,newGraph);
        checkForTwoTargetsCycle(descriptor, newGraph);
        this.setTargetsType(descriptor, newGraph);
        this.mainSimulationTaskFolderPath = descriptor.getGPUPConfiguration().getGPUPWorkingDirectory().trim();

        Map<String,List<SerialSet>> newSerialSetMap = loadSerialSets(descriptor, newGraph);
        //if all ok
        this.fileLoaded = true;
        this.originalGraph = newGraph;
        this.targetToListOfSerialSets = newSerialSetMap;
        updateSerialSets();
        this.maxParallelism = descriptor.getGPUPConfiguration().getGPUPMaxParallelism();
        //this.originalGraph.getTargetsList().forEach(target -> this.currentStatusOfTargets.put(target.getName(),target));
    }

    private void updateSerialSets() {
        this.serialSets = new HashSet<>();

        for (List<SerialSet> value : this.targetToListOfSerialSets.values()) {
            this.serialSets.addAll(value);
        }
    }

    private Map<String,List<SerialSet>> loadSerialSets(GPUPDescriptor descriptor, Graph newGraph) throws TargetNotFoundException, XMLException {
        Map<String,List<SerialSet>> returnedMap = new HashMap<>();

        List<Target> targets = newGraph.getTargetsList();
        targets.forEach(x->returnedMap.put(x.getName(), new LinkedList<>()));
        try {
            List<GPUPDescriptor.GPUPSerialSets.GPUPSerialSet> serialSets = descriptor.getGPUPSerialSets().getGPUPSerialSet();
            for (GPUPDescriptor.GPUPSerialSets.GPUPSerialSet serialSet : serialSets) {
                SerialSet current = new SerialSet(serialSet.getName());
                String[] targetsInSerialSetByName = serialSet.getTargets().split(",");
                for (String s:targetsInSerialSetByName) {
                    current.add(newGraph.getTargetByName(s));
                    returnedMap.get(s).add(current);
                }
            }

            checkForDuplicateSerialSetName(descriptor);
        }
        catch(NullPointerException ignored){}

        return returnedMap;
    }

    private void checkForDuplicateSerialSetName(GPUPDescriptor descriptor) throws XMLException {
        List<GPUPDescriptor.GPUPSerialSets.GPUPSerialSet> serialSets = descriptor.getGPUPSerialSets().getGPUPSerialSet();
        final Set<String> set = new HashSet<>();
        for (GPUPDescriptor.GPUPSerialSets.GPUPSerialSet serialSet : serialSets){
            if(!set.add(serialSet.getName()))
                throw new XMLException("Duplicate serial set name - " + serialSet.getName());

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
            for (Target y: x.getOutTargets()) {
                if(y.getOutTargets().contains(x))
                    throw new XMLException("Can't load file as there is a 2 targets cycle contains: " + x.getName() + " & " + y.getName());
            }
            for (Target y: x.getInTargets()) {
                if(y.getInTargets().contains(x))
                    throw new XMLException("Can't load file as there is a 2 targets cycle contains: " + x.getName() + " & " + y.getName());
            }
        }
    }

    private static GPUPDescriptor deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (GPUPDescriptor) u.unmarshal(in);
    }

    public GraphDTO showBasicInformationAboutEntireGraphMG() throws XMLException {//INDEPENDENT, LEAF, MIDDLE, ROOT
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");
        return new GraphDTO(this.originalGraph);
    }

    public SerialSetsDTO showBasicInformationAboutSerialSetsMG() throws XMLException {
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");
        return new SerialSetsDTO(serialSets, targetToListOfSerialSets);
    }

    public TargetDTO showInformationAboutSpecificTargetMG(String targetName) throws XMLException, TargetNotFoundException {
        // //chk if target in originalGraph
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");
        Target t = this.originalGraph.getTargetByName(targetName);
        return new TargetDTO(t);
    }

    public PathsBetweenTwoTargetsDTO findALlPathsBetweenTwoTargetsMG(String source, String destination, DependencyRelation relation) throws TargetNotFoundException, XMLException {

        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");

        List<String> res = new ArrayList<>();
        List<String> tmp;
        StringBuilder stringBuilder = new StringBuilder();

        if(relation == DependencyRelation.DEPENDS_ON)
            res = originalGraph.findAllPathsBetweenTwoTargets(this.originalGraph.getTargetByName(source),this.originalGraph.getTargetByName(destination));
        else{
            tmp = originalGraph.findAllPathsBetweenTwoTargets(this.originalGraph.getTargetByName(destination),this.originalGraph.getTargetByName(source));
            for (String str:tmp)
            {
                stringBuilder.delete(0,stringBuilder.capacity());
                stringBuilder.append(str,1,str.length()-1);
                stringBuilder.reverse();
                stringBuilder.insert(stringBuilder.length(),"]");
                stringBuilder.insert(0,"[");
                res.add(stringBuilder.toString());
            }
        }
        return new PathsBetweenTwoTargetsDTO(res);
    }

    private void createGraphByUserSelection(List<String> selectedTargets) throws TargetNotFoundException {
        List<Target> oldInTargetList, oldOutTargetList;
        this.tempGraph = new Graph();

        for (String targetName : selectedTargets) { //Create new targets in the new graph (without in/out list)
            Target originalTarget = this.originalGraph.getTargetByName(targetName);
            tempGraph.addTarget(new Target(targetName, originalTarget.getData()));
        }

        //for every target in the new graph:                                            V
        //      1.get the InList of the target in the originalGraph                     V
        //      2.On every target in the (1) that does appear in the tempGraph
        //      3.add the target from the new graph to the new Inlist of the target

        for (Target currentTargetInTempGraph : tempGraph.getTargetsList()) {
            oldInTargetList = originalGraph.getTargetByName(currentTargetInTempGraph.getName()).getInTargets();
            oldOutTargetList = originalGraph.getTargetByName(currentTargetInTempGraph.getName()).getOutTargets();

            for (Target oldTarget : oldInTargetList) {
                if (selectedTargets.contains(oldTarget.getName())) {
                    currentTargetInTempGraph.addToInTargetsList(tempGraph.getTargetByName(oldTarget.getName()));
                }
            }
            for (Target oldTarget : oldOutTargetList) {
                if (selectedTargets.contains(oldTarget.getName())) {
                    currentTargetInTempGraph.addToOutTargetsList(tempGraph.getTargetByName(oldTarget.getName()));
                }
            }
        }
        setStatusOfTargetsInTempGraph();
    }

    private void setStatusOfTargetsInTempGraph() throws TargetNotFoundException {

        for (Target current : tempGraph.getTargetsList()) {
            int outTargetAmount = tempGraph.getTargetByName(current.getName()).getOutTargets().size();
            int inTargetAmount = tempGraph.getTargetByName(current.getName()).getInTargets().size();

            if (outTargetAmount > 0 && inTargetAmount > 0) {
                tempGraph.getTargetByName(current.getName()).setCategory(Target.TargetType.MIDDLE);
                tempGraph.getTargetByName(current.getName()).setStatus(Target.TargetStatus.FROZEN);
            } else if (outTargetAmount > 0) {
                tempGraph.getTargetByName(current.getName()).setCategory(Target.TargetType.ROOT);
                tempGraph.getTargetByName(current.getName()).setStatus(Target.TargetStatus.FROZEN);
            } else if (inTargetAmount > 0)
                tempGraph.getTargetByName(current.getName()).setCategory(Target.TargetType.LEAF);
        }
    }

    public void activateSimulationTask(List<String> selectedTargets , Integer taskTime, SimulationTask.TIME_OPTION op, Double ChanceToSucceed, Double SucceedWithWarning,
                                   AbstractTask.WAYS_TO_START_SIM_TASK way, List<Consumer<String>> consumerList,Consumer<File> consumeWhenFinished, int threadsNumber) throws TargetNotFoundException, XMLException, IOException, InterruptedException {
        if(way == AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH)
            createGraphByUserSelection(selectedTargets);

        List<Target> targetList = runTaskInFirstTime(way);
        Task simulationTask = new SimulationTask(taskTime,op,ChanceToSucceed, SucceedWithWarning, way, targetList,
                this.mainSimulationTaskFolderPath, checkIfTaskIsActivatedInFirstTime());

        activateTaskMG(simulationTask, consumerList,consumeWhenFinished, threadsNumber);
    }

    public void activateCompilationTask(List<String> selectedTargets, String source, String destination, AbstractTask.WAYS_TO_START_SIM_TASK way, List<Consumer<String>> consumerList,
                                        Consumer<File> consumeWhenFinished,int threadsNumber) throws TargetNotFoundException, XMLException, IOException, InterruptedException {
        if(way == AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH)
            createGraphByUserSelection(selectedTargets);

        List<Target> targetList = runTaskInFirstTime(way);
        Task compilationTask = new CompilationTask(way, targetList, this.mainSimulationTaskFolderPath, source, destination, checkIfTaskIsActivatedInFirstTime());

        activateTaskMG(compilationTask, consumerList,consumeWhenFinished, threadsNumber);
    }
    //activateComp =>Task comp = new comp

    private void activateTaskMG(Task task, List<Consumer<String>> consumerList,Consumer<File> consumeWhenFinished, int threadsNumber) throws XMLException, IOException, InterruptedException, IllegalArgumentException, TargetNotFoundException {
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");

//        if(way == AbstractTask.WAYS_TO_START_SIM_TASK.FROM_SCRATCH)
//            createGraphByUserSelection(selectedTargets);

//        List<Target> targetList = runTaskInFirstTime(way);
        Runnable r = () -> {
//            Task simulationTask = new SimulationTask(taskTime,op,ChanceToSucceed, SucceedWithWarning, way, targetList,
//                    this.mainSimulationTaskFolderPath, checkIfTaskIsActivatedInFirstTime());
            executorManager = new ExecutorManager(task,this.tempGraph.getTargetsList(),consumerList,consumeWhenFinished,this.targetToListOfSerialSets, threadsNumber, maxParallelism, isPaused);
            try {
                executorManager.execute();
//                Graph newGraph = new Graph();
//                targets.forEach(x->newGraph.addTarget(x));
//                this.originalGraph = newGraph;
            } catch (InterruptedException | IOException ignored) {}
        };
        Thread activateTaskThread = new Thread(r,"TaskThread");
        activateTaskThread.start();
    }

    private List<Target> runTaskInFirstTime(AbstractTask.WAYS_TO_START_SIM_TASK way){
        List<Target> targetList = new LinkedList<>();
        switch (way){
            case FROM_SCRATCH:
                targetList = runFromScratch();
                break;
            case INCREMENTAL:
                targetList = runIncremental();
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

    private List<Target> runFromScratch() {
        List<Target> targetList = this.tempGraph.getTargetsList();

        for (Target target: targetList) {
            Target.TargetType myType = target.getCategory();
            target.setStatusAfterTask(Target.StatusAfterTask.SKIPPED);
            if (myType == Target.TargetType.MIDDLE || myType == Target.TargetType.ROOT)
                target.setStatus(Target.TargetStatus.FROZEN);
            else
                target.setStatus(Target.TargetStatus.WAITING);
        }
        return targetList;
    }

    private List<Target> runIncremental() {
        List<Target> targetList = this.tempGraph.getTargetsList();

        for (Target target: targetList) {
            Target.StatusAfterTask myFinishedStatus = target.getStatusAfterTask();
            if (myFinishedStatus == Target.StatusAfterTask.SKIPPED || myFinishedStatus == Target.StatusAfterTask.FAILURE) {
                target.setStatus(Target.TargetStatus.WAITING);
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + "printed from runIncremental" + target.getName() + "changed is status to waiting" + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                for (Target child : target.getOutTargets()) {
                    if (child.getStatusAfterTask() == Target.StatusAfterTask.FAILURE || (child.getStatusAfterTask() == Target.StatusAfterTask.SKIPPED))
                        target.setStatus(Target.TargetStatus.FROZEN);
                }
            }
        }

        for (Target target: targetList) {
            Target.StatusAfterTask myFinishedStatus = target.getStatusAfterTask();
            if(myFinishedStatus == Target.StatusAfterTask.SKIPPED || myFinishedStatus == Target.StatusAfterTask.FAILURE)
                target.setStatusAfterTask(Target.StatusAfterTask.SKIPPED);
        }
        return targetList;
    }

    public PathsBetweenTwoTargetsDTO checkIfTargetIsPartOfCycleMG(String targetName) throws TargetNotFoundException, XMLException {
        if (!this.fileLoaded)
            throw new XMLException("XML not loaded");

       List<String> res = this.originalGraph.checkIfTargetIsPartOfCycle(this.originalGraph.getTargetByName(targetName));

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

    public enum DependencyRelation
    {
        DEPENDS_ON, REQUIRED_FOR, KA
    }

    //What If
    public List<TargetDTO> getAllEffectedTargets(String targetName, DependencyRelation relation) throws TargetNotFoundException {
        List<TargetDTO> targetDTOS = new LinkedList<>();
        List<Target> targetsAsString = this.originalGraph.findAllEffectedTargets(this.originalGraph.getTargetByName(targetName),relation);

        targetsAsString.forEach(t->targetDTOS.add(new TargetDTO(t)));
        return targetDTOS;
    }

    public int getParallelTaskAmount() {
        return this.maxParallelism;
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
            this.originalGraph = temp.originalGraph;
            this.mainSimulationTaskFolderPath = temp.mainSimulationTaskFolderPath;
            this.fileLoaded = temp.fileLoaded;
        }
    }

    public List<TargetDTO> getCurrentTargetsStatus() {
        List<TargetDTO> status = new LinkedList<>();
        this.tempGraph.getTargetsList().forEach(target -> status.add(new TargetDTO(target)));
        return status;
    }
}
