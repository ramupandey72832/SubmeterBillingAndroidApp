package com.application.baselibrary.config.model.webhookdevicefilter;

import com.google.gson.annotations.SerializedName;

public class FilterDevices {

    @SerializedName("blockedDevices")
    private BlockedDevices blockedDevices;

    public BlockedDevices getBlockedDevices() { return blockedDevices; }
    public void setBlockedDevices(BlockedDevices blockedDevices) { this.blockedDevices = blockedDevices; }
}
