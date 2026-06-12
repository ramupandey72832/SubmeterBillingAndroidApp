package com.application.baselibrary.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.application.baselibrary.notification.model.NotificationData;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class AppNotificationListener extends NotificationListenerService {

    private static final String TAG = "AppNotificationListener";
    private static NotificationLogManager logManager;
    private static final Map<String, String> lastNotificationMap = new HashMap<>();
    // Register lifecycle manager
    public static void setLogManager(NotificationLogManager manager) {
        logManager = manager;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        Log.i(TAG, "Received Package Notification: " + sbn.getPackageName());

        String packageName = sbn.getPackageName();
        if (packageName.equals(getPackageName())) return; // ignore own app
        if (packageName.equals("android") || packageName.equals("com.android.systemui"))
            return; // ignore system

        CharSequence titleCs = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence textCs = sbn.getNotification().extras.getCharSequence("android.text");

        String title = titleCs != null ? titleCs.toString() : "";
        String text = textCs != null ? textCs.toString() : "";

        // 3. Filter: Deduplication (Ignore if the same app sends the same message twice in a row)
        String currentContent = title + "|" + text;
        if (currentContent.equals(lastNotificationMap.get(packageName))) {
            return;
        }
        lastNotificationMap.put(packageName, currentContent);

        NotificationData data = new NotificationData(
                packageName,
                title,
                text,
                System.currentTimeMillis(),
                sbn.getNotification().category != null ? sbn.getNotification().category : "None"
        );

        if (logManager != null) {
            Log.i(TAG, "Received new notification: " + data.getText());
            logManager.onNewNotification(data);
        } else {
            Log.w(TAG, "No log manager set. Dropping notification.");
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Optional: clear deduplication entry when notification is cleared
        if (sbn != null) {
            lastNotificationMap.remove(sbn.getPackageName());
        }
    }

    @Override
    public void onListenerConnected() {
        Log.i(TAG, "Notification listener connected");
    }

    @Override
    public void onListenerDisconnected() {
        Log.i(TAG, "Notification listener disconnected");
    }
}
