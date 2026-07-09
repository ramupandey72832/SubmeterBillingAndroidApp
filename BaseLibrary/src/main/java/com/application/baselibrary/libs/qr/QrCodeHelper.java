package com.application.baselibrary.libs.qr;




import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import java.io.InputStream;

/**
 * Modern QR Engine: Uses Google ML Kit for high-accuracy scanning
 * and ZXing for high-quality generation.
 */
public class QrCodeHelper {
    private static final String TAG = "QrCodeHelper";
    private final BarcodeScanner scanner;

    public QrCodeHelper() {
        // Initialize the ML Kit scanner
        this.scanner = BarcodeScanning.getClient();
    }

    /**
     * Interface to handle the async nature of ML Kit
     */
    public interface OnQrResultListener {
        void onSuccess(String data);
        void onFailure(String message);
    }
    // Inside your scanFromUri logic
    private Bitmap getEnhancedDenseQr(Bitmap original) {
        // 1. Add a White Border (Quiet Zone)
        int padding = 40; // Add 40px of white space
        Bitmap borderedBitmap = Bitmap.createBitmap(
                original.getWidth() + (padding * 2),
                original.getHeight() + (padding * 2),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(borderedBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(original, padding, padding, null);

        // 2. Convert to Pure Black and White (Binarization)
        // This removes all grey "fuzziness" from the tiny modules
        Bitmap bwBitmap = Bitmap.createBitmap(borderedBitmap.getWidth(), borderedBitmap.getHeight(), Bitmap.Config.RGB_565);
        for (int x = 0; x < borderedBitmap.getWidth(); x++) {
            for (int y = 0; y < borderedBitmap.getHeight(); y++) {
                int pixel = borderedBitmap.getPixel(x, y);
                // Calculate luminance
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // If gray < 128, make it pure black. Otherwise, pure white.
                bwBitmap.setPixel(x, y, gray < 128 ? Color.BLACK : Color.WHITE);
            }
        }
        return bwBitmap;
    }

    public void scanFromUri(Context context, Uri imageUri, OnQrResultListener listener) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (originalBitmap == null) {
                listener.onFailure("Failed to load image");
                return;
            }

            Bitmap denseBitmap = getEnhancedDenseQr(originalBitmap);
            InputImage image2 = InputImage.fromBitmap(denseBitmap, 0);

            scanner.process(image2)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            // Success! Returns the data from the tricky Airtel/Chrome QR
                            listener.onSuccess(barcodes.get(0).getRawValue());
                        } else {
                            listener.onFailure("No QR Code detected");
                        }
                    })
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));

        } catch (Exception e) {
            Log.e(TAG, "Error processing URI", e);
            listener.onFailure("Error reading file");
        }
    }


    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= maxSize && height <= maxSize) return image;

        float ratio = (float) width / (float) height;
        if (ratio > 1) {
            width = maxSize;
            height = (int) (width / ratio);
        } else {
            height = maxSize;
            width = (int) (height * ratio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public Bitmap generateQrCode(String text, int width, int height) {
        // We keep ZXing for generation because it's still the best for creating clean bits
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code", e);
            return null;
        }
    }
}
