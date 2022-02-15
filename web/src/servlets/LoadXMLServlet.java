package servlets;

import exceptions.TargetNotFoundException;
import exceptions.XMLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import managers.Manager;
import utils.ServletUtils;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.Collection;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class LoadXMLServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //todo Need to check that we didn't receive this file already (isn't available at the engine already - synchronize this action!
        //todo synchronize?

        response.setContentType("text/plain");

        Collection<Part> parts = request.getParts();
        //check if file exits?

        if (!parts.isEmpty()) { // File received from client
            Part p = parts.iterator().next(); //get the file
            Manager manager = ServletUtils.getManager(getServletContext());
            try {
                manager.loadXMLFileMG(p.getInputStream(), request.getParameter("userName"));
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().print("Loaded file successfully!");
            } catch (TargetNotFoundException | JAXBException | XMLException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print(e.getMessage());
            }

         }
        else{ //Didn't get file from client
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().print("XML not loaded - Didn't get file to server.");
        }
    }
}
