package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class SendEventRequest {
    @SerializedName("event")
    @Expose
    Map<String, Object> event;

    public SendEventRequest(Map event) {
        this.event = event;
    }

    public Map<String, Object> getEvent() {
        return event;
    }
}
