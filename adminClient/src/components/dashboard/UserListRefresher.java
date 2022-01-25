package components.dashboard;

import Utils.Constants;
import Utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.function.Consumer;

import static Utils.Constants.GSON_INSTANCE;

public class UserListRefresher extends TimerTask {

    private final Consumer<Map<String,String>> usersListConsumer;
    private int requestNumber;


    public UserListRefresher(Consumer<Map<String,String>> usersListConsumer) {
        this.usersListConsumer = usersListConsumer;
        requestNumber = 0;
    }

    @Override
    public void run() {
        final int finalRequestNumber = ++requestNumber;
        //httpRequestLoggerConsumer.accept("About to invoke: " + Constants.USERS_LIST + " | Users Request # " + finalRequestNumber);
        HttpClientUtil.runAsync(Constants.USERS_LIST, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Ended with failure...");

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonArrayOfUsersNames = response.body().string();
                //httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Response: " + jsonArrayOfUsersNames);
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> usersNames = GSON_INSTANCE.fromJson(jsonArrayOfUsersNames, type);
                //Map<String,String> usersNames = GSON_INSTANCE2.fromJson(jsonArrayOfUsersNames, Map.class);
                usersListConsumer.accept(usersNames);
            }
        });
    }
}
