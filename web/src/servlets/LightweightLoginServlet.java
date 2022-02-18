package servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;
import java.io.IOException;

public class LightweightLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String usernameFromSession = SessionUtils.getUsername(request);
        String errorMessage;
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (usernameFromSession == null) { //user is not logged in yet

            String usernameFromParameter = request.getParameter("username");
            String userTypeFromParameter = request.getParameter("type");
            String threadsFromParameter = request.getParameter("threads");

            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                //no username in session, no username in parameter or bad char were inserted - not standard situation. it's a conflict
                // stands for conflict in server state
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                errorMessage = "Username can't be empty!'";
                response.getOutputStream().print(errorMessage);
            }
            else {
                //normalize the username value
                usernameFromParameter = usernameFromParameter.trim();

                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        errorMessage = "Username " + usernameFromParameter + " already exists. Please enter a different username.";

                        // stands for unauthorized as there is already such user with this name
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getOutputStream().print(errorMessage);
                    }
                    else if(userTypeFromParameter.compareToIgnoreCase("Worker") != 0 &&
                            userTypeFromParameter.compareToIgnoreCase("Admin") != 0){
                        errorMessage = "Authorized type are 'Admin' or 'Worker' only!";

                        // stands for unauthorized user type
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getOutputStream().print(errorMessage);
                    }

                    else {
                        //add the new user to the users list
                        if(userTypeFromParameter.compareToIgnoreCase("Worker") == 0) {
                            try {
                                int threads = Integer.parseInt(threadsFromParameter);
                                if (threads > 5 || threads < 1) {
                                    errorMessage = "Worker threads should be between 1-5!";
                                    // stands for unauthorized user type
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.getOutputStream().print(errorMessage);
                                    return;
                                }
                                else
                                    userManager.addUser(usernameFromParameter, userTypeFromParameter, threads);

                            } catch (NumberFormatException e) {
                                errorMessage = "Threads can be integers only.";
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getOutputStream().print(errorMessage);
                                return;
                            }
                        }

                        else if (userTypeFromParameter.compareToIgnoreCase("Admin") == 0)
                            userManager.addUser(usernameFromParameter, userTypeFromParameter);

                        //set the username in a session so it will be available on each request
                        //the true parameter means that if a session object does not exists yet
                        //create a new one
                        request.getSession(true).setAttribute("username", usernameFromParameter);

                        //redirect the request to the chat room - in order to actually change the URL
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getOutputStream().print("Logged in successfully as " + usernameFromParameter +"!");
                    }
                }
            }
        } else {
            //user is already logged in
            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().print("User is already logged in as " + usernameFromSession +".\nPlease logout first.");
        }
    }
}