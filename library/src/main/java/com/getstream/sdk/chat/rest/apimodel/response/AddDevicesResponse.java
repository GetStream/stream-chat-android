package com.getstream.sdk.chat.rest.apimodel.response;

import com.getstream.sdk.chat.model.Device;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddDevicesResponse {
    @SerializedName("duration")
    @Expose
    private String duration;
}
