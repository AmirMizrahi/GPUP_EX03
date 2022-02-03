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

public class UpdateTargetsResultsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        AbstractTask.TASK_STATUS taskStatus = null;
//        //todo Need to check that we didn't receive this file already (isn't available at the engine already - synchronize this action!
//        //todo synchronize?
//
//        Properties prop = new Properties();
//        prop.load(request.getInputStream());
//
//        String taskStatusRaw = prop.getProperty("taskStatus");
//        String taskName = prop.getProperty("taskName");
//
//        String error = null;
//        try {
//            taskStatus = AbstractTask.TASK_STATUS.valueOf(taskStatusRaw.toUpperCase());
//        } catch (IllegalArgumentException nfe) {
//            error = "Error! choose valid task status";
//        }
//
//        if (error != null) {
//            response.getWriter().println(error);
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            response.getOutputStream().print(error);
//
//        } else {
//            Manager manager = ServletUtils.getManager(getServletContext());
//
//            try {
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
