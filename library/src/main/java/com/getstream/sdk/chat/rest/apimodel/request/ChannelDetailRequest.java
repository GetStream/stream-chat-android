package com.getstream.sdk.chat.rest.apimodel.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class ChannelDetailRequest {
    @SerializedName("messages")
    @Expose
    Map<String, Object> messages;

    @SerializedName("data")
    @Expose
    Map<String, Object> data;

    @SerializedName("state")
    @Expose
    boolean state;

    @SerializedName("watch")
    @Expose
    boolean watch;

    @SerializedName("subscribe")
    @Expose
    boolean subscribe;

    public ChannelDetailRequest(Map messages, Map data, boolean state, boolean watch) {
        this.messages = messages;
        this.data = data;
        this.state = state;
        this.watch = watch;
        this.subscribe = true;
    }
}
