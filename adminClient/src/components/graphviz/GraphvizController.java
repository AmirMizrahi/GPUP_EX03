package components.graphviz;

import DTO.GraphDTO;
import DTO.TargetDTO;
import components.mainApp.Controller;
import components.mainApp.MainAppController;
import exceptions.XMLException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;

public class GraphvizController implements Controller {

    //Controllers
    private MainAppController mainAppController;
    private Node nodeController;
    //
    @FXML private ImageView myImage;
    @FXML private TextField dotFileTextField;
    @FXML private TextField pngFileTextField;

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

    public void initializeGraphvizController(String fileName) throws XMLException, IOException {
        GraphDTO graph = this.mainAppController.getSelectedGraphDTOFromDashboard();

        if(fileName.endsWith(".png") || fileName.endsWith(".viz"))
           fileName = fileName.substring(0,fileName.length()-4);
        String to = fileName + ".viz";
        File file = new File(to);
        if (file.exists()) {
            file.delete(); //you might want to check if delete was successful
        }
        file.createNewFile();

        FileWriter myWriter = new FileWriter(file.getAbsolutePath(), true);
        myWriter.write("digraph Graphviz {"+"\n");

        for (TargetDTO current : graph.getAllTargets()) {
            for (String neighborTarget : current.getOutList()) {
                myWriter.write(current.getTargetName()+" -> "+ neighborTarget+"\n");
            }
        }
        myWriter.write("}");
        myWriter.close();

        String[] command = {"dot", "-Tpng", file.getAbsolutePath(), "-o", fileName + ".png"};
        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(false);
        Process p = processBuilder.start();
        try {
            p.waitFor();
        } catch (InterruptedException ignored) {}

        File img = new File(fileName + ".png");
        InputStream isImage = (InputStream) new FileInputStream(img);

        Platform.runLater(()->{
          myImage.setImage(new Image(isImage));
          this.dotFileTextField.setText(file.getAbsolutePath());
          this.pngFileTextField.setText(img.getAbsolutePath());
        });
    }
}
