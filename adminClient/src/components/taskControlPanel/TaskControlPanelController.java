package components.taskControlPanel;

import DTO.TargetDTO;
import DTO.TaskDTO;
import Utils.HttpClientUtil;
import com.google.gson.Gson;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import static Utils.Constants.*;

public class TaskControlPanelController implements Controller {

    //Controllers
    private MainAppController mainAppController;
    private Node nodeController;
    private List<String> inProcessData;
    private List<String> frozenData;
    private List<String> waitingData;
    private List<String> finishedData;
    private List<String> skippedData;
    private List<TargetDTO> tasksTargets;
    //
    //UI
    //Label
    @FXML private Label taskNameLabel;
    @FXML private Label graphNameLabel;
    @FXML private Label summaryTargetsAmountLabel;
    @FXML private Label summaryIndependentsLabel;
    @FXML private Label summaryLeafLabel;
    @FXML private Label summaryMiddleLabel;
    @FXML private Label summaryRootLabel;
    @FXML private Label registeredWorkersLabel;
    @FXML private Label currentWorkingTargetsLabel;
    @FXML private Label progressBarLabel;
    //Button
    @FXML private Button startButton;
    @FXML private Button pauseResumeButton;
    @FXML private Button stopButton;
    @FXML private Button clearButton;
    //TableView / Column
    @FXML private TableView<String> frozenTableView;
    @FXML private TableColumn<String, String> frozenNameCol;
    @FXML private TableView<String> waitingTableView;
    @FXML private TableColumn<String, String> waitingNameCol;
    @FXML private TableView<String> inProgressTableView;
    @FXML private TableColumn<String, String> inProgressNameCol;
    @FXML private TableView<String> skippedTableView;
    @FXML private TableColumn<String, String> skippedNameCol;
    @FXML private TableView<String> finishedTableView;
    @FXML private TableColumn<String, String> finishedNameCol;
    //Other UI
    @FXML private ListView<String> targetInfoListView;
    @FXML private ProgressBar taskProgressBar;
    @FXML private TextArea logTextArea;

    public TaskControlPanelController(){
        inProcessData = new LinkedList<>();
        frozenData = new LinkedList<>();
        waitingData = new LinkedList<>();
        finishedData = new LinkedList<>();
        skippedData = new LinkedList<>();
    }

    @Override
    public void setMainAppController(MainAppController newMainAppController) {
        this.mainAppController = newMainAppController;
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
    void clearButtonAction(ActionEvent event) {

    }

    @FXML
    void pauseResumeButtonAction(ActionEvent event) {

    }

    @FXML
    void startButtonAction(ActionEvent event) {
        String body = "taskStatus=play" + LINE_SEPARATOR + "taskName="+taskNameLabel.getText();

        HttpClientUtil.postRequest(RequestBody.create(body.getBytes()) , new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    HttpClientUtil.createErrorPopup("Server down!", e.getMessage()).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(()->HttpClientUtil.createErrorPopup("Update Task Error!", responseBody).show());
                }
                else {
                    //update client somehow?
                }
                response.close();
            }
        }, UPDATE_TASK_STATUS);

    }

    @FXML
    void stopButtonAction(ActionEvent event) {

    }

    @FXML
    void whenClickedOnRowAction(MouseEvent event) {
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

    private void showTargetOnTable(String targetName) throws XMLException {
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

    private TargetDTO searchForRefreshedTarget(String targetName){
        TargetDTO dto = null;
        for (TargetDTO targetDTO : this.tasksTargets) {
            if (targetName.compareTo(targetDTO.getTargetName()) == 0){
                dto = targetDTO;
            }
        }
        return dto;
    }

    public void refreshPanel(TaskDTO selectedTaskDTOFromDashboard) {
        if (selectedTaskDTOFromDashboard != null) {
            this.taskNameLabel.setText(selectedTaskDTOFromDashboard.getTaskName());
            this.graphNameLabel.setText(selectedTaskDTOFromDashboard.getGraphName());
            this.summaryTargetsAmountLabel.setText(selectedTaskDTOFromDashboard.getAmountOfTargets().toString());
            this.summaryLeafLabel.setText(selectedTaskDTOFromDashboard.getLeafAmount().toString());
            this.summaryIndependentsLabel.setText(selectedTaskDTOFromDashboard.getIndependentAmount().toString());
            this.summaryMiddleLabel.setText(selectedTaskDTOFromDashboard.getMiddleAmount().toString());
            this.summaryRootLabel.setText(selectedTaskDTOFromDashboard.getRootAmount().toString());
            this.tasksTargets = selectedTaskDTOFromDashboard.getAllTargets();
            updateTables(tasksTargets);
            updateProgressBar();
        }
    }

    private void updateProgressBar() {
        Platform.runLater(()-> {
            double value = calculateValueForProgressBar();
            taskProgressBar.setProgress(value/100);
            progressBarLabel.setText(String.format("%.1f",value) + "%");
            if (value == 100) {
                //this.taskInAction.set(false);
                //this.enableThreadsChange.set(true);
                //this.createTaskController.reportTaskFinished();
                this.pauseResumeButton.setText("Pause");
                //this.createTaskController.informPauseToMainActivateTaskController(false);
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
            customiseFactory(finishedNameCol);
            //colorCellsInFinishedTable();
        });
    }

    private void customiseFactory(TableColumn<String, String> calltypel) {
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

    private void printAccordingToCurrentStatus(TargetDTO targetDTO) throws TargetNotFoundException, XMLException {
        if(targetDTO.getTargetStatus().compareTo("FROZEN") == 0){
            String buffer ="Targets which keeps '" +targetDTO.getTargetName() + "' FROZEN: ";
            //List<String> transitives = targetDTO.getTransitiveOutList();
            List<String> transitives = this.mainAppController.getAllEffectedTargets(targetDTO.getTargetName(), "Depends On");
            for (String transitiveTargetName : transitives) {
                for (TargetDTO target : this.tasksTargets) {
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
           // long timeElapsed = Duration.between(startTaskTime, currentTime).toMillis(); //todo the starting time will provided start
           // this.targetInfoListView.getItems().add("Target '"+ targetDTO.getTargetName() + "' is waiting for " + timeElapsed +"ms");
        }

        else if(targetDTO.getTargetStatus().compareTo("SKIPPED") == 0){
            String buffer ="Failed targets which caused '" +targetDTO.getTargetName() + "' becoming SKIPPED: ";
            List<String> transitives = this.mainAppController.getAllEffectedTargets(targetDTO.getTargetName(), "Depends On");
            for (String transitiveTargetName : transitives) {
                for (TargetDTO target : this.tasksTargets) {
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

    public void initializeProcessLog() {
        Platform.runLater(()->{
            //ObservableList<CheckBox> checkBoxes = this.createTaskController.getCheckBoxes();
            //checkBoxes.forEach(x->x.setSelected(false));
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
            taskProgressBar.setProgress(0.0); //todo ?????
            progressBarLabel.setText("0.0%");
            logTextArea.clear();
        });
    }
}
