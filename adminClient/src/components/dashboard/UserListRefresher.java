package components.dashboard;

import DTO.GraphDTO;
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
import java.util.*;
import java.util.function.Consumer;

import static Utils.Constants.GSON_INSTANCE;

//upload
//timer
//servlets

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
                Map<String,String> failed = new HashMap<>();
                failed.put("","");
                usersListConsumer.accept(failed);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonArrayOfUsersNames = response.body().string();
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> usersNames = GSON_INSTANCE.fromJson(jsonArrayOfUsersNames, type);
                usersListConsumer.accept(usersNames);
            }
        });
    }
}