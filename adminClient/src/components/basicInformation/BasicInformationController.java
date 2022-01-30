package components.basicInformation;

import DTO.GraphDTO;
import DTO.TargetDTO;
import components.graphMainComponent.GraphMainComponentController;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.LinkedList;
import java.util.List;

public class BasicInformationController implements Controller {

    //Controllers
    private GraphMainComponentController mainAppControllerTest;
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

    public void setMainAppControllerTest(GraphMainComponentController newMainAppController) {
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
        updateData(mainAppController.getSelectedGraphDTOFromDashboard());
    }

    public void updateData(GraphDTO graphDTO){
        List<TargetDTO> allTargets = graphDTO.getAllTargets();

        updateTargetsTableView(allTargets, graphDTO);
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
        Platform.runLater(() -> targetsTableView.refresh());
    }

    private void wireColumn(TableColumn column, String property){
        column.setCellValueFactory(
                new PropertyValueFactory<>(property)
        );
    }

    private void updateSummary(GraphDTO graphDTO){
        //System.out.println(this.mainAppControllerTest.test());
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