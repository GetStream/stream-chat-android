/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.R
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * Factory for creating notifications using the MessagingStyle.
 *
 * @param context The context to use for creating notifications.
 * @param notificationManager The NotificationManager to manage notifications.
 * @param notificationChannelId The ID of the notification channel to use.
 * @param userIconBuilder Builder for creating user icons.
 * @param newMessageIntent Function to create an intent for new messages.
 * @param notificationTextFormatter Function to format the text of the notification.
 * @param actionsProvider Function to provide actions for the notification.
 */
@Suppress("LongParameterList")
@RequiresApi(Build.VERSION_CODES.M)
internal class MessagingStyleNotificationFactory(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val notificationChannelId: String,
    private val userIconBuilder: UserIconBuilder,
    private val newMessageIntent: (message: Message, channel: Channel) -> Intent,
    private val notificationTextFormatter: (currentUser: User?, message: Message) -> CharSequence,
    private val actionsProvider: (notificationId: Int, channel: Channel, message: Message) -> List<Action>,
) {

    /**
     * Creates a unique notification ID based on the type, channel type, channel ID, and message ID.
     *
     * @param type The type of notification. See [NotificationType].
     * @param channelType The type of the channel.
     * @param channelId The ID of the channel.
     * @param messageId The ID of the message.
     */
    internal fun createNotificationId(
        type: String,
        channelType: String,
        channelId: String,
        messageId: String,
    ): Int = when (type) {
        NotificationType.NOTIFICATION_REMINDER_DUE -> "$channelType:$channelId:$messageId".hashCode()
        else -> "$channelType:$channelId".hashCode()
    }

    /**
     * Creates a notification based on the provided type, channel, and message.
     *
     * @param type The type of notification to create. See [NotificationType].
     * @param channel The channel associated with the notification.
     * @param message The message associated with the notification.
     * @return A [Notification] object if the current user is available, otherwise null.
     */
    internal suspend fun createNotification(type: String, channel: Channel, message: Message): Notification? {
        val currentUser = ChatClient.instance().getCurrentUser()
            ?: ChatClient.instance().getStoredUser()
            ?: return null
        val notificationId = createNotificationId(
            type = type,
            channelType = channel.type,
            channelId = channel.id,
            messageId = message.id,
        )
        // Base builder
        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.stream_ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.stream_ic_notification))
            .setContentIntent(createContentIntent(notificationId, channel, message))
        // Customize the notification based on the type
        when (type) {
            NotificationType.NOTIFICATION_REMINDER_DUE -> {
                builder
                    .setContentTitle(context.getString(R.string.stream_chat_notification_reminder_due_title))
                    .setContentText(notificationTextFormatter(currentUser, message))
                    .setAutoCancel(true)
            }

            else -> {
                val style = restoreMessagingStyle(type, channel, message)
                    ?: createMessagingStyle(currentUser, channel)
                builder
                    .setStyle(style.addMessage(message.toMessagingStyleMessage(context, currentUser)))
                    .apply { actionsProvider(notificationId, channel, message).forEach(::addAction) }
            }
        }
        return builder.build()
    }

    private fun createContentIntent(notificationId: Int, channel: Channel, message: Message) =
        PendingIntent.getActivity(
            context,
            notificationId,
            newMessageIntent(message, channel),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun restoreMessagingStyle(
        type: String,
        channel: Channel,
        message: Message,
    ): NotificationCompat.MessagingStyle? =
        notificationManager.activeNotifications
            .firstOrNull { it.id == createNotificationId(type, channel.type, channel.id, message.id) }
            ?.notification
            ?.let(NotificationCompat.MessagingStyle::extractMessagingStyleFromNotification)

    private suspend fun createMessagingStyle(currentUser: User, channel: Channel): NotificationCompat.MessagingStyle =
        NotificationCompat.MessagingStyle(currentUser.toPerson(context))
            .setConversationTitle(channel.name)
            .setGroupConversation(channel.name.isNotBlank())

    private suspend fun Message.toMessagingStyleMessage(
        context: Context,
        currentUser: User?,
    ): NotificationCompat.MessagingStyle.Message {
        return NotificationCompat.MessagingStyle.Message(
            notificationTextFormatter(currentUser, this),
            timestamp,
            person(context),
        )
    }

    private suspend fun Message.person(context: Context): Person = user.toPerson(context)

    private val Message.timestamp: Long
        get() = (createdAt ?: createdLocallyAt ?: Date()).time

    private suspend fun User.toPerson(context: Context): Person =
        Person.Builder()
            .setKey(id)
            .setName(personName(context))
            .setIcon(userIconBuilder.buildIcon(this))
            .build()

    private fun User.personName(context: Context): String =
        name.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.stream_chat_notification_empty_username)
}
