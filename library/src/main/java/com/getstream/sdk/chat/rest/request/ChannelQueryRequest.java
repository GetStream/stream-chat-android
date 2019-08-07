package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.enums.Pagination;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ChannelQueryRequest {
    @SerializedName("messages")
    Map<String, Object> messages;

    @SerializedName("state")
    boolean state;

    @SerializedName("watch")
    boolean watch;

    @SerializedName("presence")
    boolean presence;

    @SerializedName("data")
    Map<String, Object> data;

    public ChannelQueryRequest() {
        this.state = true;
        this.watch = true;
        this.presence = false;
        this.data = new HashMap<>();
        this.messages = new HashMap<>();
    }

    private ChannelQueryRequest cloneOpts() {
        ChannelQueryRequest _this = new ChannelQueryRequest();
        _this.state = this.state;
        _this.watch = this.state;
        _this.presence = this.state;
        _this.messages = new HashMap<>(this.messages);
        _this.data = new HashMap<>(this.data);
        return _this;
    }

    public ChannelQueryRequest withData(Map<String, Object> data) {
        ChannelQueryRequest clone = this.cloneOpts();
        clone.data = data;
        return clone;
    }

    public ChannelQueryRequest withWatch() {
        ChannelQueryRequest clone = this.cloneOpts();
        clone.watch = true;
        return clone;
    }

    public ChannelQueryRequest noWatch() {
        ChannelQueryRequest clone = this.cloneOpts();
        clone.watch = false;
        return clone;
    }

    public ChannelQueryRequest withState() {
        ChannelQueryRequest clone = this.cloneOpts();
        clone.state = true;
        return clone;
    }

    public ChannelQueryRequest noState() {
        ChannelQueryRequest clone = this.cloneOpts();
        clone.state = false;
        return clone;
    }

    public ChannelQueryRequest withPresence() {
        ChannelQueryRequest clone = this.cloneOpts();
        return clone;
    }

    public ChannelQueryRequest noPresence() {
        ChannelQueryRequest clone = this.cloneOpts();
        clone.presence = false;
        return clone;
    }

    public ChannelQueryRequest withMessages(int limit) {
        ChannelQueryRequest clone = this.cloneOpts();
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", limit);
        clone.messages = messages;
        return clone;
    }

    public ChannelQueryRequest withMessages(Pagination direction, String messageId, int limit) {
        ChannelQueryRequest clone = this.cloneOpts();
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", limit);
        messages.put(direction.toString(), messageId);
        clone.messages = messages;
        return clone;
    }
}
