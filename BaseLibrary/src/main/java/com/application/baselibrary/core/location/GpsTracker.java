package com.application.baselibrary.core.location;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * GpsTracker
 * ----------
 * Optimized for post-boot scenarios with multi-provider fallback.
 */
public class GpsTracker implements LocationListener {

    private static final String TAG = "GpsTracker";
    private final Context context;
    private final LocationManager locationManager;
    private Location currentLocation;
    private CountDownLatch latch;
    private HandlerThread handlerThread;

    public GpsTracker(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void startTracking() {
        if (!hasPermissions() || locationManager == null) return;

        try {
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.d(TAG, "Post-Boot Provider Check - GPS: " + gpsEnabled + ", Network: " + networkEnabled);

            if (handlerThread == null || !handlerThread.isAlive()) {
                handlerThread = new HandlerThread("GpsTrackerThread");
                handlerThread.start();
            }

            Looper looper = handlerThread.getLooper();

            if (gpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this, looper);
            }
            if (networkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this, looper);
            }
            // Passive provider as a safety net
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, this, looper);

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error starting tracking: " + e.getMessage());
        }
    }

    public Location getLocationSync(int timeoutSeconds) {
        latch = new CountDownLatch(1);
        startTracking();

        try {
            Log.d(TAG, "Waiting " + timeoutSeconds + "s for post-boot location fix...");
            boolean received = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            if (!received) {
                Log.w(TAG, "Timed out. System cache might be empty after reboot.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stopTracking();
        }

        return getCurrentLocation();
    }

    public void stopTracking() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
            // ❌ Do not quit the thread here
        } catch (Exception e) {
            Log.e(TAG, "Cleanup error: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (handlerThread != null) {
            handlerThread.quitSafely();
            handlerThread = null;
        }
    }

    public Location getCurrentLocation() {
        if (currentLocation != null) return currentLocation;
        if (!hasPermissions() || locationManager == null) return null;

        try {
            // Aggressive fallback: Check ALL providers (GPS, Network, Passive)
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) continue;

                // Logic: Prefer the location with the best accuracy found so far
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                }
            }
            return bestLocation;
        } catch (SecurityException e) {
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i(TAG, "Location Fix Acquired: " + location.getProvider());
            this.currentLocation = location;
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) { Log.d(TAG, "Provider Enabled: " + provider); }
    @Override public void onProviderDisabled(String provider) { Log.d(TAG, "Provider Disabled: " + provider); }
}

