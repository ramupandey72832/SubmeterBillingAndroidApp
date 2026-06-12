package com.application.baselibrary.network;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.application.baselibrary.threading.scheduler.RxSchedulerProvider;
import com.application.baselibrary.utils.file.FileSplitter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * DiscordWebhookClient
 * --------------------
 * A utility class to interact with Discord Webhooks.
 * Supports sending plain text messages, embeds, and uploading files with automatic chunking for files > 5MB.
 */
public class DiscordWebhookClient {

    private static final long MAX_PART_SIZE = 5 * 1024 * 1024; // 5MB
    private final String webhookUrl;
    private final OkHttpClientHelper httpClient;

    public DiscordWebhookClient(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = new OkHttpClientHelper();
    }

    // --- Messaging ---

    public void sendMessage(String content, WebhookCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("content", content);
        httpClient.postJson(webhookUrl, json.toString(), wrapCallback(callback));
    }

    public void sendMessage(String content) {
        sendMessage(content, null);
    }

    public void sendEmbed(Embed embed, WebhookCallback callback) {
        JsonObject payload = new JsonObject();
        JsonArray embeds = new JsonArray();
        embeds.add(embed.toJson());
        payload.add("embeds", embeds);
        httpClient.postJson(webhookUrl, payload.toString(), wrapCallback(callback));
    }

    public void sendEmbed(Embed embed) {
        sendEmbed(embed, null);
    }

    // --- File Uploads ---

    public void uploadFile(File file, String message, WebhookCallback callback) {
        if (file.length() <= MAX_PART_SIZE) {
            uploadFileSingle(file, message, callback);
        } else {
            RxSchedulerProvider.runOnIO(() -> {
                try {
                    List<File> parts = FileSplitter.splitFile(file, MAX_PART_SIZE);
                    int totalParts = parts.size();
                    long totalSizeMb = file.length() / (1024 * 1024);

                    for (int i = 0; i < totalParts; i++) {
                        File part = parts.get(i);
                        String partHeader = String.format("📦 File Split (%dMB)\nPart %d of %d",
                                totalSizeMb, (i + 1), totalParts);
                        String partMessage = (message != null ? message + "\n" : "") + partHeader;

                        uploadFileSingle(part, partMessage, new WebhookCallback() {
                            @Override
                            public void onSuccess(WebhookResponse response) {
                                part.delete();
                                if (callback != null) callback.onSuccess(response);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                part.delete();
                                if (callback != null) callback.onError(errorMessage);
                            }
                        });
                    }
                } catch (IOException e) {
                    if (callback != null) {
                        RxSchedulerProvider.runOnMain(() ->
                                callback.onError("Failed to split file: " + e.getMessage()));
                    }
                }
            });
        }
    }


