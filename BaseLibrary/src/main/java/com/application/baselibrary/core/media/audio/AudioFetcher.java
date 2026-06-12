package com.application.baselibrary.core.media.audio;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.application.baselibrary.core.media.audio.model.AudioInfo;

import java.util.ArrayList;
import java.util.List;

public class AudioFetcher {
    private static final String TAG = "AudioFetcher";
    private final Context context;

    public AudioFetcher(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<AudioInfo> getAllAudioFiles() {
        List<AudioInfo> audioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED
        };

        try (Cursor cursor = context.getContentResolver().query(
                uri, projection, null, null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC")) {

            if (cursor == null) return audioList;

            int idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int nameIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int dataIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int sizeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int dateModIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIdx);
                String path = cursor.getString(dataIdx);
                long size = cursor.getLong(sizeIdx);
                long dateModified = cursor.getLong(dateModIdx) * 1000L; // normalize

                if (path == null && idIdx != -1) {
                    long id = cursor.getLong(idIdx);
                    path = Uri.withAppendedPath(uri, String.valueOf(id)).toString();
                }

                audioList.add(new AudioInfo(name, path, size, dateModified));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching audio files", e);
        }

        return audioList;
    }
}
