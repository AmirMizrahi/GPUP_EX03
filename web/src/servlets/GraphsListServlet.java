package servlets;

import DTO.GraphDTO;
import DTO.TargetDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import graph.Graph;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.Manager;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class GraphsListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            Manager manager = ServletUtils.getManager(getServletContext());
            List<GraphDTO> graphsList = manager.getAllGraphs();
            String json = gson.toJson(graphsList);
            out.println(json);
            out.flush();
        }
    }
}
