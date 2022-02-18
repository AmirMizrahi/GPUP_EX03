package servlets;

import DTO.TaskDTO;
import DTO.UserDTO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.Manager;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class UpdateTaskSubscriberServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //todo Need to check that we didn't receive this file already (isn't available at the engine already - synchronize this action!
        //todo synchronize?

        response.setContentType("application/json");
        Properties prop = new Properties();
        prop.load(request.getInputStream());

        String taskName = prop.getProperty("taskName");
        String userName = prop.getProperty("userName");

        Manager manager = ServletUtils.getManager(getServletContext());

        try {
            UserDTO userDTO = ServletUtils.getUserManager(getServletContext()).getUsers().get(userName);
            if(userDTO == null)
                throw new Exception("Username not exits!");
            manager.updateTaskSubscriber(taskName, userDTO);
            TaskDTO taskDTO = null;
            Gson gson = new Gson();
            for (TaskDTO task : manager.getAllTasks()) {
                if(task.getTaskName().compareTo(taskName) == 0)
                    taskDTO = task;
            }
            if(taskDTO == null)
                throw new Exception("Task not exits!");
            response.setStatus(HttpServletResponse.SC_OK);
           // response.getOutputStream().print("User " + userName + " added as a subscriber to task " + taskName + " Successfully");
            PrintWriter out = response.getWriter();
            out.println("User " + userName + " added as a subscriber to task " + taskName + " Successfully!\n" + gson.toJson(taskDTO));
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().print(e.getMessage());
        }
    }
}