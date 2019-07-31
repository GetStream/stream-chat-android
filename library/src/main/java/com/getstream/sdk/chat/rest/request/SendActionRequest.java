package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class SendActionRequest {
    @SerializedName("id")
    @Expose
    String channelId;

    @SerializedName("message_id")
    @Expose
    String messageId;

    @SerializedName("type")
    @Expose
    String type;

    @SerializedName("form_data")
    @Expose
    Map<String, String> formData;

    public SendActionRequest(String channelId, String messageId, String type, Map form_data) {
        this.channelId = channelId;
        this.messageId = messageId;
        this.type = type;
        this.formData = form_data;
    }
}
