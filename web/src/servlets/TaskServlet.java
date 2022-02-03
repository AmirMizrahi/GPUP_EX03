package servlets;

import DTO.TaskDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.Manager;
import utils.ServletUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;

public class TaskServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        String taskType = prop.getProperty("taskType");
        String source = prop.getProperty("source");
        String destination = prop.getProperty("destination");

        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> targets = new Gson().fromJson(targetsRaw, listType);

        Integer time = 0;
        Double successRates = 0.0, warningRates = 0.0;
        String error = null;
        try {
            if(taskType.compareToIgnoreCase("simulation") == 0){
                time = Integer.parseInt(timeRaw);
                successRates = Double.parseDouble(successRatesRaw);
                warningRates = Double.parseDouble(warningRatesRaw);
            }
        } catch (NumberFormatException nfe) {
            error = "Error! one of the arguments is not a number";
        }

        if (error != null) {
            response.getWriter().println(error);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //response.getOutputStream().print(error);

        } else {
            Manager manager = ServletUtils.getManager(getServletContext());
            try{
                if(taskType.compareToIgnoreCase("simulation") == 0)
                    manager.addNewTask(time,time_option,successRates,warningRates,userName, graphName, taskName, targets);
                else
                    manager.addNewTask(source, destination ,userName, graphName, taskName, targets);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().print("Task " + taskName + " Created Successfully!");
            }
            catch (Exception e){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print("123"+e.getMessage());
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
