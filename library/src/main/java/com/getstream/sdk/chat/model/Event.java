package com.getstream.sdk.chat.model;

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

    public String getConnection_id() {
        return connection_id;
    }

    public String getCid() {
        return cid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
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

    public static final String user_presence_changed                = "user.presence.changed";
    public static final String user_watching_start                  = "user.watching.start";
    public static final String user_watching_stop                   = "user.watching.stop";
    public static final String user_updated                         = "user.updated";
    public static final String typing_start                         = "typing.start";
    public static final String typing_stop                          = "typing.stop";
    public static final String message_new                          = "message.new";
    public static final String message_updated                      = "message.updated";
    public static final String message_deleted                      = "message.deleted";
    public static final String message_read                         = "message.read";
    public static final String message_reaction                     = "message.reaction";
    public static final String reaction_new                         = "reaction.new";
    public static final String reaction_deleted                     = "reaction.deleted";
    public static final String member_added                         = "member.added";
    public static final String member_removed                       = "member.removed";
    public static final String channel_updated                      = "channel.updated";
    public static final String channel_deleted                      = "channel.deleted";
    public static final String health_check                         = "health.check";
    public static final String connection_changed                   = "connection.changed";
    public static final String connection_recovered                 = "connection.recovered";
    public static final String notification_message_new             = "notification.message_new";
    public static final String notification_mark_read               = "notification.mark_read";
    public static final String notification_invited                 = "notification.invited";
    public static final String notification_invite_accepted         = "notification.invite_accepted";
    public static final String notification_added_to_channel        = "notification.added_to_channel";
    public static final String notification_removed_from_channel    = "notification.removed_from_channel";
}
