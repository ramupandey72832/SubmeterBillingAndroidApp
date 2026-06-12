package com.application.baselibrary.config.model.deviceconfigmanifest;

import com.google.gson.annotations.SerializedName;

/**
 * DeviceConfigMeta
 * ----------------
 * Represents metadata for device configuration, including latest version and URL.
 */
public class DeviceConfigManifest {

    @SerializedName("configLatestVersion")
    private String configLatestVersion;

    @SerializedName("configUrl")
    private String configUrl;

    // Getters and Setters
    public String getConfigLatestVersion() {
        return configLatestVersion;
    }

    public void setConfigLatestVersion(String configLatestVersion) {
        this.configLatestVersion = configLatestVersion;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }
}
