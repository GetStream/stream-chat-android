package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.enums.Pagination;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ChannelQueryRequest extends BaseQueryChannelRequest<ChannelQueryRequest> {

    @SerializedName("messages")
    protected Map<String, Object> messages;
    @SerializedName("data")
    private Map<String, Object> data;

    public ChannelQueryRequest() {
        this.watch = true;
        this.state = true;
    }

    protected ChannelQueryRequest cloneOpts() {
        ChannelQueryRequest _this = new ChannelQueryRequest();
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

    public ChannelQueryRequest withData(Map<String, Object> data) {
        ChannelQueryRequest clone = this.cloneOpts();
        clone.data = data;
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
