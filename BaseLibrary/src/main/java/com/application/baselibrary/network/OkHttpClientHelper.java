package com.application.baselibrary.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OkHttpClientManager
 * -------------------
 * A lightweight wrapper around OkHttpClient providing async/sync GET, POST, and file upload.
 * Results are delivered via callbacks on the main/UI thread.
 */
public class OkHttpClientHelper {

    private static final String TAG = OkHttpClientHelper.class.getSimpleName();
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public OkHttpClientHelper() {
        this(new OkHttpClient(), Executors.newFixedThreadPool(4));
    }

    public OkHttpClientHelper(OkHttpClient client, ExecutorService executor) {
        this.client = client != null ? client : new OkHttpClient();
        this.executor = executor != null ? executor : Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // --- GET Requests ---

    public void getText(String url, HttpCallback callback) {
        executor.submit(() -> {
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                handleResponse(response, callback);
            } catch (IOException e) {
                postError(callback, "GET error: " + e.getMessage());
            }
        });
    }

    public String getTextSync(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
            throw new IOException("GET failed: " + response.code());
        }
    }

    // --- POST Requests ---

    public void postJson(String url, String json, HttpCallback callback) {
        executor.submit(() -> {
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder().url(url).post(body).build();
            try (Response response = client.newCall(request).execute()) {
                handleResponse(response, callback);
            } catch (IOException e) {
                postError(callback, "POST error: " + e.getMessage());
            }
        });
    }

    public void uploadFile(String url, File file, String mediaType, HttpCallback callback) {
        uploadFileWithParams(url, file, mediaType, null, callback);
    }

    public void uploadFileWithParams(String url, File file, String mediaType,
                                     Map<String, String> params, HttpCallback callback) {
        executor.submit(() -> {
            try {
                // Correct RequestBody signature in OkHttp 4.x
                RequestBody fileBody = RequestBody.create(
                        MediaType.parse(mediaType != null ? mediaType : "application/octet-stream"),
                        file
                );

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(), fileBody);

                // Add payload_json or content safely
                if (params != null) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        builder.addFormDataPart(entry.getKey(), entry.getValue());
                    }
                }

                RequestBody requestBody = builder.build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    handleResponse(response, callback);
                }
            } catch (IOException e) {
                postError(callback, "Upload error: " + e.getMessage());
            }
        });
    }

    public void enqueueRequest(Request request, HttpCallback callback) {
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (callback != null) {
                    callback.onSuccess(new ResponseResult(
                            response.isSuccessful(),
                            response.body() != null ? response.body().string() : "",
                            response.code()
                    ));
                }
            }
        });
    }



    // --- Shutdown ---

    public void shutdown() {
        executor.shutdown();
    }

    // --- Internal Helpers ---

    private void handleResponse(Response response, HttpCallback callback) throws IOException {
        if (response.isSuccessful() && response.body() != null) {
            postSuccess(callback, new ResponseResult(true, response.body().string(), response.code()));
        } else {
            postError(callback, "Request failed: " + response.code());
        }
    }

    private void postSuccess(HttpCallback callback, ResponseResult result) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    private void postError(HttpCallback callback, String errorMessage) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(errorMessage));
        }
    }

    // --- Callback & Result Types ---

    public interface HttpCallback {
        void onSuccess(ResponseResult result);
        void onError(String errorMessage);
    }

    public static class ResponseResult {
        public final boolean success;
        public final String body;
        public final int code;

        public ResponseResult(boolean success, String body, int code) {
            this.success = success;
            this.body = body;
            this.code = code;
        }
    }
}
