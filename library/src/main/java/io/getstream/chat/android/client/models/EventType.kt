package io.getstream.chat.android.client.models

/**
 * https://getstream.io/chat/docs/js/#event_object
 */
object EventType {

    /**
     * Remote
     */
    const val USER_PRESENCE_CHANGED = "user.presence.changed"
    const val USER_WATCHING_START = "user.watching.start"
    const val USER_WATCHING_STOP = "user.watching.stop"
    const val USER_UPDATED = "user.updated"
    const val USER_BANNED = "user.banned"
    const val USER_DELETED = "user.deleted"
    const val USER_MUTED = "user.muted"
    const val USER_UNMUTED = "user.unmuted"
    const val USER_UNBANNED = "user.unbanned"
    const val TYPING_START = "typing.start"
    const val TYPING_STOP = "typing.stop"
    const val MESSAGE_NEW = "message.new"
    const val MESSAGE_UPDATED = "message.updated"
    const val MESSAGE_DELETED = "message.deleted"
    const val MESSAGE_READ = "message.read"
    const val REACTION_NEW = "reaction.new"
    const val REACTION_DELETED = "reaction.deleted"
    const val REACTION_UPDATED = "reaction.updated"
    const val MEMBER_ADDED = "member.added"
    const val MEMBER_REMOVED = "member.removed"
    const val MEMBER_UPDATED = "member.updated"
    const val CHANNEL_CREATED = "channel.created"
    const val CHANNEL_UPDATED = "channel.updated"
    const val CHANNEL_HIDDEN = "channel.hidden"
    const val CHANNEL_MUTED = "channel.muted"
    const val CHANNEL_UNMUTED = "channel.unmuted"
    const val CHANNEL_DELETED = "channel.deleted"
    const val CHANNEL_VISIBLE = "channel.visible"
    const val CHANNEL_TRUNCATED = "channel.truncated"
    const val HEALTH_CHECK = "health.check"
    const val NOTIFICATION_MESSAGE_NEW = "notification.message_new"
    const val NOTIFICATION_CHANNEL_TRUNCATED = "notification.channel_truncated"
    const val NOTIFICATION_CHANNEL_DELETED = "notification.channel_deleted"
    const val NOTIFICATION_MARK_READ = "notification.mark_read"
    const val NOTIFICATION_INVITED = "notification.invited"
    const val NOTIFICATION_INVITE_ACCEPTED = "notification.invite_accepted"
    const val NOTIFICATION_ADDED_TO_CHANNEL = "notification.added_to_channel"
    const val NOTIFICATION_REMOVED_FROM_CHANNEL = "notification.removed_from_channel"
    const val NOTIFICATION_MUTES_UPDATED = "notification.mutes_updated"
    const val NOTIFICATION_CHANNEL_MUTES_UPDATED = "notification.channel_mutes_updated"

    /**
     * Local
     */
    const val CONNECTION_CONNECTING = "connection.connecting"
    const val CONNECTION_DISCONNECTED = "connection.disconnected"
    const val CONNECTION_ERROR = "connection.error"
    /**
     * Unknown
     */
    const val UNKNOWN = "unknown_event"
}
