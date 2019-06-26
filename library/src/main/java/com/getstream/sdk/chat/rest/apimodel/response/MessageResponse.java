package com.getstream.sdk.chat.rest.apimodel.response;

import com.getstream.sdk.chat.model.message.Message;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("message")
    @Expose
    private Message message;

    public Message getMessage() {
        return message;
    }
}
