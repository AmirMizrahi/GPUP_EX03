package Utils;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static Utils.Constants.LOAD_XML;

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

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void runAsync(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void uploadFile(File file, Callback callback){
        RequestBody body =
                new MultipartBody.Builder()
                        .addFormDataPart("file1", file.getName(), RequestBody.create(file, MediaType.parse("text/plain")))
                        //.addFormDataPart("key1", "value1") // you can add multiple, different parts as needed
                        .build();

        Request request = new Request.Builder()
                .url(LOAD_XML)
                .post(body)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        try {
            Response response = call.execute();
        } catch (IOException e) {
            System.out.println("byebye");
        }
        //call.enqueue(callback);
    }

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
