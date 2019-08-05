package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * An event
 */
public class Event {
    @SerializedName("connection_id")
    @Expose
    private String connection_id;

    @SerializedName("cid")
    @Expose
    private String cid;

    @SerializedName("client_id")
    @Expose
    private String clientId;

    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("me")
    @Expose
    private User me;

    @SerializedName("member")
    @Expose
    private User member;

    @SerializedName("message")
    @Expose
    private Message message;

    @SerializedName("reaction")
    @Expose
    private Reaction reaction;

    @SerializedName("channel")
    @Expose
    private Channel channel;

    @SerializedName("total_unread_count")
    @Expose
    private int total_unread_count;

    @SerializedName("watcher_count")
    @Expose
    private int watcher_count;

    @SerializedName("created_at")
    @Expose
    private String created_at;

    private boolean online;

    public Event() {}

    public Event(boolean online) {
        this.online = online;
        setType(EventType.CONNECTION_CHANGED);
    }

    public String getConnection_id() {
        return connection_id;
    }

    public String getCid() {
        return cid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType(EventType type) {
        this.type = type.label;
    }

    public EventType getType() {
        return EventType.findByString(type);
    }

    public User getUser() {
        return user;
    }

    public User getMe() {
        return me;
    }

    public User getMember() {
        return member;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public Reaction getReaction() {
        return reaction;
    }

    public Channel getChannel() {
        return channel;
    }

    public int getTotal_unread_count() {
        return total_unread_count;
    }

    public int getWatcher_count() {
        return watcher_count;
    }

    public String getCreated_at() {
        return created_at;
    }

    public Boolean getOnline() {
        return online;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
