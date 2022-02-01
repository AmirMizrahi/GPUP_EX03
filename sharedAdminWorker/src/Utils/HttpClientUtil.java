package Utils;

import javafx.scene.control.Alert;
import okhttp3.*;

import java.io.File;
import java.util.function.Consumer;

import static Utils.Constants.*;

public class HttpClientUtil {

    private final static SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        simpleCookieManager.setLogData(logConsumer);
    }

    public static Alert createErrorPopup(String header, String content){
        Alert failLoginPopup = new Alert(Alert.AlertType.ERROR);
        failLoginPopup.setHeaderText(header);
        failLoginPopup.setContentText("Something went wrong: " + content);
        return failLoginPopup;
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void runAsync(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    public static void uploadFile(File file ,String userName, Callback callback){
        RequestBody body =
                new MultipartBody.Builder()
                        .addFormDataPart("file1", file.getName(), RequestBody.create(file, MediaType.parse("text/plain")))
                        .build();

        Request request = new Request.Builder()
                .url(LOAD_XML + userName)
                .post(body)
                .build();

        Call call = HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    public static void postRequest(RequestBody body, Callback callback, String url){
        Request request = new Request.Builder()
                .url(url)
                // .addHeader("Content-Type", "text/plain")
                .post(body)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
