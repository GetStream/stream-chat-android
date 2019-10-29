package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class MarkReadRequest {
    @SerializedName("event")
    @Expose
    Map<String, Object> event;

    public MarkReadRequest(String messageId) {
        Map<String, Object> map = new HashMap<>();
        if (messageId != null)
            map.put("message_id", messageId);
        this.event = map;
    }

    public Map<String, Object> getEvent() {
        return event;
    }
}
