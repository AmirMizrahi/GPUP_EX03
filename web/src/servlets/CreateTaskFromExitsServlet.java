package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.Manager;
import tasks.AbstractTask;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Properties;

public class CreateTaskFromExitsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Properties prop = new Properties();
        prop.load(request.getInputStream());

        AbstractTask.WAYS_TO_START_SIM_TASK wayToCreateFrom = null;
        String taskName = prop.getProperty("taskName");
        String wayToCreateFromRaw = prop.getProperty("wayToCreateFrom");
        String userName = prop.getProperty("userName");

        String error = null;
        try {
            wayToCreateFrom = AbstractTask.WAYS_TO_START_SIM_TASK.valueOf(wayToCreateFromRaw.toUpperCase());
        } catch (IllegalArgumentException nfe) {
            error = "Error! choose valid task status";
        }

        if (error != null) {
            response.getWriter().println(error);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().print(error);

        } else {
            Manager manager = ServletUtils.getManager(getServletContext());

            try {
                manager.addTaskFromExits(taskName, wayToCreateFrom, userName);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().print("New task was created from " + taskName + " Successfully");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print(e.getMessage());
            }
        }
    }
}