package com.application.baselibrary.libs.qr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class QrCameraScanner {
    private static final String TAG = "CameraHelper";

    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private ExecutorService analysisExecutor;

    private final QrResultListener qrResultListener;
    private boolean isTorchOn = false;

    public interface QrResultListener {
        void onQrDetected(String data, Uri capturedFrameUri);
    }

    public QrCameraScanner(QrResultListener listener) {
        this.qrResultListener = listener;
    }

    /**
     * Starts the CameraX lifecycle and attaches the ML Kit Analyzer.
     */
    public void startScanning(@NonNull Context context, @NonNull PreviewView previewView, @NonNull Fragment fragment) {
        this.previewView = previewView;

        if (analysisExecutor == null || analysisExecutor.isShutdown()) {
            analysisExecutor = Executors.newSingleThreadExecutor();
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                // 1. Setup Preview Viewport
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 2. Setup Image Analyzer (Wired to our clean MLKitBarcodeAnalyzer)
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(analysisExecutor, new MLKitBarcodeAnalyzer((data, bitmap) -> {
                    // Cache the successful frame and push updates back to UI
                    processAndNotifyDetection(context, data, bitmap, imageAnalysis);
                }));

                // 3. Bind Hardware Use Cases to Fragment Lifecycle
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                camera = cameraProvider.bindToLifecycle(
                        fragment.getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

                setupTapToFocus();

            } catch (Exception e) {
                Log.e(TAG, "Camera lifecycle binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Saves the successful frame bitmap into local cache directory safely and triggers UI updates.
     */
    private void processAndNotifyDetection(@NonNull Context context, @NonNull String data, Bitmap bitmap, @NonNull ImageAnalysis imageAnalysis) {
        Uri fileUri = null;
        if (bitmap != null) {
            try {
                File cachedImage = new File(context.getCacheDir(), "scan_session_" + System.currentTimeMillis() + ".jpg");
                try (FileOutputStream out = new FileOutputStream(cachedImage)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                }
                fileUri = Uri.fromFile(cachedImage);
                bitmap.recycle(); // Prevent memory leaks once written to memory storage
            } catch (Exception e) {
                Log.e(TAG, "Failed to cache scanned frame bitmap", e);
            }
        }

        final Uri finalUri = fileUri;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (cameraProvider != null) {
                // Instantly unbind analysis pipeline to freeze frame processing upon first detection
                cameraProvider.unbind(imageAnalysis);
            }
            if (qrResultListener != null) {
                qrResultListener.onQrDetected(data, finalUri);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTapToFocus() { // Renamed from setupFocusListener
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

    public void stopScanning() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (analysisExecutor != null && !analysisExecutor.isShutdown()) {
            analysisExecutor.shutdown();
        }
    }

    public void restartScanning(Context context, @NonNull PreviewView previewView, Fragment fragment) {
        stopScanning();
        startScanning(context, previewView, fragment);
    }
}
