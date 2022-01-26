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
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        Collection<Part> parts = request.getParts();
        for (Part part : parts) {
            int content;
            InputStream fileAsStream = part.getInputStream();
            while ((content = fileAsStream.read()) != -1) {
                System.out.print((char)content);
            }
        }

        Part p = parts.iterator().next();
        System.out.println("111");
        Manager manager = ServletUtils.getManager(getServletContext());
        try {
            System.out.println("222");
            manager.loadXMLFileMG(p.getInputStream());
            System.out.println("333");
        } catch (TargetNotFoundException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (XMLException e) {
            e.printStackTrace();
        }

        //int size = fileAsStream.available();
        //byte[] bucket = new byte[size];

        //fileAsStream.read(bucket);

//        try (FileOutputStream outputStream = new FileOutputStream( "c:\\temp\\output.xml")) {
          //  outputStream.write(bucket);
    }
}
