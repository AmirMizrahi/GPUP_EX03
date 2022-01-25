package Utils;

import com.google.gson.Gson;

public class Constants {
    public final static int TARGETS_REFRESH_RATE = 100;
    public final static int DASHBOARD_REFRESH_RATE = 1500;
    public final static String GRAPHVIZ_LOCATION = "JavaFX/src/components/graphviz";
    public final static String XOO_LOCATION = "./engine/src/resources/XOO";
    private final static String SERVER_ADDRESS = "http://localhost:8080/gpupAdmin";
    public final static String LOGIN_ADDRESS = SERVER_ADDRESS + "/loginShortResponse";
    public final static String USERS_LIST = SERVER_ADDRESS + "/userslist";
    // GSON instance
    public final static Gson GSON_INSTANCE = new Gson();


}
