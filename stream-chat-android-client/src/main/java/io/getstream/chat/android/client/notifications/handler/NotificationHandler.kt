/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.notifications.handler

import io.getstream.android.push.permissions.NotificationPermissionStatus
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationReminderDueEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage

/**
 * Handler responsible for showing and dismissing notification.
 * Implement this interface and use [ChatClient.Builder.notifications] if you want to customize default behavior
 *
 * @see [MessagingStyleNotificationHandler]
 * @see [ChatNotificationHandler]
 */
public interface NotificationHandler {

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
     * Handles showing notification after receiving [NotificationReminderDueEvent].
     * Default implementation of the method ignores the event, because the same reminder due is also delivered via a
     * push notification.
     *
     * @param event The [NotificationReminderDueEvent] to handle.
     * @return True if the event was handled in the method, false if it should be handled internally.
     */
    public fun onNotificationReminderDueEvent(event: NotificationReminderDueEvent): Boolean {
        return true
    }

    /**
     * Handles showing notification after receiving [PushMessage].
     * Default implementation loads necessary data from the server and shows notification if application is not in
     * foreground.
     *
     * @return False if remote message should be handled internally.
     */
    public fun onPushMessage(message: PushMessage): Boolean {
        return false
    }

    /**
     * Show a notification for the given [ChatNotification].
     *
     * @param notification The notification to be shown.
     */
    public fun showNotification(notification: ChatNotification) {
        when (notification) {
            is ChatNotification.MessageNew ->
                showNotification(notification.channel, notification.message)
            is ChatNotification.NotificationReminderDue ->
                showNotification(notification.channel, notification.message)
        }
    }

    /**
     * Show a notification for the given [channel] and [message]
     *
     * @param channel where the new message was posted
     * @param message was received
     */
    @Deprecated("Use showNotification(notification: ChatNotification) instead.")
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

    /**
     * Handles [android.Manifest.permission.POST_NOTIFICATIONS] permission lifecycle.
     *
     * @param status Represents current [android.Manifest.permission.POST_NOTIFICATIONS] permission status.
     */
    public fun onNotificationPermissionStatus(status: NotificationPermissionStatus)
}
