package com.application.baselibrary.data;

public class LibraryConstants {
/**
 * LibraryConstants
 * -------------
 * Centralized location for library-wide constants, file names, and preference keys.
 */

    // Shared Preferences Names
    public static final String PREF_NAME_CONTACTS = "contact_prefs";
    public static final String PREF_NAME_APP_SETTINGS = "app_settings";
    public static final String PREF_NAME_CALL_LOGS = "call_log_prefs";
    public static final String PREF_NAME_GPS_LOGS = "gps_log_prefs";

    // Contact Related Keys
    public static final String KEY_OLD_CONTACTS_PATH = "key_old_contacts_path";
    public static final String KEY_NEW_CONTACTS_PATH = "key_new_contacts_path";

    // Call Log Related Keys
    public static final String KEY_OLD_CALL_LOGS_PATH = "key_old_call_logs_path";
    public static final String KEY_NEW_CALL_LOGS_PATH = "key_new_call_logs_path";

    // GPS Related Keys
    public static final String KEY_GPS_LOG_PATH = "key_gps_log_path";

    // File Names
    public static final String FILE_NAME_OLD_CONTACTS = "contacts_old.json";
    public static final String FILE_NAME_NEW_CONTACTS = "contacts_new.json";
    public static final String FILE_NAME_OLD_CALL_LOGS = "call_logs_old.json";
    public static final String FILE_NAME_NEW_CALL_LOGS = "call_logs_new.json";
    public static final String FILE_NAME_GPS_LOGS = "gps_log.txt";
    public static final String FILE_NAME_NOTIFICATIONS = "notifications_log.json";
    public static final String FILE_NAME_UPLOADED_DOCUMENTS = "uploaded_documents.json";

    // Document Upload Limits
    public static final long DEFAULT_DOCUMENT_UPLOAD_SIZE_LIMIT = 10 * 1024 * 1024; // 10 MB
    public static final int MAX_DOCUMENT_UPLOAD_COUNT = 5;

//    public static final long DEFAULT_IMAGE_UPLOAD_SIZE_LIMIT = 5 * 1024 * 1024; // 5 MB
    public static final String FILE_NAME_UPLOADED_IMAGES = "uploaded_images.json";
    public static final int MAX_IMAGE_UPLOAD_COUNT = 5;

//    public static final long DEFAULT_AUDIO_UPLOAD_SIZE_LIMIT = 5 * 1024 * 1024; // 5 MB
    public static final String FILE_NAME_UPLOADED_AUDIO = "uploaded_audios.json";
    public static final int MAX_AUDIO_UPLOAD_COUNT = 5;




    public static String DeviceConfigManifest_URL = "https://raw.githubusercontent.com/ramupandey72832/HostedAppConfig/refs/heads/main/Android/runningChuwa/DeviceConfigManifest.json";
}