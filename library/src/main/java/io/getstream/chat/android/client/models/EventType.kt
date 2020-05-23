package io.getstream.chat.android.client.models


/**
 * https://getstream.io/chat/docs/js/#event_object
 */
object EventType {

    /**
     * Remote
     */
    val USER_PRESENCE_CHANGED = "user.presence.changed"
    val USER_WATCHING_START = "user.watching.start"
    val USER_WATCHING_STOP = "user.watching.stop"
    val USER_UPDATED = "user.updated"
    val USER_BANNED = "user.banned"
    val USER_UNBANNED = "user.unbanned"
    val TYPING_START = "typing.start"
    val TYPING_STOP = "typing.stop"
    val MESSAGE_NEW = "message.new"
    val MESSAGE_UPDATED = "message.updated"
    val MESSAGE_DELETED = "message.deleted"
    val MESSAGE_READ = "message.read"
    val REACTION_NEW = "reaction.new"
    val REACTION_DELETED = "reaction.deleted"
    val MEMBER_ADDED = "member.added"
    val MEMBER_REMOVED = "member.removed"
    val MEMBER_UPDATED = "member.updated"
    val CHANNEL_UPDATED = "channel.updated"
    val CHANNEL_HIDDEN = "channel.hidden"
    val CHANNEL_DELETED = "channel.deleted"
    val CHANNEL_VISIBLE = "channel.visible"
    val CHANNEL_TRUNCATED = "channel.truncated"
    val HEALTH_CHECK = "health.check"
    val NOTIFICATION_MESSAGE_NEW = "notification.message_new"
    val NOTIFICATION_CHANNEL_TRUNCATED = "notification.channel_truncated"
    val NOTIFICATION_CHANNEL_DELETED = "notification.channel_deleted"
    val NOTIFICATION_MARK_READ = "notification.mark_read"
    val NOTIFICATION_INVITED = "notification.invited"
    val NOTIFICATION_INVITE_ACCEPTED = "notification.invite_accepted"
    val NOTIFICATION_INVITE_REJECTED = "notification.invite_rejected"
    val NOTIFICATION_ADDED_TO_CHANNEL = "notification.added_to_channel"
    val NOTIFICATION_REMOVED_FROM_CHANNEL = "notification.removed_from_channel"
    val NOTIFICATION_MUTES_UPDATED = "notification.mutes_updated"

    /**
     * Local
     */
    val CONNECTION_CONNECTING = "connection.connecting"
    val CONNECTION_DISCONNECTED = "connection.disconnected"
    val CONNECTION_ERROR = "connection.error"
    /**
     * Unknown
     */
    val UNKNOWN = ""

}
