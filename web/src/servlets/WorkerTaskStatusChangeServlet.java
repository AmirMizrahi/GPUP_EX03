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

public class WorkerTaskStatusChangeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //todo synchronize?

        Properties prop = new Properties();
        prop.load(request.getInputStream());

        String taskStatus = prop.getProperty("status");
        String userName = prop.getProperty("userName");
        String taskName = prop.getProperty("taskName");


         Manager manager = ServletUtils.getManager(getServletContext());

         try {
             if(taskStatus.compareTo("unregister") == 0)
                manager.removeSubscriber(userName, taskName);
             else if (taskStatus.compareTo("Pause") == 0)
                 manager.updatePauseFromWorker(userName, taskName, true);
             else if (taskStatus.compareTo("Resume") == 0)
                 manager.updatePauseFromWorker(userName, taskName, false);
             response.setStatus(HttpServletResponse.SC_OK);
             response.getOutputStream().print("Subscriber "+ userName + "is now " + taskStatus + "ed from task " + taskName + " Successfully");
         } catch (Exception e) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getOutputStream().print(e.getMessage());
         }

    }
}
