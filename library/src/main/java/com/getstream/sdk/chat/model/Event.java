package com.getstream.sdk.chat.model;

import androidx.room.Ignore;

import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * An event
 */
public class Event implements UserEntity {
    @SerializedName("connection_id")
    @Expose
    private String connectionId;

    @SerializedName("cid")
    @Expose
    private String cid;

    @SerializedName("client_id")
    @Expose
    private String clientId;

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
    private Member member;

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
    private Number totalUnreadCount;

    @SerializedName("unread_channels")
    @Expose
    private Number unreadChannels;

    @SerializedName("watcher_count")
    @Expose
    private Number watcherCount;

    @SerializedName("created_at")
    @Expose
    private Date createdAt;

    @SerializedName("clear_history")
    @Expose
    private Boolean clearHistory;

    @Ignore
    private Date receivedAt;

    private boolean online;

    public Event() {
    }

    public Event(String type) {
        this.type = type;
    }

    public Event(boolean online) {
        this.online = online;
        setType(EventType.CONNECTION_CHANGED);
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isChannelEvent() {
        return cid != null && !cid.equals("*");
    }

    public String getCid() {
        return cid;
    }

    public EventType getType() {
        return EventType.findByString(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType(EventType type) {
        this.type = type.label;
    }

    public User getUser() {
        return user;
    }

    public User getMe() {
        return me;
    }

    public boolean isAnonymous() {
        if (me != null) {
            return me.getId().equals("!anon");
        }
        return true;
    }

    public Member getMember() {
        return member;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Reaction getReaction() {
        return reaction;
    }

    public Channel getChannel() {
        return channel;
    }

    public Number getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public Number getUnreadChannels() {
        return unreadChannels;
    }

    public Number getWatcherCount() {
        return watcherCount;
    }

    public Date getCreatedAt() {
        return createdAt;
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
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Boolean getClearHistory() {
        return clearHistory;
    }

    public void setClearHistory(Boolean clearHistory) {
        this.clearHistory = clearHistory;
    }
}
