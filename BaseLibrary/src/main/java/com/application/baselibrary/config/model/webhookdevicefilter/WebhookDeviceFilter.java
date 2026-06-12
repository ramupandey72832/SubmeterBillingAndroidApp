package com.application.baselibrary.config.model.webhookdevicefilter;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WebhookDeviceFilter {

    @SerializedName("webhookUrl")
    private String webhookUrl;

    @SerializedName("filterDevices")
    private FilterDevices filterDevices;

    // Getters & Setters
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

    public FilterDevices getFilterDevices() { return filterDevices; }
    public void setFilterDevices(FilterDevices filterDevices) { this.filterDevices = filterDevices; }
}
