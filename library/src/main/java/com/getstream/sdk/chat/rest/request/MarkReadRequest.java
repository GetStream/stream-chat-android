package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.utils.Global;
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
        Map<String, String> user = new HashMap<>();
        user.put("id", Global.client.user.getId());
        map.put("user", user);
        map.put("message_id", messageId);
        this.event = map;
    }
}
