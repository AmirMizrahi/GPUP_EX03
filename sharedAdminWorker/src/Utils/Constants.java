package Utils;

import com.google.gson.Gson;

public class Constants {
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static int TARGETS_REFRESH_RATE = 100;
    public final static int DASHBOARD_REFRESH_RATE = 1500;
    public final static String GRAPHVIZ_LOCATION = "JavaFX/src/components/graphviz";
    public final static String XOO_LOCATION = "./engine/src/resources/XOO";
    private final static String SERVER_ADDRESS = "http://localhost:8080/gpupAdmin";
    private final static String ENGINE_ADDRESS = "/engine";
    public final static String LOGIN_ADDRESS = SERVER_ADDRESS + "/loginShortResponse";
    public final static String USERS_LIST = SERVER_ADDRESS + "/userslist";
    public final static String GRAPHS_LIST = SERVER_ADDRESS + "/graphslist";
    public final static String LOAD_XML = SERVER_ADDRESS + ENGINE_ADDRESS + "/loadxml?userName=";
    public final static String UPLOAD_TASK = SERVER_ADDRESS + ENGINE_ADDRESS + "/uploadtask";
    public final static String UPDATE_TASK_STATUS = SERVER_ADDRESS + ENGINE_ADDRESS + "/updatetaskstatus";
    public final static String UPDATE_TASK_SUBSCRIBER = SERVER_ADDRESS + ENGINE_ADDRESS + "/updatetasksubscriber";
    // GSON instance
    public final static Gson GSON_INSTANCE = new Gson();


}
