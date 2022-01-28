package components.activateTask.processLogs;

import DTO.SerialSetDTO;
import DTO.TargetDTO;
import components.activateTask.mainActivateTask.ActivateTaskController;
import components.activateTask.mainActivateTask.MainActivateTaskController;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ProcessLogController implements ActivateTaskController {

    //Controllers
    private MainActivateTaskController mainActivateTaskController;
    private Node nodeController;
    //
    //UI
    @FXML private Button startButton;
    @FXML private Button pauseResumeButton;
    @FXML private TableView<String> frozenTableView;
    @FXML private TableColumn<String,String> frozenNameCol;
    @FXML private TableView<String> waitingTableView;
    @FXML private TableColumn<String,String> waitingNameCol;
    @FXML private TableView<String> inProgressTableView;
    @FXML private TableColumn<String, String> inProgressNameCol;
    @FXML private TableView<String> skippedTableView;
    @FXML private TableColumn<String,String> skippedNameCol;
    @FXML private TableView<String> finishedTableView;
    @FXML private TableColumn<String,String> finishedNameCol;
    @FXML private ListView<String> targetInfoListView;
    @FXML private ProgressBar taskProgressBar;
    @FXML private Label progressBarLabel;
    @FXML private Button clearButton;
    @FXML private TextArea logTextArea;
    //
    private SimpleBooleanProperty taskInAction;
    private SimpleBooleanProperty enableThreadsChange;
    private boolean isPause;

    private List<String> inProcessData;
    private List<String> frozenData;
    private List<String> waitingData;
    private List<String> finishedData;
    private List<String> skippedData;
    private List<TargetDTO> newTargetsStatus;
    private Instant startTaskTime;


    public ProcessLogController(){
        inProcessData = new LinkedList<>();
        frozenData = new LinkedList<>();
        waitingData = new LinkedList<>();
        finishedData = new LinkedList<>();
        skippedData = new LinkedList<>();
        this.newTargetsStatus = new LinkedList<>();
    }

    @Override
    public void setMainActivateTaskController(MainActivateTaskController mainActivateTaskController) {
        this.mainActivateTaskController = mainActivateTaskController;
    }

    @Override
    public Node getNodeController(){
        return this.nodeController;
    }

    @Override
    public void setNodeController(Node node){
        this.nodeController = node;
    }

    @FXML
    private void initialize(){
        this.taskInAction = new SimpleBooleanProperty(false);
        this.enableThreadsChange = new SimpleBooleanProperty(true);
        this.isPause = false;
        this.pauseResumeButton.disableProperty().bind(this.taskInAction.not());
    }

    @FXML
    private void whenClickedOnRowAction(MouseEvent event){

        //Get the pressed target name from the tables
        String temp = event.getPickResult().toString();
        int start = temp.indexOf("'");
        int last = temp.lastIndexOf("'");
        if(start == -1){
            start = temp.indexOf("\"");
            last = temp.lastIndexOf("\"");
        }
        try {
            String name = temp.substring(++start, last);
            if(name.compareTo("null") != 0)
                this.showTargetOnTable(name);
        }
        catch (Exception e) {};
    }

    @FXML
    void clearButtonAction(ActionEvent event) {
        this.logTextArea.clear();
    }

    @FXML
    void pauseResumeButtonAction(ActionEvent event) {
        boolean pause = false;
        if(pauseResumeButton.getText().compareTo("Pause") == 0){
            pauseResumeButton.setText("Resume");
            pause = true;
            this.isPause= true;
            this.enableThreadsChange.set(true);
        }

        else {
            pauseResumeButton.setText("Pause");
            this.isPause= false;
            this.enableThreadsChange.set(false);
        }

        this.mainActivateTaskController.informPauseToMainActivateTaskController(pause);
    }

    @FXML
    void startButtonAction(ActionEvent event) {
        this.enableThreadsChange.set(false);
        startTaskTime = Instant.now();
        Consumer<File> consumeWhenFinished = new Consumer<File>() {
            @Override
            public void accept(File targetLogFile) {
                BufferedReader br
                        = null;
                try {
                    br = new BufferedReader(new FileReader(targetLogFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // Declaring a string variable
                String st;
                // Condition holds true till
                // there is character in a string

                while (true) {
                    try {
                        if (!((st = br.readLine()) != null)) break;
                        String finalSt = st;
                        Platform.runLater(()->logTextArea.appendText(finalSt + "\n"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        this.taskInAction.set(true);
        this.mainActivateTaskController.activateTask(consumeWhenFinished);
    }

    public void updateProcess(List<TargetDTO> newTargetsStatus) {
        //Platform.runlater
        //Instant now
        this.newTargetsStatus = newTargetsStatus;
        updateTables(newTargetsStatus);
        updateProgressBar();
    }

    private void updateProgressBar() {
        Platform.runLater(()-> {
            double value = calculateValueForProgressBar();
            taskProgressBar.setProgress(value/100);
            progressBarLabel.setText(String.format("%.1f",value) + "%");
            if (value == 100) {
                this.taskInAction.set(false);
                this.enableThreadsChange.set(true);
                this.mainActivateTaskController.reportTaskFinished();
                this.pauseResumeButton.setText("Pause");
                this.mainActivateTaskController.informPauseToMainActivateTaskController(false);
            }
        });
    }

    private double calculateValueForProgressBar(){
        double finished = this.finishedData.size();
        double skipped = this.skippedData.size();
        double waiting = this.waitingData.size();
        double inProcess = this.inProcessData.size();
        double frozen = this.frozenData.size();

        if(isOnlyDummy(this.finishedData))
            finished = finished - 1;

        if(isOnlyDummy(this.skippedData))
            skipped = skipped - 1;

        if(isOnlyDummy(this.waitingData))
            waiting = waiting - 1;

        if(isOnlyDummy(this.inProcessData))
            inProcess = inProcess - 1;

        if(isOnlyDummy(this.frozenData))
            frozen = frozen - 1;

        return (finished + skipped) /
                (skipped + waiting + inProcess + frozen + finished)
                * 100;
    }

    private boolean isOnlyDummy(List<String> list){
        boolean answer = false;
        if(list.get(0).compareTo("") == 0)
            answer = true;
        return answer;

    }

    private void updateTables(List<TargetDTO> newTargetsStatus) {
        wireColumn(inProgressNameCol);
        wireColumn(frozenNameCol);
        wireColumn(finishedNameCol);
        wireColumn(skippedNameCol);
        wireColumn(waitingNameCol);
        Platform.runLater(()-> {
            deleteAllLists();
            newTargetsStatus.forEach(target -> {
                filterTargetToList(target.getTargetStatus(),target.getTargetName());
            });
            putAllDataInTables(frozenTableView, frozenData, frozenNameCol);
            putAllDataInTables(inProgressTableView, inProcessData, inProgressNameCol);
            putAllDataInTables(waitingTableView, waitingData, waitingNameCol);
            putAllDataInTables(skippedTableView, skippedData, skippedNameCol);
            putAllDataInTables(finishedTableView, finishedData, finishedNameCol);
            customiseFactory(finishedNameCol, newTargetsStatus);
            //colorCellsInFinishedTable();
        });
    }

    private void customiseFactory(TableColumn<String, String> calltypel, List<TargetDTO> newTargetsStatus) {
        calltypel.setCellFactory(column -> {
            return new TableCell<String, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    setText(empty ? "" : getItem().toString());
                    setGraphic(null);

                    TableRow<String> currentRow = getTableRow();
                    try {

                        TargetDTO dto = searchForRefreshedTarget(item);
                        String statusAfter = dto.getTargetStatusAfterTask();

                        if (!isEmpty()) {
                            if(statusAfter.compareTo("FAILURE") == 0)
                                currentRow.setStyle("-fx-background-color:lightcoral");
                            else if(statusAfter.compareTo("SUCCESS") == 0)
                                currentRow.setStyle("-fx-background-color:lightgreen");
                            else if (statusAfter.compareTo("WARNING") == 0)
                                currentRow.setStyle("-fx-background-color:#e3e329");
                        }
                    } catch (NullPointerException ignore) {};
                }
            };
        });
    }

    private TargetDTO searchForRefreshedTarget(String targetName){
        TargetDTO dto = null;
        for (TargetDTO targetDTO : newTargetsStatus) {
            if (targetName.compareTo(targetDTO.getTargetName()) == 0){
                dto = targetDTO;
            }
        }
        return dto;
    }

    private void putAllDataInTables(TableView<String> table, List<String> list, TableColumn<String,String> col) {
        if(list.size() > 1)
            list.remove(0);
        final ObservableList<String> data = FXCollections.observableArrayList(list);
        table.setItems(data);
        table.getColumns().clear();
        table.getColumns().addAll(col);
    }

    private void wireColumn(TableColumn<String,String> column){
        column.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue()));
    }

    private void deleteAllLists(){
        inProcessData.clear();
        frozenData.clear();
        waitingData.clear();
        finishedData.clear();
        skippedData.clear();
        inProcessData.add(0,"");
        frozenData.add(0,"");
        waitingData.add(0,"");
        finishedData.add(0,"");
        skippedData.add(0,"");
    }

    private void filterTargetToList(String status, String targetName){
        switch (status){
            case "IN_PROCESS":
                inProcessData.add(targetName);
                break;
            case "WAITING":
                waitingData.add(targetName);
                break;
            case "SKIPPED":
                skippedData.add(targetName);
                break;
            case "FROZEN":
                frozenData.add(targetName);
                break;
            case "FINISHED":
                finishedData.add(targetName);
                break;
        }
    }

    private void showTargetOnTable(String targetName) throws XMLException {
        //TargetDTO targetDTO = this.mainActivateTaskController.getTargetInformationWhenTableClicked(targetName);
        TargetDTO targetDTO = searchForRefreshedTarget(targetName);
        List<String> serialSets = new LinkedList<>();
        Platform.runLater(()->{
            this.targetInfoListView.getItems().clear();
            this.targetInfoListView.getItems().add("Target name: " + targetDTO.getTargetName());
            this.targetInfoListView.getItems().add("Target category: " + targetDTO.getTargetCategory());
            this.targetInfoListView.getItems().add("Target dependOn list: " + targetDTO.getOutList());
            this.targetInfoListView.getItems().add("Target requiredFor list: " + targetDTO.getInList());
            this.targetInfoListView.getItems().add("Target data: " + targetDTO.getTargetData());
            try {
                printAccordingToCurrentStatus(targetDTO);
            } catch (TargetNotFoundException | XMLException ignore) {}

        });
    }

    private void printAccordingToCurrentStatus(TargetDTO targetDTO) throws TargetNotFoundException, XMLException {
        if(targetDTO.getTargetStatus().compareTo("FROZEN") == 0){
            String buffer ="Targets which keeps '" +targetDTO.getTargetName() + "' FROZEN: ";
            //List<String> transitives = targetDTO.getTransitiveOutList();
            List<String> transitives = this.mainActivateTaskController.getAllEffectedTargets(targetDTO.getTargetName(), "Depends On");
            for (String transitiveTargetName : transitives) {
                for (TargetDTO target : this.newTargetsStatus) {
                    if(target.getTargetName().compareTo(transitiveTargetName) == 0){
                        if((target.getTargetStatusAfterTask().compareTo("SUCCESS") != 0) && (target.getTargetStatusAfterTask().compareTo("WARNING") != 0))
                            buffer += target.getTargetName() +" ";
                    }
                }
            }
            this.targetInfoListView.getItems().add(buffer);
        }

        else if(targetDTO.getTargetStatus().compareTo("WAITING") == 0){
            Instant currentTime = Instant.now();
            long timeElapsed = Duration.between(startTaskTime, currentTime).toMillis();
            this.targetInfoListView.getItems().add("Target '"+ targetDTO.getTargetName() + "' is waiting for " + timeElapsed +"ms");
        }

        else if(targetDTO.getTargetStatus().compareTo("SKIPPED") == 0){
            String buffer ="Failed targets which caused '" +targetDTO.getTargetName() + "' becoming SKIPPED: ";
            List<String> transitives = this.mainActivateTaskController.getAllEffectedTargets(targetDTO.getTargetName(), "Depends On");
            for (String transitiveTargetName : transitives) {
                for (TargetDTO target : this.newTargetsStatus) {
                    if(target.getTargetName().compareTo(transitiveTargetName) == 0){
                        if((target.getTargetStatusAfterTask().compareTo("FAILURE") == 0))
                            buffer += target.getTargetName() +" ";
                    }
                }
            }
            this.targetInfoListView.getItems().add(buffer);
        }

        else if(targetDTO.getTargetStatus().compareTo("IN_PROCESS") == 0){
            long timeElapsed = Duration.between(targetDTO.getTargetStartingTime(), Instant.now()).toMillis();
            this.targetInfoListView.getItems().add("Target '"+ targetDTO.getTargetName() + "' being process for " + timeElapsed +"ms");
        }

        else if(targetDTO.getTargetStatus().compareTo("FINISHED") == 0)
            this.targetInfoListView.getItems().add("Target status after task: " + targetDTO.getTargetStatusAfterTask());
    }

    public void bindTargetsToButton(){
        ObservableList<CheckBox> checkBoxes = this.mainActivateTaskController.getCheckBoxes();
        SimpleBooleanProperty isCompilation = this.mainActivateTaskController.getIsCompilationSelectedProperty();
        SimpleBooleanProperty isSourceSelected = this.mainActivateTaskController.getIsSourceSelectedProperty();
        SimpleBooleanProperty isDestinationSelected = this.mainActivateTaskController.getIsDestinationSelectedProperty();
        this.startButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        ()->!checkBoxes.stream().anyMatch(CheckBox::isSelected),
                        checkBoxes.stream().map(x->x.selectedProperty()).toArray(Observable[]::new)
                ).or(this.taskInAction).or(isCompilation.and(isSourceSelected.not().or(isDestinationSelected.not())))
        );
    }

    public void initializeProcessLog() {
        Platform.runLater(()->{
            ObservableList<CheckBox> checkBoxes = this.mainActivateTaskController.getCheckBoxes();
            checkBoxes.forEach(x->x.setSelected(false));
            frozenTableView.getItems().clear();
            //frozenNameCol
            waitingTableView.getItems().clear();
            // waitingNameCol
            inProgressTableView.getItems().clear();
            //inProgressNameCol
            skippedTableView.getItems().clear();
            //skippedNameCol
            finishedTableView.getItems().clear();
            //finishedNameCol
            targetInfoListView.getItems().clear();
            taskProgressBar.setProgress(0.0);
            progressBarLabel.setText("0.0%");
            logTextArea.clear();
        });
    }

    public boolean isPause() {
        return this.isPause;
    }

    public SimpleBooleanProperty getEnableThreadsChangeProperty() {
        return enableThreadsChange;
    }
}