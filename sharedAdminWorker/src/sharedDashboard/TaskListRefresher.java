package sharedDashboard;

import DTO.TaskDTO;
import Utils.Constants;
import Utils.HttpClientUtil;
import com.google.gson.reflect.TypeToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import static Utils.Constants.GSON_INSTANCE;

public class TaskListRefresher extends TimerTask {
    private final Consumer<List<TaskDTO>> tasksListConsumer;

    public TaskListRefresher(Consumer<List<TaskDTO>> graphsListConsumer) {
        this.tasksListConsumer = graphsListConsumer;
    }

    @Override
    public void run() {
        HttpClientUtil.runAsync(Constants.TASK, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                List<TaskDTO> failed = new LinkedList<>();
                tasksListConsumer.accept(failed);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                synchronized (this) {
                String jsonArrayOfUsersNames = response.body().string();
                //httpRequestLoggerConsumer.accept("Users Request # " + finalRequestNumber + " | Response: " + jsonArrayOfUsersNames);
                Type type = new TypeToken<List<TaskDTO>>(){}.getType();
                List<TaskDTO> tasksDTOS = GSON_INSTANCE.fromJson(jsonArrayOfUsersNames, type);
                //Map<String,String> usersNames = GSON_INSTANCE2.fromJson(jsonArrayOfUsersNames, Map.class);
                response.close();
                tasksListConsumer.accept(tasksDTOS);
                }

            }
        });
    }
}
