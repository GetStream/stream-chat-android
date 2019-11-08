package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.rest.Message;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("message")
    @Expose
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
