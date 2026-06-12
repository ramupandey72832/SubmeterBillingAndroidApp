package com.application.baselibrary.activity;


import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PermissionManager {

    private final ComponentActivity activity;
    private final PermissionHelper helper;
    private final PermissionCallback callback;
    private final ActivityResultLauncher<String[]> permissionLauncher;

    public interface PermissionCallback {
        void onResult(List<String> grantedPermissions, List<String> deniedPermissions);
    }

    /**
     * Constructor for Activities
     */
    public PermissionManager(ComponentActivity activity, PermissionCallback callback) {
        this.activity = activity;
        this.helper = new PermissionHelper(activity);
        this.callback = callback;

        this.permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    List<String> granted = new ArrayList<>();
                    List<String> denied = new ArrayList<>();

                    for (String permission : result.keySet()) {
                        if (Boolean.TRUE.equals(result.get(permission))) {
                            granted.add(permission);
                        } else {
                            denied.add(permission);
                        }
                    }
                    if (this.callback != null) {
                        this.callback.onResult(granted, denied);
                    }
                }
        );
    }

    /**
     * ✅ Unified check method.
     */
    public boolean isPermissionGranted(Context context, String permissionName) {
        return ContextCompat.checkSelfPermission(context, permissionName)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * ✅ Request runtime permissions
     */
    public void requestPermissions(List<String> permissions) {
        List<String> toRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (!helper.isPermissionGranted(permission)) {
                toRequest.add(permission);
            }
        }
        if (!toRequest.isEmpty()) {
            permissionLauncher.launch(toRequest.toArray(new String[0]));
        } else {
            if (callback != null) {
                callback.onResult(permissions, new ArrayList<>());
            }
        }
    }

    /**
     * ✅ Special routing for single permission
     */
    public void requestPermission(String permissionName) {
        switch (permissionName) {

            case Manifest.permission.POST_NOTIFICATIONS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(List.of(android.Manifest.permission.POST_NOTIFICATIONS));
                }
                break;

            case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // First ensure foreground location is granted
                    if (helper.isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                            helper.isPermissionGranted(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                        // ✅ Request background location properly
                        requestPermissions(List.of(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION));
                    } else {
                        Log.e("PermissionManager", "Foreground location must be granted before requesting background location.");
                    }
                }
                break;
            case Manifest.permission.BLUETOOTH_CONNECT:
            case Manifest.permission.BLUETOOTH_SCAN:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(List.of(android.Manifest.permission.BLUETOOTH_CONNECT,
                            android.Manifest.permission.BLUETOOTH_SCAN));
                }
                break;
            case Manifest.permission.READ_CONTACTS:
            case Manifest.permission.WRITE_CONTACTS:
                requestPermissions(List.of(android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.WRITE_CONTACTS));
                break;
            case Manifest.permission.ACCESS_WIFI_STATE:
            case Manifest.permission.CHANGE_WIFI_STATE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(List.of(android.Manifest.permission.ACCESS_WIFI_STATE,
                            android.Manifest.permission.CHANGE_WIFI_STATE));
                }
                break;
            case Manifest.permission.READ_MEDIA_IMAGES:
            case Manifest.permission.READ_MEDIA_VIDEO:
            case Manifest.permission.READ_MEDIA_AUDIO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(List.of(android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO, android.Manifest.permission.READ_MEDIA_AUDIO));
                } else {
                    requestPermissions(List.of(android.Manifest.permission.READ_EXTERNAL_STORAGE));
                }
                break;
            case Manifest.permission.FOREGROUND_SERVICE_CAMERA:
            case Manifest.permission.FOREGROUND_SERVICE_MICROPHONE:
            case Manifest.permission.FOREGROUND_SERVICE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    requestPermissions(List.of(android.Manifest.permission.FOREGROUND_SERVICE_CAMERA,
                            android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE, android.Manifest.permission.FOREGROUND_SERVICE_LOCATION));
                }
                break;

            default:
                requestPermissions(List.of(permissionName));
                break;
        }
    }

    public void requestSpecialPermission(String permissionName) {
        switch (permissionName) {
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                if (!Settings.canDrawOverlays(activity)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                }
                break;

            case Manifest.permission.WRITE_SETTINGS:
                if (!Settings.System.canWrite(activity)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                }
                break;

            case Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                        !Environment.isExternalStorageManager()) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        activity.startActivity(intent);
                    }
                }
                break;

            case "IGNORE_BATTERY_OPTIMIZATIONS":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                    if (pm != null && !pm.isIgnoringBatteryOptimizations(activity.getPackageName())) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivity(intent);
                    }
                }
                break;

            case "NOTIFICATION_LISTENER":
                if (!hasNotificationAccess(activity)) {
                    requestNotificationPermission(activity);
                }
                break;
        }
    }

    public boolean hasNotificationAccess(Context context) {
        Set<String> enabledPackages =
                NotificationManagerCompat.getEnabledListenerPackages(context);
        return enabledPackages.contains(context.getPackageName());
    }

    public void requestNotificationPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        context.startActivity(intent);
    }


    /**
     * ✅ Batch request
     */
    public void requestPermissionsBatch(List<String> permissions) {
        for (String permission : permissions) {
            requestPermission(permission);
        }
    }

    public void requestPermissionsSequential(List<String> permissions) {
        if (permissionLauncher == null) {
            Log.e("PermissionManager", "Cannot request permissions without an Activity constructor.");
            return;
        }

        for (String permission : permissions) {
            if (!helper.isPermissionGranted(permission)) {
                // Request this permission one by one
                requestPermission(permission);
            } else {
                Log.d("PermissionManager", permission + " already granted ✅");
            }
        }
    }


    /**
     * ✅ Expose helper methods if needed
     */
    public PermissionHelper getHelper() {
        return helper;
    }
}
