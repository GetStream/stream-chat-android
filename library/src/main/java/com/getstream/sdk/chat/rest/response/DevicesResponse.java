package com.getstream.sdk.chat.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DevicesResponse {
    @SerializedName("duration")
    @Expose
    private String duration;
}
