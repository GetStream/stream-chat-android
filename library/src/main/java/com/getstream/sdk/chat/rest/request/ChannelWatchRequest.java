package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.enums.Pagination;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ChannelWatchRequest extends ChannelQueryRequest {

    @SerializedName("messages")
    protected Map<String, Object> messages;
    @SerializedName("data")
    private Map<String, Object> data;

    public ChannelWatchRequest() {
        this.watch = true;
        this.presence = false;
        this.state = true;
    }

    protected ChannelWatchRequest cloneOpts() {
        ChannelWatchRequest _this = new ChannelWatchRequest();
        _this.state = this.state;
        _this.watch = this.watch;
        _this.presence = this.presence;
        if (this.messages != null) {
            _this.messages = new HashMap<>(this.messages);
        }
        if (this.data != null) {
            _this.data = new HashMap<>(this.data);
        }
        return _this;
    }

    public ChannelWatchRequest withData(Map<String, Object> data) {
        ChannelWatchRequest clone = this.cloneOpts();
        clone.data = data;
        return clone;
    }

    public ChannelWatchRequest withMessages(int limit) {
        ChannelWatchRequest clone = this.cloneOpts();
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", limit);
        clone.messages = messages;
        return clone;
    }

    public ChannelWatchRequest withMessages(Pagination direction, String messageId, int limit) {
        ChannelWatchRequest clone = this.cloneOpts();
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", limit);
        messages.put(direction.toString(), messageId);
        clone.messages = messages;
        return clone;
    }

    public ChannelWatchRequest withPresence() {
        ChannelWatchRequest clone = this.cloneOpts();
        clone.presence = true;
        return clone;
    }
}
