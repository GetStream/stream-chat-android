package io.getstream.chat.android.client.notifications.handler

import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.PushMessage

public interface NotificationHandler {
    public val config: NotificationConfig

    /**
     * Handles showing notification after receiving [NewMessageEvent] from other users.
     * Default implementation loads necessary data and displays notification even if app is in foreground.
     *
     * @return False if notification should be handled internally.
     */
    public fun onChatEvent(event: NewMessageEvent): Boolean {
        return true
    }

    /**
     * Handles showing notification after receiving [PushMessage].
     * Default implementation loads necessary data from the server and shows notification if application is not in foreground.
     *
     * @return False if remote message should be handled internally.
     */
    public fun onPushMessage(message: PushMessage): Boolean {
        return false
    }

    /**
     * Show a notification for the given [channel] and [message]
     *
     * @param channel where the new message was posted
     * @param message was received
     */
    public fun showNotification(channel: Channel, message: Message)

    /**
     * Dismiss notifications from a given [channelType] and [channelId].
     *
     * @param channelType String that represent the channel type of the channel you want to dismiss notifications.
     * @param channelId String that represent the channel id of the channel you want to dismiss notifications.
     */
    public fun dismissChannelNotifications(channelType: String, channelId: String)

    /**
     * Dismiss all notifications.
     */
    public fun dismissAllNotifications()
}