    /**
     * Uploads a file from an InputStream (for content:// URIs).
     */
    public void uploadStream(InputStream stream, String fileName, String message, WebhookCallback callback) {
        try {
            // Wrap InputStream into RequestBody
            RequestBody fileBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse("application/octet-stream");
                }

                @Override
                public void writeTo(okio.BufferedSink sink) throws IOException {
                    try {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = stream.read(buffer)) != -1) {
                            sink.write(buffer, 0, read);
                        }
                    } finally {
                        stream.close(); // close only after OkHttp finishes consuming
                    }
                }
            };

            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, fileBody);

            if (message != null) {
                builder.addFormDataPart("content", message);
            }

            Request request = new Request.Builder()
                    .url(webhookUrl)
                    .post(builder.build())
                    .build();

            httpClient.enqueueRequest(request, wrapCallback(callback));

        } catch (Exception e) {
            if (callback != null) {
                callback.onError("Failed to upload stream: " + e.getMessage());
            }
        }
    }

    public void uploadStreamWithParams(String url,
                                       InputStream stream,
                                       String fileName,
                                       String mimeType,
                                       Map<String, String> params,
                                       WebhookCallback callback) {
        RequestBody fileBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(mimeType);
            }

            @Override
            public void writeTo(okio.BufferedSink sink) throws IOException {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = stream.read(buffer)) != -1) {
                    sink.write(buffer, 0, read);
                }
            }
        };

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();

        // Instead of executeRequest, call your existing OkHttpClientHelper internal method
        httpClient.enqueueRequest(request, wrapCallback(callback)); // <-- you’ll need to expose this
    }



    public void uploadFile(File file, String message) {
        uploadFile(file, message, null);
    }

    public void uploadFileWithEmbed(File file, String title, String description,
                                    int color, WebhookCallback callback) {
        JsonObject embedJson = new JsonObject();
        embedJson.addProperty("title", title);
        embedJson.addProperty("description", description);
        embedJson.addProperty("color", color);

        JsonObject image = new JsonObject();
        image.addProperty("url", "attachment://" + file.getName());
        embedJson.add("image", image);

        JsonArray embeds = new JsonArray();
        embeds.add(embedJson);

        JsonObject payload = new JsonObject();
        payload.add("embeds", embeds);

        Map<String, String> params = new HashMap<>();
        params.put("payload_json", payload.toString());

        httpClient.uploadFileWithParams(webhookUrl, file, "application/octet-stream", params, wrapCallback(callback));
    }

    // --- Internal Helpers ---

    private void uploadFileSingle(File file, String message, WebhookCallback callback) {
        Map<String, String> params = new HashMap<>();
        if (message != null) params.put("content", message);
        httpClient.uploadFileWithParams(webhookUrl, file, "application/octet-stream", params, wrapCallback(callback));
    }

    private OkHttpClientHelper.HttpCallback wrapCallback(WebhookCallback callback) {
        if (callback == null) return null;
        return new OkHttpClientHelper.HttpCallback() {
            @Override
            public void onSuccess(OkHttpClientHelper.ResponseResult result) {
                callback.onSuccess(new WebhookResponse(result.success, result.body, result.code));
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        };
    }

    // --- Embed Builder ---

    public static class Embed {
        private String title;
        private String description;
        private Integer color;
        private final JsonArray fields = new JsonArray();
        private String footerText;
        private String imageUrl;
        private String thumbnailUrl;
        private String url;

        public Embed setTitle(String title) { this.title = title; return this; }
        public Embed setDescription(String description) { this.description = description; return this; }
        public Embed setColor(int color) { this.color = color; return this; }
        public Embed setFooter(String text) { this.footerText = text; return this; }
        public Embed setImageUrl(String url) { this.imageUrl = url; return this; }
        public Embed setThumbnailUrl(String url) { this.thumbnailUrl = url; return this; }
        public Embed setUrl(String url) { this.url = url; return this; }

        public Embed addField(String name, String value, boolean inline) {
            JsonObject field = new JsonObject();
            field.addProperty("name", name);
            field.addProperty("value", value);
            field.addProperty("inline", inline);
            fields.add(field);
            return this;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            if (title != null) json.addProperty("title", title);
            if (description != null) json.addProperty("description", description);
            if (color != null) json.addProperty("color", color);
            if (url != null) json.addProperty("url", url);
            if (fields.size() > 0) json.add("fields", fields);
            if (footerText != null) {
                JsonObject footer = new JsonObject();
                footer.addProperty("text", footerText);
                json.add("footer", footer);
            }
            if (imageUrl != null) {
                JsonObject image = new JsonObject();
                image.addProperty("url", imageUrl);
                json.add("image", image);
            }
            if (thumbnailUrl != null) {
                JsonObject thumb = new JsonObject();
                thumb.addProperty("url", thumbnailUrl);
                json.add("thumbnail", thumb);
            }
            return json;
        }
    }

    // --- Callback & Response Types ---

    public interface WebhookCallback {
        void onSuccess(WebhookResponse response);
        void onError(String errorMessage);
    }

    public static class WebhookResponse {
        public final boolean success;
        public final String body;
        public final int code;

        public WebhookResponse(boolean success, String body, int code) {
            this.success = success;
            this.body = body;
            this.code = code;
        }
    }
}
