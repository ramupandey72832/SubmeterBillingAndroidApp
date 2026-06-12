package com.application.baselibrary.power;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class WakeLockManager implements AutoCloseable {

    private static final String TAG = "WakeLockManager";
    private static final long DEFAULT_TIMEOUT_MS = 10 * 60 * 1000L; // 10 minutes

    private PowerManager.WakeLock wakeLock;

    public WakeLockManager(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "com.rohit.makingpermissionlibrary:WakeLock"
            );
            wakeLock.setReferenceCounted(false);
        } else {
            Log.w(TAG, "PowerManager not available, WakeLock cannot be created");
        }
    }

    public void acquire() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire(DEFAULT_TIMEOUT_MS);
            Log.i(TAG, "WakeLock acquired for " + DEFAULT_TIMEOUT_MS + " ms");
        }
    }

    public void release() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.i(TAG, "WakeLock released");
        }
    }

    @Override
    public void close() {
        release();
    }
}

