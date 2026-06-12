package com.application.baselibrary.core.media.document;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import com.application.baselibrary.core.media.document.model.DocumentInfo;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for fetching documents
 */
class DocumentHelper {
    private static final String TAG = "DocumentHelper";
    public static final int REQUEST_CODE_PICK_DOCUMENT = 1001;

    public static final String[] SUPPORTED_MIME_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "application/rtf"
    };

    private final Context context;

    public DocumentHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Normalize date values (seconds → millis if needed)
     */
    private static long normalizeDate(long raw) {
        return (raw < 1000000000000L) ? raw * 1000L : raw;
    }

    /**
     * Fetch all documents from MediaStore
     */
    public List<DocumentInfo> getAllDocumentsFromMediaStore() {
        List<DocumentInfo> documents = new ArrayList<>();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            projection = new String[]{
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.RELATIVE_PATH
            };
        } else {
            projection = new String[]{
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED
            };
        }

        // Selection for supported MIME types
        String selection = String.join(" OR ",
                Collections.nCopies(SUPPORTED_MIME_TYPES.length,
                        MediaStore.Files.FileColumns.MIME_TYPE + "=?"));

        try (Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, SUPPORTED_MIME_TYPES,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC")) {

            if (cursor == null) return documents;

            int idIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
            int nameIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
            int sizeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
            int dateModIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
            int dataIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int relPathIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIdx);
                long size = cursor.getLong(sizeIdx);
                long dateModified = normalizeDate(cursor.getLong(dateModIdx));

                String path = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && relPathIdx != -1) {
                    long id = cursor.getLong(idIdx);
                    Uri contentUri = ContentUris.withAppendedId(uri, id);
                    path = contentUri.toString();
                    String relPath = cursor.getString(relPathIdx);

                    Log.d(TAG, "Q+ file: " + name + " relPath=" + relPath);
                } else if (dataIdx != -1) {
                    path = cursor.getString(dataIdx);
                    Log.d(TAG, "Pre-Q file: " + name + " path=" + path);
                }

                if (path != null) {
                    documents.add(new DocumentInfo(name, path, size, dateModified));
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying MediaStore", e);
        }

        return documents;
    }

    public List<DocumentInfo> getDocumentsByExtensions(String[] extensions, boolean onlyDocsAndDownloads) {
        List<DocumentInfo> documents = new ArrayList<>();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            projection = new String[] {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.RELATIVE_PATH
            };
        } else {
            projection = new String[] {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED
            };
        }

        try (Cursor cursor = context.getContentResolver().query(
                uri, projection, null, null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC")) {

            if (cursor == null) return documents;

            int idIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
            int nameIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
            int sizeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
            int dateModIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
            int relPathIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH);
            int dataIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIdx);
                if (name == null) continue;

                // Extension filter
                boolean matchesExt = false;
                for (String ext : extensions) {
                    if (name.toLowerCase().endsWith(ext.toLowerCase())) {
                        matchesExt = true;
                        break;
                    }
                }
                if (!matchesExt) continue;

                // Folder filter
                boolean inTargetFolder = true;
                if (onlyDocsAndDownloads) {
                    String relPath = (relPathIdx != -1) ? cursor.getString(relPathIdx) : null;
                    String dataPath = (dataIdx != -1) ? cursor.getString(dataIdx) : null;

                    if (relPath != null) {
                        inTargetFolder = relPath.contains("Documents") || relPath.contains("Download");
                    } else if (dataPath != null) {
                        inTargetFolder = dataPath.contains("/Documents/") || dataPath.contains("/Download/");
                    }
                }
                if (!inTargetFolder) continue;

                long id = cursor.getLong(idIdx);
                long size = cursor.getLong(sizeIdx);
                long dateModified = normalizeDate(cursor.getLong(dateModIdx));

                Uri contentUri = ContentUris.withAppendedId(uri, id);
                documents.add(new DocumentInfo(name, contentUri.toString(), size, dateModified));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying MediaStore", e);
        }

        return documents;
    }



    /**
     * SAF picker
     */
    public void openDocumentPicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, SUPPORTED_MIME_TYPES);
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_DOCUMENT);
    }

    /**
     * SAF result handler
     */
    public DocumentInfo handlePickedDocument(Uri uri) {
        if (uri == null) return new DocumentInfo(null, null, 0, 0);

        String name = null;
        long size = 0;
        long dateModified = 0;

        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));

                int dateIdx = cursor.getColumnIndex("last_modified");
                if (dateIdx == -1)
                    dateIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
                if (dateIdx != -1) dateModified = normalizeDate(cursor.getLong(dateIdx));
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to read metadata for uri: " + uri, e);
        }
        return new DocumentInfo(name, uri.toString(), size, dateModified);
    }
}

/**
 * JSON persistence utility
 */
class JsonFileStore<T> {
    private final File file;
    private final Gson gson = new Gson();

    public JsonFileStore(Context context, String fileName) {
        this.file = new File(context.getFilesDir(), fileName);
    }

    public void save(T data) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(data));
        } catch (IOException e) {
            Log.e("JsonFileStore", "Error saving data", e);
        }
    }

    public T load(Type typeOfT, T defaultValue) {
        if (!file.exists()) return defaultValue;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return gson.fromJson(reader, typeOfT);
        } catch (IOException e) {
            Log.e("JsonFileStore", "Error loading data", e);
            return defaultValue;
        }
    }
}