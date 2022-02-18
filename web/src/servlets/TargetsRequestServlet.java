package servlets;

import DTO.TargetDTOForWorker;
import DTO.UserDTO;
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
            String availableThreadsFromParameter = request.getParameter("numberOfTargets");

            try {
                availableThreads = Integer.parseInt(availableThreadsFromParameter);
                if(availableThreads > 5 || availableThreads < 0)
                    error = "Error! Please insert threads between 0 to 5.";
            } catch (NumberFormatException nfe) {
                error = "Error! one of the arguments is not a number.";
            }

            UserDTO userDTO = ServletUtils.getUserManager(getServletContext()).getUsers().get(usernameFromParameter);
            if(userDTO == null)
                error = "Error! Username isn't exits.";
            else if(availableThreads > userDTO.getThreadsAmount())
                error = "Error! Number of targets can be greater than the number you registered with.";

            if (error != null) {
                response.getWriter().println(error);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print(error);

            } else {
                Manager manager = ServletUtils.getManager(getServletContext());
                List<TargetDTOForWorker> toReturn = manager.getTargetsForWorker(availableThreads, usernameFromParameter);
                String json = gson.toJson(toReturn);
                out.println(json);
                out.flush();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}