package com.getstream.sdk.chat.enums;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
    USER_PRESENCE_CHANGED("user.presence.changed"),
    USER_WATCHING_START("user.watching.start"),
    USER_WATCHING_STOP("user.watching.stop"),
    USER_UPDATED("user.updated"),
    USER_BANNED("user.banned"),
    USER_UNBANNED("user.unbanned"),
    TYPING_START("typing.start"),
    TYPING_STOP("typing.stop"),
    MESSAGE_NEW("message.new"),
    MESSAGE_UPDATED("message.updated"),
    MESSAGE_DELETED("message.deleted"),
    MESSAGE_READ("message.read"),
    REACTION_NEW("reaction.new"),
    REACTION_DELETED("reaction.deleted"),
    MEMBER_ADDED("member.added"),
    MEMBER_REMOVED("member.removed"),
    MEMBER_UPDATED("member.updated"),
    CHANNEL_UPDATED("channel.updated"),
    CHANNEL_HIDDEN("channel.hidden"),
    CHANNEL_DELETED("channel.deleted"),
    CHANNEL_VISIBLE("channel.visible"),
    HEALTH_CHECK("health.check"),
    CONNECTION_CHANGED("connection.changed"),
    CONNECTION_RECOVERED("connection.recovered"),
    NOTIFICATION_MESSAGE_NEW("notification.message_new"),
    NOTIFICATION_MARK_READ("notification.mark_read"),
    NOTIFICATION_INVITED("notification.invited"),
    NOTIFICATION_INVITE_ACCEPTED("notification.invite_accepted"),
    NOTIFICATION_INVITE_REJECTED("notification.invite_rejected"),
    NOTIFICATION_ADDED_TO_CHANNEL("notification.added_to_channel"),
    NOTIFICATION_REMOVED_FROM_CHANNEL("notification.removed_from_channel"),
    NOTIFICATION_MUTES_UPDATED("notification.mutes_updated"),
    UNKNOWN("");

    private static final Map<String, EventType> lookup = new HashMap<>();

    static {
        for (EventType d : EventType.values()) {
            if (d == EventType.UNKNOWN) {
                continue;
            }
            lookup.put(d.label, d);
        }
    }

    public final String label;

    EventType(String label) {
        this.label = label;
    }

    public static EventType findByString(String value) {
        EventType eventType = EventType.lookup.get(value);
        if (eventType == null) {
            return UNKNOWN;
        }
        return eventType;
    }

}
