package com.application.bottomnavigationbarui.utils;

import android.util.Log;
import androidx.fragment.app.FragmentActivity;
import com.application.baselibrary.activity.PermissionManager;
import java.util.Arrays;
import java.util.List;

public class LocalPermissionHelper {
    private static final String TAG = "LocalPermissionHelper";
    private final PermissionManager permissionManager;
    private final FragmentActivity activity;
    private final OnPermissionsListener listener;

    private final List<String> requiredPermissions = Arrays.asList(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES
    );

    // Updated Interface to handle both success and failure cases
    public interface OnPermissionsListener {
        void onAllPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    public LocalPermissionHelper(FragmentActivity activity, OnPermissionsListener listener){
        this.listener = listener;
        this.activity = activity;

        this.permissionManager = new PermissionManager(activity, new PermissionManager.PermissionCallback() {
            @Override
            public void onResult(List<String> grantedPermissions, List<String> deniedPermissions) {
                if (deniedPermissions.isEmpty()) {
                    Log.d(TAG, "All permissions just granted by user.");
                    if (listener != null) listener.onAllPermissionsGranted();
                } else {
                    Log.w(TAG, "Permissions denied by user: " + deniedPermissions);
                    if (listener != null) listener.onPermissionsDenied(deniedPermissions);
                }
            }
        });
    }

    public void checkForPermissions() {
        if (hasAllPermissions()) {
            Log.d(TAG, "All permissions already granted.");
            if (listener != null) listener.onAllPermissionsGranted();
        } else {
            permissionManager.requestPermissions(requiredPermissions);
        }
    }

    public boolean hasAllPermissions() {
        if (permissionManager == null) return false;
        for (String permission : requiredPermissions) {
            if (!permissionManager.isPermissionGranted(activity, permission)) {
                Log.i(TAG, "Missing permission: " + permission);
                return false;
            }
        }
        return true;
    }
}