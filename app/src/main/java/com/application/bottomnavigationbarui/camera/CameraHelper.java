package com.application.bottomnavigationbarui.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.application.bottomnavigationbarui.qr.MLKitBarcodeAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraHelper {
    private static final String TAG = "CameraHelper";

    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private ExecutorService executor;

    private final QrResultListener listener;
    private boolean isTorchOn = false;

    public interface QrResultListener {
        void onQrDetected(String data, Uri uri);
    }

    public CameraHelper(QrResultListener listener) {
        this.listener = listener;
    }

    /**
     * Starts the CameraX lifecycle and attaches the ML Kit Analyzer.
     */
    public void startCamera(Context context, @NonNull PreviewView previewView, Fragment fragment) {
        this.previewView = previewView;

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                // 1. Preview Use Case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 2. Image Analysis Use Case (ML Kit)
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(executor, new MLKitBarcodeAnalyzer((data, bitmap) -> {
                    // Logic to save the frame where QR was found and notify listener
                    handleQrDetection(context, data, bitmap, imageAnalysis);
                }));

                // 3. Select Camera and Bind
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                camera = cameraProvider.bindToLifecycle(
                        fragment.getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

                setupFocusListener();

            } catch (Exception e) {
                Log.e(TAG, "Camera binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Saves the detected frame to cache and sends results to the UI thread.
     */
    private void handleQrDetection(Context context, String data, Bitmap bitmap, ImageAnalysis imageAnalysis) {
        Uri uri = null;
        if (bitmap != null) {
            try {
                File file = new File(context.getCacheDir(), "scan_" + System.currentTimeMillis() + ".jpg");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                }
                uri = Uri.fromFile(file);
            } catch (Exception e) {
                Log.e(TAG, "Failed to save result image", e);
            }
        }

        final Uri finalUri = uri;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            if (cameraProvider != null) {
                // Stop analysis once a code is found to prevent multiple triggers
                cameraProvider.unbind(imageAnalysis);
            }
            if (listener != null) {
                listener.onQrDetected(data, finalUri);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupFocusListener() {
        if (previewView == null) return;

        previewView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && camera != null) {
                FocusMeteringAction action = new FocusMeteringAction.Builder(
                        previewView.getMeteringPointFactory().createPoint(event.getX(), event.getY()),
                        FocusMeteringAction.FLAG_AF
                ).setAutoCancelDuration(3, TimeUnit.SECONDS).build();
                camera.getCameraControl().startFocusAndMetering(action);
            }
            return true;
        });
    }

    public boolean toggleTorch() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isTorchOn = !isTorchOn;
            camera.getCameraControl().enableTorch(isTorchOn);
            return isTorchOn;
        }
        return false;
    }

    public void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public void restartCamera(Context context, @NonNull PreviewView previewView, Fragment fragment) {
        stopCamera();
        startCamera(context, previewView, fragment);
    }
}