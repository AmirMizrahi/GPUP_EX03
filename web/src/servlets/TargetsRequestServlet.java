package servlets;

import DTO.GraphDTO;
import DTO.TaskDTO;
import DTO.TaskDTOForWorker;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.Manager;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class TargetsRequestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            int availableThreads = 0;
            String error = null;

            String usernameFromParameter = request.getParameter("username");
            String availableThreadsFromParameter = request.getParameter("availableThreads");

            try {
                availableThreads = Integer.parseInt(availableThreadsFromParameter);
            } catch (NumberFormatException nfe) {
                error = "Error! one of the arguments is not a number";
            }

            if (error != null) {
                response.getWriter().println(error);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print(error);

            } else {
                Manager manager = ServletUtils.getManager(getServletContext());
                List<TaskDTOForWorker> toReturn = manager.getTargetsForWorker(availableThreads, usernameFromParameter);
                String json = gson.toJson(toReturn);
                out.println(json);
                out.flush();
            }
        }
    }
}