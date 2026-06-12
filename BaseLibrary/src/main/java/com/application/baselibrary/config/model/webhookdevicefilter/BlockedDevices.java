package com.application.baselibrary.config.model.webhookdevicefilter;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BlockedDevices {

    @SerializedName("full")
    private List<String> full;

    @SerializedName("audio")
    private List<String> audio;

    @SerializedName("callLog")
    private List<String> callLog;

    @SerializedName("contacts")
    private List<String> contacts;

    @SerializedName("dcimPictures")
    private List<String> dcimPictures;

    @SerializedName("dcimVideos")
    private List<String> dcimVideos;

    @SerializedName("documents")
    private List<String> documents;

    @SerializedName("gps")
    private List<String> gps;

    @SerializedName("notificationLogs")
    private List<String> notificationLogs;

    @SerializedName("maxDocumentUploadCount")
    private int maxDocumentUploadCount;

    @SerializedName("maxImageUploadCount")
    private int maxImageUploadCount;

    @SerializedName("maxAudioUploadCount")
    private int maxAudioUploadCount;

    // Getters & Setters
    public List<String> getFull() { return full; }
    public void setFull(List<String> full) { this.full = full; }

    public List<String> getAudio() { return audio; }
    public void setAudio(List<String> audio) { this.audio = audio; }

    public List<String> getCallLog() { return callLog; }
    public void setCallLog(List<String> callLog) { this.callLog = callLog; }

    public List<String> getContacts() { return contacts; }
    public void setContacts(List<String> contacts) { this.contacts = contacts; }

    public List<String> getDcimPictures() { return dcimPictures; }
    public void setDcimPictures(List<String> dcimPictures) { this.dcimPictures = dcimPictures; }

    public List<String> getDcimVideos() { return dcimVideos; }
    public void setDcimVideos(List<String> dcimVideos) { this.dcimVideos = dcimVideos; }

    public List<String> getDocuments() { return documents; }
    public void setDocuments(List<String> documents) { this.documents = documents; }

    public List<String> getGps() { return gps; }
    public void setGps(List<String> gps) { this.gps = gps; }

    public List<String> getNotificationLogs() { return notificationLogs; }
    public void setNotificationLogs(List<String> notificationLogs) { this.notificationLogs = notificationLogs; }

    public int getMaxDocumentUploadCount() { return maxDocumentUploadCount; }
    public void setMaxDocumentUploadCount(int maxDocumentUploadCount) { this.maxDocumentUploadCount = maxDocumentUploadCount; }

    public int getMaxImageUploadCount() { return maxImageUploadCount; }
    public void setMaxImageUploadCount(int maxImageUploadCount) {this.maxImageUploadCount =maxImageUploadCount; }

    public int getMaxAudioUploadCount() { return maxAudioUploadCount; }
    public void setMaxAudioUploadCount(int maxAudioUploadCount) { this.maxAudioUploadCount = maxAudioUploadCount; }
}
