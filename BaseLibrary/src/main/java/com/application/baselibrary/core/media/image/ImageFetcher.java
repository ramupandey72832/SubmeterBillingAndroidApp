package com.application.baselibrary.core.media.image;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.application.baselibrary.core.media.image.model.ImageInfo;

import java.util.ArrayList;
import java.util.List;

public class ImageFetcher {

    private final Context context;

    public ImageFetcher(Context context) {
        this.context = context;
    }

    /**
     * Get all image URIs from MediaStore with version fallback.
     */
    public List<ImageInfo> getAllImages() {
        List<ImageInfo> imageInfoList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped storage (Android 10+)
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            // Legacy storage
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
        };

        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = resolver.query(collection, projection,
                null, null, sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri contentUri = Uri.withAppendedPath(collection, String.valueOf(id));

                    String displayName = cursor.getString(nameColumn);
                    // Create a new ImageInfo object with the display name
                    imageInfoList.add(new ImageInfo(contentUri, displayName));
                }
            }
        }

        return imageInfoList;
    }
}
