package com.application.baselibrary.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissionHelper {

    private final Context context;

    public PermissionHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * ✅ Unified check method
     */
    public boolean isPermissionGranted(String permissionName) {
        return ContextCompat.checkSelfPermission(context, permissionName)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * ✅ Notification listener check
     */
    public boolean isNotificationServiceEnabled() {
        Set<String> enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context);
        return enabledPackages != null && enabledPackages.contains(context.getPackageName());
    }

    /**
     * ✅ Open notification listener settings
     */
    public void requestNotificationPermission() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * ✅ Status Report Method
     */
    public Map<String, Boolean> getPermissionStatusReport(List<String> permissions) {
        Map<String, Boolean> statusMap = new HashMap<>();

        for (String permission : permissions) {
            switch (permission) {
                case android.Manifest.permission.SYSTEM_ALERT_WINDOW:
                    statusMap.put(permission, Settings.canDrawOverlays(context));
                    break;

                case android.Manifest.permission.WRITE_SETTINGS:
                    statusMap.put(permission, Settings.System.canWrite(context));
                    break;

                case Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        if (pm != null && !pm.isIgnoringBatteryOptimizations(context.getPackageName())) {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + context.getPackageName()));
                            context.startActivity(intent);
                        }else{
                            statusMap.put(permission, true);
                        }
                    }


                case android.Manifest.permission.POST_NOTIFICATIONS:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        statusMap.put(permission, isPermissionGranted(permission));
                    } else {
                        statusMap.put(permission, true); // auto-granted
                    }
                    break;

                case android.Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        statusMap.put(permission, isPermissionGranted(permission));
                    } else {
                        statusMap.put(permission, true); // auto-granted
                    }
                    break;

                case android.Manifest.permission.BLUETOOTH_CONNECT:
                case android.Manifest.permission.BLUETOOTH_SCAN:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        statusMap.put(permission, isPermissionGranted(permission));
                    } else {
                        statusMap.put(permission, true); // auto-granted
                    }
                    break;

                case android.Manifest.permission.READ_CONTACTS:
                case android.Manifest.permission.WRITE_CONTACTS:
                    statusMap.put(permission, isPermissionGranted(permission));
                    break;

                case android.Manifest.permission.ACCESS_WIFI_STATE:
                case android.Manifest.permission.CHANGE_WIFI_STATE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        statusMap.put(permission, isPermissionGranted(permission));
                    } else {
                        statusMap.put(permission, true); // auto-granted
                    }
                    break;

                case android.Manifest.permission.READ_MEDIA_IMAGES:
                case android.Manifest.permission.READ_MEDIA_VIDEO:
                case android.Manifest.permission.READ_MEDIA_AUDIO:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        statusMap.put(permission, isPermissionGranted(permission));
                    } else {
                        statusMap.put(permission, isPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE));
                    }
                    break;

                case android.Manifest.permission.FOREGROUND_SERVICE_CAMERA:
                case android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE:
                case android.Manifest.permission.FOREGROUND_SERVICE_LOCATION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        statusMap.put(permission, isPermissionGranted(permission));
                    } else {
                        statusMap.put(permission, true); // auto-granted
                    }
                    break;

                default:
                    statusMap.put(permission, isPermissionGranted(permission));
                    break;
            }
        }
        return statusMap;
    }
}
