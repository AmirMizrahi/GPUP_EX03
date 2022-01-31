package servlets;

import DTO.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.Manager;
import tasks.AbstractTask;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Properties;

public class UpdateTaskSubscriberServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //todo Need to check that we didn't receive this file already (isn't available at the engine already - synchronize this action!
        //todo synchronize?

        Properties prop = new Properties();
        prop.load(request.getInputStream());

        String taskName = prop.getProperty("taskName");
        String userName = prop.getProperty("userName");

        Manager manager = ServletUtils.getManager(getServletContext());

        try {
            UserDTO userDTO = ServletUtils.getUserManager(getServletContext()).getUsers().get(userName);
            manager.updateTaskSubscriber(taskName, userDTO);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().print("User: " + userName + " added as a subscriber to task " + taskName + " Successfully");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().print(e.getMessage());
        }
    }
}