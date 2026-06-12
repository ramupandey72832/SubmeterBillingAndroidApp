package com.application.baselibrary.config;

import android.content.Context;
import android.util.Log;

import com.application.baselibrary.config.model.deviceconfigmanifest.DeviceConfigManifest;
import com.application.baselibrary.config.model.webhookdevicefilter.WebhookDeviceFilter;
import com.application.baselibrary.data.LibraryConstants;
import com.application.baselibrary.network.OkHttpClientHelper;
import com.application.baselibrary.utils.CallerInspector;
import com.google.gson.Gson;

public class ConfigUpdater {

    public static final String TAG = "ConfigUpdater";
    private final OkHttpClientHelper httpClient;
    private final ConfigStorage storage;
    private ConfigUpdateListener updateListener;
    private static final String MANIFEST_URL = LibraryConstants.DeviceConfigManifest_URL;

    public ConfigUpdater(Context context) {
        this.httpClient = new OkHttpClientHelper();
        this.storage = new ConfigStorage(context);
    }

    /**
     * Allow caller to register a listener
     */
    public void setUpdateListener(ConfigUpdateListener listener) {
        this.updateListener = listener;
    }

    public void checkAndUpdateConfig() {
        httpClient.getText(MANIFEST_URL, new OkHttpClientHelper.HttpCallback() {
            @Override
            public void onSuccess(OkHttpClientHelper.ResponseResult result) {
                String manifestJson = result.body;
                DeviceConfigManifest manifest = new Gson().fromJson(manifestJson, DeviceConfigManifest.class);

                String latestVersion = manifest.getConfigLatestVersion();
                String storedVersion = storage.getVersion();

                if (!latestVersion.equals(storedVersion)) {
                    fetchAndStoreConfig(manifest.getConfigUrl(), latestVersion);
                } else {
                    Log.d(TAG, "Config is up to date.");
                    if (updateListener != null) {
                        updateListener.onConfigUpToDate(storage.loadConfig());
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to fetch manifest: " + errorMessage);
                if (updateListener != null) {
                    updateListener.onUpdateFailed(errorMessage);
                }
            }
        });
    }

    private void fetchAndStoreConfig(String configUrl, String newVersion) {
        httpClient.getText(configUrl, new OkHttpClientHelper.HttpCallback() {
            @Override
            public void onSuccess(OkHttpClientHelper.ResponseResult result) {
                String configJson = result.body;
                WebhookDeviceFilter config = new Gson().fromJson(configJson, WebhookDeviceFilter.class);

                // Delegate persistence to ConfigStorage
                storage.saveVersion(newVersion);
                storage.saveConfig(config);

                Log.d(TAG, "Config updated to version " + newVersion);
                if (updateListener != null) {
                    updateListener.onConfigUpdated(config, newVersion);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to fetch config: " + errorMessage);
                if (updateListener != null) {
                    updateListener.onUpdateFailed(errorMessage);
                }
            }
        });
    }

    public WebhookDeviceFilter getStoredConfig() {
        return storage.loadConfig();
    }

    public void logAllPrefs() {
        storage.logAllPrefs();
    }

    /**
     * 🔹 Callback interface
     */
    public interface ConfigUpdateListener {
        void onConfigUpdated(WebhookDeviceFilter config, String version);

        void onConfigUpToDate(WebhookDeviceFilter config);

        void onUpdateFailed(String errorMessage);
    }
}