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

            if(graphsList.size() > 0) {
                //todo remove when cycle is fixed
                System.out.println("Graph name: " + graphsList.get(0).getGraphName());
                System.out.println("TargetDTO names: " + graphsList.get(0).getAllTargets());
               // System.out.println(graphsList.get(0).getAllTargets().get(0));
               // System.out.println(gson.toJson(graphsList.get(0).getAllTargets().get(0).returnTargetTest()));
            }

            String json = gson.toJson(graphsList);
            out.println(json);
            out.flush();
        }
    }
}
