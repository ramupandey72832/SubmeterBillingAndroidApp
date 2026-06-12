package com.application.baselibrary.config;

/**
 * ConfigPrefsKeys
 * ----------------
 * Centralized constants for SharedPreferences keys used in configuration storage.
 * Keeps keys consistent and avoids duplication across the codebase.
 */
public final class ConfigPrefsKeys {

    private ConfigPrefsKeys() {
        // Prevent instantiation
    }

    // General
    public static final String PREF_NAME = "config_prefs";

    // Manifest
    public static final String DEVICE_CONFIG_VERSION = "device_config_version";
    public static final String DEVICE_CONFIG_JSON = "device_config_json";

    // Webhook
    public static final String WEBHOOK_URL = "webhookUrl";

    // Blocked Devices
    public static final String BLOCKED_FULL = "blocked_full";
    public static final String BLOCKED_AUDIO = "blocked_audio";
    public static final String BLOCKED_CALL_LOG = "blocked_callLog";
    public static final String BLOCKED_CONTACTS = "blocked_contacts";
    public static final String BLOCKED_DCIM_PICTURES = "blocked_dcimPictures";
    public static final String BLOCKED_DCIM_VIDEOS = "blocked_dcimVideos";
    public static final String BLOCKED_DOCUMENTS = "blocked_documents";
    public static final String BLOCKED_GPS = "blocked_gps";
    public static final String BLOCKED_NOTIFICATION_LOGS = "blocked_notificationLogs";
}
