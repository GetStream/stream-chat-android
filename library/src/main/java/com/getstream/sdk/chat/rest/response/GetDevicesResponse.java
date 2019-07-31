package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.model.Device;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetDevicesResponse {
    @SerializedName("devices")
    @Expose
    private List<Device> devices;

    public List<Device> getDevices() {
        return devices;
    }
}
