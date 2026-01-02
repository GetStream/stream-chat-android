/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message

/**
 * Factory for creating [NotificationCompat.Action] instances used in chat push notifications.
 *
 * This factory provides convenient methods to create common notification actions such as
 * "Mark as Read" and "Reply" with sensible defaults, while allowing full customization.
 *
 * @see NotificationCompat.Action
 */
public object NotificationActionsFactory {

    /**
     * Creates a "Mark as Read" notification action.
     *
     * When triggered, this action marks the specified message as read in the channel.
     *
     * @param context The [Context] used to access resources and create the pending intent.
     * @param notificationId The unique identifier for the notification. Used for the pending intent request code.
     * @param channel The [Channel] containing the message to mark as read.
     * @param message The [Message] to mark as read when the action is triggered.
     * @param icon The drawable resource ID for the action icon. Defaults to [android.R.drawable.ic_menu_view].
     * @param title The label text displayed on the action button. Defaults to the localized "Mark as read" string.
     * @param pendingIntent The [PendingIntent] to execute when the action is triggered. Defaults to a broadcast intent
     * which will mark the channel as read.
     * @return A [NotificationCompat.Action] configured for marking messages as read.
     */
    public fun createMarkReadAction(
        context: Context,
        notificationId: Int,
        channel: Channel,
        message: Message,
        @DrawableRes icon: Int = android.R.drawable.ic_menu_view,
        title: String = context.getString(R.string.stream_chat_notification_read),
        pendingIntent: PendingIntent = createMarkReadPendingIntent(context, notificationId, channel, message),
    ): NotificationCompat.Action {
        return NotificationMessageReceiver.createMarkReadAction(
            context = context,
            notificationId = notificationId,
            channel = channel,
            message = message,
            icon = icon,
            title = title,
            pendingIntent = pendingIntent,
        )
    }

    /**
     * Creates a "Reply" notification action with inline reply support.
     *
     * This action displays an inline text input field (on supported devices) allowing users
     * to reply directly from the notification without opening the app.
     *
     * @param context The [Context] used to access resources and create the pending intent.
     * @param notificationId The unique identifier for the notification. Used for the pending intent request code.
     * @param channel The [Channel] to send the reply message to.
     * @param icon The drawable resource ID for the action icon. Defaults to [android.R.drawable.ic_menu_send].
     * @param title The label text displayed on the action button. Defaults to the localized "Reply" string.
     * @param hint The placeholder text shown in the inline reply input field. Defaults to the localized
     * "Type a message" string.
     * @param pendingIntent The [PendingIntent] to execute when the reply is submitted. Defaults to a broadcast intent
     * which sends a message in the channel.
     * @return A [NotificationCompat.Action] configured for inline reply.
     */
    public fun createReplyAction(
        context: Context,
        notificationId: Int,
        channel: Channel,
        @DrawableRes icon: Int = android.R.drawable.ic_menu_send,
        title: String = context.getString(R.string.stream_chat_notification_reply),
        hint: String = context.getString(R.string.stream_chat_notification_type_hint),
        pendingIntent: PendingIntent = createReplyPendingIntent(context, notificationId, channel),
    ): NotificationCompat.Action {
        return NotificationMessageReceiver.createReplyAction(
            context = context,
            notificationId = notificationId,
            channel = channel,
            icon = icon,
            title = title,
            hint = hint,
            pendingIntent = pendingIntent,
        )
    }

    private fun createMarkReadPendingIntent(
        context: Context,
        notificationId: Int,
        channel: Channel,
        message: Message,
    ): PendingIntent {
        return NotificationMessageReceiver.createMarkReadPendingIntent(
            context = context,
            notificationId = notificationId,
            channel = channel,
            message = message,
        )
    }

    private fun createReplyPendingIntent(
        context: Context,
        notificationId: Int,
        channel: Channel,
    ): PendingIntent {
        return NotificationMessageReceiver.createReplyPendingIntent(
            context = context,
            notificationId = notificationId,
            channel = channel,
        )
    }
}
