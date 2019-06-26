package com.getstream.sdk.chat.rest.apimodel.response;

import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.model.message.Message;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetRepliesResponse {

    @SerializedName("messages")
    @Expose
    private List<Message>messages;

    public List<Message> getMessages() {
        return messages;
    }
}
