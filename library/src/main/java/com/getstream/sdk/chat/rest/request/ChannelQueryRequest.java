package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.enums.Pagination;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ChannelQueryRequest extends BaseQueryChannelRequest<ChannelQueryRequest> {

    @SerializedName("messages")
    @Expose
    protected Map<String, Object> messages;

    @SerializedName("watchers")
    @Expose
    protected Map<String, Object> watchers;

    @SerializedName("members")
    @Expose
    protected Map<String, Object> members;

    @SerializedName("data")
    @Expose
    protected Map<String, Object> data;

    public ChannelQueryRequest() {
        this.watch = false;
        this.presence = false;
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
        if (this.watchers != null) {
            _this.watchers = new HashMap<>(this.watchers);
        }
        if (this.members != null) {
            _this.members = new HashMap<>(this.members);
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

    public ChannelQueryRequest withPresence() {
        ChannelQueryRequest clone = this.cloneOpts();
        clone.presence = true;
        return clone;
    }

    public ChannelQueryRequest withMembers(int limit, int offset) {
        ChannelQueryRequest clone = this.cloneOpts();
        Map<String, Object> members = new HashMap<>();
        members.put("limit", limit);
        members.put("offset", offset);
        clone.members = members;
        return clone;
    }

    public ChannelQueryRequest withWatchers(int limit, int offset) {
        ChannelQueryRequest clone = this.cloneOpts();
        Map<String, Object> watchers = new HashMap<>();
        watchers.put("limit", limit);
        watchers.put("offset", offset);
        clone.watchers = watchers;
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

    public Map<String, Object> getData() {
        return data;
    }
}
