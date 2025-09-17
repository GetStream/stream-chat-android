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
    private val notificationBuilderTransformer:
    (NotificationCompat.Builder, ChatNotification) -> NotificationCompat.Builder,
) {

    /**
     * Creates a unique notification ID based on the provided [ChatNotification].
     */
    internal fun createNotificationId(notification: ChatNotification): Int = when (notification) {
        is ChatNotification.MessageNew ->
            createChannelNotificationId(notification.channel.type, notification.channel.id)
        is ChatNotification.MessageUpdated ->
            "${notification.channel.type}:${notification.channel.id}:${notification.message.id}".hashCode()
        is ChatNotification.ReactionNew ->
            "${notification.message.id}:${notification.reactionUserId}:${notification.type}".hashCode()
        is ChatNotification.NotificationReminderDue ->
            "${notification.channel.type}:${notification.channel.id}:${notification.message.id}".hashCode()
    }

    /**
     * Creates a unique notification ID for a channel based on its type and ID.
     *
     * @param channelType The type of the channel.
     * @param channelId The ID of the channel.
     * @return A unique notification ID for the channel.
     */
    internal fun createChannelNotificationId(channelType: String, channelId: String): Int =
        "$channelType:$channelId".hashCode()

    /**
     * Creates a notification based on the provided [ChatNotification].
     *
     * @return A [Notification] object if the current user is available, otherwise null.
     */
    internal suspend fun createNotification(notification: ChatNotification): Notification? {
        val currentUser = ChatClient.instance().getCurrentUser()
            ?: ChatClient.instance().getStoredUser()
            ?: return null
        val notificationId = createNotificationId(notification)
        // Base builder
        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.stream_ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.stream_ic_notification))
        // Customize the notification based on the type
        when (notification) {
            is ChatNotification.MessageNew -> {
                val channel = notification.channel
                val message = notification.message
                val style = restoreMessagingStyle(channel) ?: createMessagingStyle(currentUser, channel)
                builder
                    .setContentIntent(createContentIntent(notificationId, channel, message))
                    .setStyle(style.addMessage(message.toMessagingStyleMessage(context, currentUser)))
                    .apply { actionsProvider(notificationId, channel, message).forEach(::addAction) }
            }

            is ChatNotification.MessageUpdated -> {
                // Note: Handled the same as MessageNew - perhaps in future we want to differentiate them
                val channel = notification.channel
                val message = notification.message
                val style = restoreMessagingStyle(channel) ?: createMessagingStyle(currentUser, channel)
                builder
                    .setContentIntent(createContentIntent(notificationId, channel, message))
                    .setStyle(style.addMessage(message.toMessagingStyleMessage(context, currentUser)))
                    .apply { actionsProvider(notificationId, channel, message).forEach(::addAction) }
            }

            is ChatNotification.ReactionNew -> {
                builder
                    .setContentTitle(notification.title)
                    .setContentText(notification.body)
                    .setContentIntent(createContentIntent(notificationId, notification.channel, notification.message))
                    .setAutoCancel(true)
            }

            is ChatNotification.NotificationReminderDue -> {
                builder
                    .setContentTitle(context.getString(R.string.stream_chat_notification_reminder_due_title))
                    .setContentText(notificationTextFormatter(currentUser, notification.message))
                    .setContentIntent(createContentIntent(notificationId, notification.channel, notification.message))
                    .setAutoCancel(true)
            }
        }
        return notificationBuilderTransformer(builder, notification).build()
    }

    private fun createContentIntent(notificationId: Int, channel: Channel, message: Message) =
        PendingIntent.getActivity(
            context,
            notificationId,
            newMessageIntent(message, channel),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun restoreMessagingStyle(channel: Channel): NotificationCompat.MessagingStyle? =
        notificationManager.activeNotifications
            .firstOrNull { it.id == createChannelNotificationId(channel.type, channel.id) }
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
