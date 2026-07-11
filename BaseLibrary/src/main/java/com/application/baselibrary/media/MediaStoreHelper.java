package com.application.baselibrary.media;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Build;

public class MediaStoreHelper {
    private static final String TAG = "MediaStoreHelper";

    @Nullable
    public static Uri checkDownloadFileExistence(@NonNull Context context, @NonNull String fileName) {
        // Fallback table for API 24-28, official Downloads table for API 29+
        Uri collectionUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        } else {
            collectionUri = MediaStore.Files.getContentUri("external");
        }

        String[] projection = new String[]{ MediaStore.MediaColumns._ID };
        String selection = MediaStore.MediaColumns.DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{ fileName };

        try (Cursor cursor = context.getContentResolver().query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                long fileId = cursor.getLong(idColumnIndex);
                return ContentUris.withAppendedId(collectionUri, fileId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to query MediaStore for file existence: " + fileName, e);
        }

        return null;
    }
}