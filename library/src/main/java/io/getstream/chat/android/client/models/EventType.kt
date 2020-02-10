package io.getstream.chat.android.client.models


/**
 * https://getstream.io/chat/docs/js/#event_object
 */
enum class EventType(val label: String) {

    /**
     * Remote
     */
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
    NOTIFICATION_MESSAGE_NEW("notification.message_new"),
    NOTIFICATION_MARK_READ("notification.mark_read"),
    NOTIFICATION_INVITED("notification.invited"),
    NOTIFICATION_INVITE_ACCEPTED("notification.invite_accepted"),
    NOTIFICATION_INVITE_REJECTED("notification.invite_rejected"),
    NOTIFICATION_ADDED_TO_CHANNEL("notification.added_to_channel"),
    NOTIFICATION_REMOVED_FROM_CHANNEL("notification.removed_from_channel"),
    NOTIFICATION_MUTES_UPDATED("notification.mutes_updated"),

    /**
     * Local
     */
    CONNECTION_SOCKET_OPEN("connection.socket.open"),
    CONNECTION_SOCKET_CLOSING("connection.socket.closing"),
    CONNECTION_SOCKET_CLOSED("connection.socket.closed"),
    CONNECTION_SOCKET_FAILURE("connection.socket.failure"),
    CONNECTION_CONNECTING("connection.connecting"),
    CONNECTION_RESOLVED("connection.resolved"),
    CONNECTION_CHANGED("connection.changed"),
    CONNECTION_RECOVERED("connection.recovered"),
    CONNECTION_DISCONNECTED("connection.disconnected"),
    CONNECTION_ERROR("connection.error"),
    TOKEN_EXPIRED("token.expired"),
    /**
     * Unknown
     */
    UNKNOWN("");

}
