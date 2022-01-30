package servlets;

import DTO.GraphDTO;
import DTO.TaskDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import managers.Manager;
import utils.ServletUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class UploadTaskServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //todo Need to check that we didn't receive this file already (isn't available at the engine already - synchronize this action!
        //todo synchronize?

        Properties prop = new Properties();
        prop.load(request.getInputStream());

        String timeRaw = prop.getProperty("time");
        String time_option = prop.getProperty("time_option");
        String successRatesRaw = prop.getProperty("successRates");
        String warningRatesRaw = prop.getProperty("warningRates");
        String userName = prop.getProperty("userName");
        String graphName = prop.getProperty("graphName");
        String targetsRaw = prop.getProperty("targets");
        String taskName = prop.getProperty("taskName");

        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> targets = new Gson().fromJson(targetsRaw, listType);

        Integer time = 0;
        Double successRates = 0.0, warningRates = 0.0;
        String error = null;
        try {
            time = Integer.parseInt(timeRaw);
            successRates = Double.parseDouble(successRatesRaw);
            warningRates = Double.parseDouble(warningRatesRaw);
        } catch (NumberFormatException nfe) {
            error = "Error! one of the arguments is not a number";
        }

        if (error != null) {
            response.getWriter().println(error);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().print(error);

        } else {
            Manager manager = ServletUtils.getManager(getServletContext());
            // if taskType == Simulation
            try{
                manager.addNewTask(time,time_option,successRates,warningRates,userName, graphName, taskName, targets);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().print("Task " + taskName + " Created Successfully!");
            }
            catch (Exception e){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print(e.getMessage());
            }

        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            Manager manager = ServletUtils.getManager(getServletContext());
            List<TaskDTO> tasksList = manager.getAllTasks();
            String json = gson.toJson(tasksList);
            out.println(json);
            out.flush();
        }
    }
}
