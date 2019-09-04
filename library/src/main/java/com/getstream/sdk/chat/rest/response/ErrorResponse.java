package com.getstream.sdk.chat.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ErrorResponse {
    @SerializedName("code")
    @Expose
    private int code;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("StatusCode")
    @Expose
    private int StatusCode;

    @SerializedName("duration")
    @Expose
    private String duration;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return StatusCode;
    }

    public String getDuration() {
        return duration;
    }
}
