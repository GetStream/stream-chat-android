package com.getstream.sdk.chat.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CompletableResponse {
    @SerializedName("duration")
    @Expose
    private String duration;

    public String getDuration() {
        return duration;
    }
}
