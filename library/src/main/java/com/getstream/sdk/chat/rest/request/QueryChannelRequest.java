package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class QueryChannelRequest {
    @SerializedName("messages")
    Map<String, Object> messages;
    @SerializedName("data")
    Map<String, Object> data;
    @SerializedName("state")
    boolean state;
    @SerializedName("watch")
    boolean watch;

    @SerializedName("subscribe")
    boolean subscribe;

    public QueryChannelRequest(Map messages, Map data, boolean state, boolean watch) {
        this.messages = messages;
        this.data = data;
        this.state = state;
        this.watch = watch;
        this.subscribe = true;
    }
}
