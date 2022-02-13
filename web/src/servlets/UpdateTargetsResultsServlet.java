package servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import exceptions.TargetNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.Manager;
import tasks.AbstractTask;
import utils.ServletUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UpdateTargetsResultsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //todo Need to check that we didn't receive this file already (isn't available at the engine already - synchronize this action!
        //todo synchronize?

        Properties prop = new Properties();
        prop.load(request.getInputStream());

        String resultsRaw = prop.getProperty("results");

        Type listType = new TypeToken<List<Map<String,String>>>() {}.getType();
        List<Map<String,String>> results = new Gson().fromJson(resultsRaw, listType);
        Manager manager = ServletUtils.getManager(getServletContext());

        for (Map<String, String> result : results) {
            String targetName = result.get("targetName");
            String taskName = result.get("taskName");
            String targetStatus = result.get("status");
            String totalTime = result.get("totalTime");
            if (targetName == null || taskName == null || targetStatus == null || totalTime == null) {
                response.getWriter().println("Missing info");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print("Missing info");
                throw new NullPointerException("Missing info");
            }
            try {
                manager.updateTargetStatusAfterTask(targetName, taskName, targetStatus, totalTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                manager.updateTargetsByTargetResult(targetName, taskName, targetStatus);
            } catch (TargetNotFoundException e) {
                e.printStackTrace();
            }
            response.getOutputStream().print("Target " + targetName + ":"+ taskName + " Changed Status to " + targetStatus);
        }

        response.setStatus(HttpServletResponse.SC_OK);


//        } else {
//            Manager manager = ServletUtils.getManager(getServletContext());
//
//            try {
//
//                manager.updateTaskStatus(taskName, taskStatus);
//                response.setStatus(HttpServletResponse.SC_OK);
//                response.getOutputStream().print("Task " + taskName + " Changed Status to " + taskStatusRaw + " Successfully");
//            } catch (Exception e) {
//                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                response.getOutputStream().print(e.getMessage());
//            }
//        }
    }
}
