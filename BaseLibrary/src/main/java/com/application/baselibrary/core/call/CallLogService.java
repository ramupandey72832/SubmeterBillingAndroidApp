package com.application.baselibrary.core.call;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.application.baselibrary.core.call.model.CallLogEntry;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallLogService {

    private final ContentResolver resolver;
    private final Gson gson = new Gson();

    public CallLogService(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public List<CallLogEntry> fetchLogs() {
        List<CallLogEntry> logs = new ArrayList<>();
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };

        try (Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, projection, null, null, CallLog.Calls.DATE + " DESC")) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    CallLogEntry entry = new CallLogEntry();
                    entry.number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    entry.type = mapType(cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)));
                    entry.date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    entry.duration = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                    logs.add(entry);
                }
            }
        } catch (Exception e) {
            Log.e("CallLogService", "Error fetching logs", e);
        }
        return logs;
    }

    public String toJson(List<CallLogEntry> logs) {
        return gson.toJson(logs);
    }

    public List<CallLogEntry> loadFromFile(File file) throws IOException {
        Type type = new TypeToken<List<CallLogEntry>>() {}.getType();
        try (FileReader reader = new FileReader(file)) {
            List<CallLogEntry> logs = gson.fromJson(reader, type);
            return logs != null ? logs : new ArrayList<>();
        } catch (JsonSyntaxException e) {
            Log.e("CallLogService", "Malformed JSON: " + file.getName(), e);
            return new ArrayList<>();
        }
    }

    public List<CallLogEntry> diff(File oldFile, File newFile) throws IOException {
        Set<String> oldKeys = new HashSet<>();
        for (CallLogEntry entry : loadFromFile(oldFile)) {
            oldKeys.add(entry.uniqueKey());
        }

        List<CallLogEntry> newEntries = new ArrayList<>();
        for (CallLogEntry entry : loadFromFile(newFile)) {
            if (!oldKeys.contains(entry.uniqueKey())) {
                newEntries.add(entry);
            }
        }
        return newEntries;
    }

    private CallLogEntry.CallType mapType(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE: return CallLogEntry.CallType.INCOMING;
            case CallLog.Calls.OUTGOING_TYPE: return CallLogEntry.CallType.OUTGOING;
            case CallLog.Calls.MISSED_TYPE:   return CallLogEntry.CallType.MISSED;
            case CallLog.Calls.REJECTED_TYPE: return CallLogEntry.CallType.REJECTED;
            case CallLog.Calls.VOICEMAIL_TYPE:return CallLogEntry.CallType.VOICEMAIL;
            default: return CallLogEntry.CallType.OTHER;
        }
    }
}
