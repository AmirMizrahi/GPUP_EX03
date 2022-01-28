package components.basicInformation;

import DTO.GraphDTO;
import DTO.SerialSetsDTO;
import DTO.TargetDTO;
import Utils.Constants;
import Utils.HttpClientUtil;
import components.graphMainComponent.Test2Controller;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import sun.applet.Main;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BasicInformationController implements Controller {

    //Controllers
    private Test2Controller mainAppControllerTest;
    private MainAppController mainAppController;
    private Node nodeController;
    //
    //UI
    // targetsTableView
    @FXML private TableView<TargetsTableViewRow> targetsTableView;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsNumberCol;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsNameCol;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsTypeCol;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsDirectDepCol;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsTotalDepCol;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsDirectReqCol;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsTotalReqCol;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsDataCol;
    @FXML private TableColumn<TargetsTableViewRow, String> targetsSSAmountCol; //todo
    //serialSetTableView
    @FXML private TableView<SerialSetTableViewRow> serialSetTableView;
    @FXML private TableColumn<SerialSetTableViewRow, String> serialSetHashCol;
    @FXML private TableColumn<SerialSetTableViewRow, String> serialSetNameCol;
    @FXML private TableColumn<SerialSetTableViewRow, String> serialSetTargetListCol;
    //summary
    @FXML private Label summaryTargetsAmountLabel;
    @FXML private Label summaryIndependentsLabel;
    @FXML private Label summaryLeafLabel;
    @FXML private Label summaryMiddleLabel;
    @FXML private Label summaryRootLabel;
    //
    private ObservableList<CheckBox> checkBoxes= FXCollections.observableArrayList();
    //

    @Override
    public void setMainAppController(MainAppController newMainAppController) {
        this.mainAppController = newMainAppController;
    }

    public void setMainAppControllerTest(Test2Controller newMainAppController) {
        this.mainAppControllerTest = newMainAppController;
    }

    @Override
    public Node getNodeController(){
        return this.nodeController;
    }

    @Override
    public void setNodeController(Node node){
        this.nodeController = node;
    }

    public void initializeBasicInformationController() {
        updateData(mainAppController.getGraphDTOFromDashboard());
    }

    public void updateData(GraphDTO graphDTO){
        List<TargetDTO> allTargets = graphDTO.getAllTargets();

        updateTargetsTableView(allTargets, graphDTO);
        //updateSerialSetTableView(serialSetsDTO);
        updateSummary(graphDTO);
    }

    private void updateTargetsTableView(List<TargetDTO> allTargets, GraphDTO currentGraph){
        List<TargetsTableViewRow> rows = new LinkedList<>();
        final int[] counter = {0};

        allTargets.forEach(row-> {
                rows.add(new TargetsTableViewRow(row, ++counter[0], currentGraph.getAllEffected(row.getTargetName(),"DEPENDS_ON").size(), currentGraph.getAllEffected(row.getTargetName(), "REQUIRED_FOR").size(),false));
                checkBoxes.add(rows.get(rows.size()-1).getCheckBox());
        });

        final ObservableList<TargetsTableViewRow> data = FXCollections.observableArrayList(rows);

        wireColumn(targetsNumberCol,"targetNumber");
        wireColumn(targetsNameCol,"targetName");
        wireColumn(targetsTypeCol,"targetType");
        wireColumn(targetsDirectDepCol,"targetDirectDep");
        wireColumn(targetsTotalDepCol,"targetTotalDep");
        wireColumn(targetsDirectReqCol,"targetDirectReq");
        wireColumn(targetsTotalReqCol,"targetTotalReq");
        wireColumn(targetsDataCol,"targetData");

        targetsTableView.setItems(data);
        targetsTableView.getColumns().clear();

        targetsTableView.getColumns().addAll(targetsNumberCol,targetsNameCol,targetsTypeCol, targetsDirectDepCol, targetsTotalDepCol,targetsDirectReqCol,targetsTotalReqCol ,targetsDataCol);
        //Platform.runLater(() -> targetsTableView.refresh());
    }


    private void updateSerialSetTableView(SerialSetsDTO dto){
        List<SerialSetTableViewRow> rows = new LinkedList<>();
        final int[] counter = {0};
        dto.getSetList().forEach(row->rows.add(new SerialSetTableViewRow(row,++counter[0])));

        //final ObservableList<TargetDTO> data = FXCollections.observableArrayList(allTargets);
        final ObservableList<SerialSetTableViewRow> data = FXCollections.observableArrayList(rows);
        // final ObservableList<TableColumn<TargetsTableViewRow,?>> datass = FXCollections.observableArrayList(targetsTableView.getColumns());

        wireColumn(serialSetHashCol, "serialSetHash" );
        wireColumn(serialSetNameCol, "serialSetName" );
        wireColumn(serialSetTargetListCol, "serialSetTargetList" );

        serialSetTableView.setItems(data);
        serialSetTableView.getColumns().clear();
        serialSetTableView.getColumns().addAll(serialSetHashCol,serialSetNameCol,serialSetTargetListCol);

    }

    private void wireColumn(TableColumn column, String property){
        column.setCellValueFactory(
                new PropertyValueFactory<>(property)
        );
    }

    private void updateSummary(GraphDTO graphDTO){
        summaryTargetsAmountLabel.setText(graphDTO.getAmountOfTargets().toString());
        summaryIndependentsLabel.setText(graphDTO.getIndependentAmount().toString());
        summaryLeafLabel.setText(graphDTO.getLeafAmount().toString());
        summaryMiddleLabel.setText(graphDTO.getMiddleAmount().toString());
        summaryRootLabel.setText(graphDTO.getRootAmount().toString());
    }

    public TableView<TargetsTableViewRow> getTargetTable() {
        return this.targetsTableView;
    }

    public ObservableList<CheckBox> getCheckBoxes() {
        return checkBoxes;
    }
}