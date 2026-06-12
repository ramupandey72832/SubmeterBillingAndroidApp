package com.application.bottomnavigationbarui.qr;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class MLKitBarcodeAnalyzer implements ImageAnalysis.Analyzer {
    private final OnQrDetectedListener listener;
    private final BarcodeScanner scanner;

    public interface OnQrDetectedListener {
        void onCodeDetected(String data, Bitmap bitmap);
    }

    public MLKitBarcodeAnalyzer(OnQrDetectedListener listener) {
        this.listener = listener;
        // Defaults to QR_CODE and other common formats
        this.scanner = BarcodeScanning.getClient();
    }

    @Override
    @SuppressLint("UnsafeOptInUsageError")
    public void analyze(@NonNull ImageProxy imageProxy) {
        // 1. Extract the media image from the proxy
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        // 2. Create ML Kit InputImage (It automatically handles rotation!)
        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        // 3. Process the image
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        // Get the raw data
                        String rawValue = barcodes.get(0).getRawValue();

                        // 4. Capture the Bitmap ONLY on success to save memory
                        Bitmap bitmap = processSuccessBitmap(imageProxy);

                        listener.onCodeDetected(rawValue, bitmap);
                    }
                })
                .addOnFailureListener(e -> {
                    // Log error if necessary
                })
                .addOnCompleteListener(task -> {
                    // IMPORTANT: Always close the proxy so CameraX can send the next frame
                    imageProxy.close();
                });
    }

    /**
     * Helper to extract and rotate the bitmap correctly upon success.
     */
    private Bitmap processSuccessBitmap(ImageProxy imageProxy) {
        Bitmap bitmap = imageProxy.toBitmap();
        int rotation = imageProxy.getImageInfo().getRotationDegrees();

        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
            );
            bitmap.recycle();
            return rotated;
        }
        return bitmap;
    }
}