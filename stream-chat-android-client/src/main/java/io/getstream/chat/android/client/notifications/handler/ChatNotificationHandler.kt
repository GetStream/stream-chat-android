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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import io.getstream.android.push.permissions.NotificationPermissionStatus
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Class responsible for handling chat notifications.
 * Notification channel should only be accessed if Build.VERSION.SDK_INT >= Build.VERSION_CODES.O.
 */
@Suppress("TooManyFunctions")
internal class ChatNotificationHandler(
    private val context: Context,
    private val newMessageIntent: (message: Message, channel: Channel) -> Intent,
    private val notificationChannel: () -> NotificationChannel,
    private val notificationTextFormatter: (currentUser: User?, message: Message) -> CharSequence,
    private val actionsProvider: (notificationId: Int, channel: Channel, message: Message) -> List<Action>,
    private val notificationBuilderTransformer:
    (NotificationCompat.Builder, ChatNotification) -> NotificationCompat.Builder,
    private val currentUserProvider: () -> User? = {
        ChatClient.instance().getCurrentUser() ?: ChatClient.instance().getStoredUser()
    },
) : NotificationHandler {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )
    }
    private val notificationManager: NotificationManager by lazy {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.createNotificationChannel(notificationChannel())
            }
        }
    }

    private fun getNotificationChannelId(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel().id
        } else {
            ""
        }
    }

    override fun onNotificationPermissionStatus(status: NotificationPermissionStatus) { /* no-op */ }

    override fun showNotification(notification: ChatNotification) {
        showNotificationInternal(notification)
    }

    override fun showNotification(channel: Channel, message: Message) {
        // Only possible type is message.new
        showNotificationInternal(ChatNotification.MessageNew(channel, message))
    }

    private fun showNotificationInternal(notification: ChatNotification) {
        when (notification) {
            is ChatNotification.MessageNew -> showMessageNewNotification(notification)
            is ChatNotification.MessageUpdated -> showMessageUpdatedNotification(notification)
            is ChatNotification.ReactionNew -> showReactionNewNotification(notification)
            is ChatNotification.NotificationReminderDue -> showReminderDueNotification(notification)
        }
    }

    private fun showMessageNewNotification(notification: ChatNotification.MessageNew) {
        val (channel, message) = notification
        val notificationId: Int = System.nanoTime().toInt()
        val notificationSummaryId = getNotificationGroupSummaryId(channel.type, channel.id)
        addNotificationId(notificationId, notificationSummaryId)
        val notification = notificationBuilderTransformer(
            buildNotification(notificationId, channel, message),
            notification,
        ).build()
        showNotification(notificationId, notification)
        showNotification(notificationSummaryId, buildNotificationGroupSummary(channel, message).build())
    }

    private fun showMessageUpdatedNotification(notification: ChatNotification.MessageUpdated) {
        // Note: Handled the same as MessageNew - perhaps in future we want to differentiate them
        val (channel, message) = notification
        val notificationId: Int = System.nanoTime().toInt()
        val notificationSummaryId = getNotificationGroupSummaryId(channel.type, channel.id)
        addNotificationId(notificationId, notificationSummaryId)
        val notification = notificationBuilderTransformer(
            buildNotification(notificationId, channel, message),
            notification,
        ).build()
        showNotification(notificationId, notification)
        showNotification(notificationSummaryId, buildNotificationGroupSummary(channel, message).build())
    }

    private fun showReactionNewNotification(notification: ChatNotification.ReactionNew) {
        val notificationId = "${notification.message.id}:${notification.reactionUserId}:${notification.type}".hashCode()
        addNotificationIdWithoutSummary(notificationId)
        val notification = notificationBuilderTransformer(
            buildReactionNewNotification(notification),
            notification,
        ).build()
        showNotification(notificationId, notification)
    }

    private fun showReminderDueNotification(chatNotification: ChatNotification.NotificationReminderDue) {
        val (channel, message) = chatNotification
        val notificationId = "${channel.type}:${channel.id}:${message.id}".hashCode()
        addNotificationIdWithoutSummary(notificationId)
        val notification = notificationBuilderTransformer(
            buildReminderDueNotification(channel, message),
            chatNotification,
        ).build()
        showNotification(notificationId, notification)
    }

    private fun buildNotification(
        notificationId: Int,
        channel: Channel,
        message: Message,
    ): NotificationCompat.Builder {
        val currentUser = currentUserProvider()
        return getNotificationBuilder(
            contentTitle = channel.getNotificationContentTitle(),
            contentText = notificationTextFormatter(currentUser, message),
            groupKey = getNotificationGroupKey(channelType = channel.type, channelId = channel.id),
            intent = getNewMessageIntent(message = message, channel = channel),
        ).apply {
            actionsProvider(notificationId, channel, message).forEach(::addAction)
            setDeleteIntent(NotificationMessageReceiver.createDismissPendingIntent(context, notificationId, channel))
        }
    }

    private fun buildReactionNewNotification(notification: ChatNotification.ReactionNew): NotificationCompat.Builder {
        return getNotificationBuilder(
            contentTitle = notification.title,
            contentText = notification.body,
            groupKey = null,
            intent = getNewMessageIntent(message = notification.message, channel = notification.channel),
        )
    }

    private fun buildReminderDueNotification(channel: Channel, message: Message): NotificationCompat.Builder {
        val currentUser = currentUserProvider()
        return getNotificationBuilder(
            contentTitle = context.getString(R.string.stream_chat_notification_reminder_due_title),
            contentText = notificationTextFormatter(currentUser, message),
            groupKey = null,
            intent = getNewMessageIntent(message = message, channel = channel),
        )
    }

    private fun buildNotificationGroupSummary(channel: Channel, message: Message): NotificationCompat.Builder {
        return getNotificationBuilder(
            contentTitle = channel.getNotificationContentTitle(),
            contentText = context.getString(R.string.stream_chat_notification_group_summary_content_text),
            groupKey = getNotificationGroupKey(channelType = channel.type, channelId = channel.id),
            intent = getNewMessageIntent(message = message, channel = channel),
        ).apply {
            setGroupSummary(true)
        }
    }

    private fun getNotificationGroupKey(channelType: String, channelId: String): String {
        return "$channelType:$channelId"
    }

    private fun getNotificationGroupSummaryId(channelType: String, channelId: String): Int {
        return getNotificationGroupKey(channelType = channelType, channelId = channelId).hashCode()
    }

    private fun getRequestCode(): Int {
        return System.currentTimeMillis().toInt()
    }

    private fun getNewMessageIntent(message: Message, channel: Channel): Intent = newMessageIntent(message, channel)

    /**
     * Dismiss notifications from a given [channelType] and [channelId].
     *
     * @param channelType String that represent the channel type of the channel you want to dismiss notifications.
     * @param channelId String that represent the channel id of the channel you want to dismiss notifications.
     */
    override fun dismissChannelNotifications(channelType: String, channelId: String) {
        dismissSummaryNotification(getNotificationGroupSummaryId(channelType, channelId))
    }

    /**
     * Dismiss all notifications.
     */
    override fun dismissAllNotifications() {
        getNotificationSummaryIds().forEach(::dismissSummaryNotification)
        // Dismiss any non-grouped notifications
        getNotificationWithoutSummaryIds().forEach { notificationManager.cancel(it) }
        clearNotificationWithoutSummaryIds()
    }

    private fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    private fun getNotificationBuilder(
        contentTitle: String,
        contentText: CharSequence,
        groupKey: String?,
        intent: Intent,
    ): NotificationCompat.Builder {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            getRequestCode(),
            intent,
            flags,
        )

        return NotificationCompat.Builder(context, getNotificationChannelId())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.stream_ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.stream_ic_notification))
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShowWhen(true)
            .setContentIntent(contentIntent)
            .setGroup(groupKey)
    }

    private fun Channel.getNotificationContentTitle(): String =
        name.takeIf { it.isNotEmpty() }
            ?: getMemberNamesWithoutCurrentUser()
            ?: context.getString(R.string.stream_chat_notification_title)

    private fun Channel.getMemberNamesWithoutCurrentUser(): String? = getUsersExcludingCurrent()
        .joinToString { it.name }
        .takeIf { it.isNotEmpty() }

    private fun dismissSummaryNotification(notificationSummaryId: Int) {
        getAssociatedNotificationIds(notificationSummaryId).forEach {
            notificationManager.cancel(it)
            removeNotificationId(it)
        }
        notificationManager.cancel(notificationSummaryId)
        sharedPreferences.edit { remove(getNotificationSummaryIdKey(notificationSummaryId)) }
    }

    private fun addNotificationId(notificationId: Int, notificationSummaryId: Int) {
        sharedPreferences.edit {
            putInt(getNotificationIdKey(notificationId), notificationSummaryId)
            putStringSet(
                KEY_NOTIFICATION_SUMMARY_IDS,
                (getNotificationSummaryIds() + notificationSummaryId).map(Int::toString).toSet(),
            )
            putStringSet(
                getNotificationSummaryIdKey(notificationSummaryId),
                (getAssociatedNotificationIds(notificationSummaryId) + notificationId).map(Int::toString).toSet(),
            )
        }
    }

    private fun addNotificationIdWithoutSummary(notificationId: Int) {
        sharedPreferences.edit {
            putStringSet(
                KEY_NOTIFICATION_WITHOUT_SUMMARY_IDS,
                (getNotificationWithoutSummaryIds() + notificationId).map(Int::toString).toSet(),
            )
        }
    }

    private fun removeNotificationId(notificationId: Int) {
        sharedPreferences.edit {
            val notificationSummaryId = getAssociatedNotificationSummaryId(notificationId)
            remove(getNotificationIdKey(notificationId))
            putStringSet(
                getNotificationSummaryIdKey(notificationSummaryId),
                (getAssociatedNotificationIds(notificationSummaryId) - notificationId).map(Int::toString).toSet(),
            )
        }
    }

    private fun getNotificationSummaryIds(): Set<Int> =
        sharedPreferences.getStringSet(KEY_NOTIFICATION_SUMMARY_IDS, null).orEmpty().map(String::toInt).toSet()

    private fun getAssociatedNotificationSummaryId(notificationId: Int): Int =
        sharedPreferences.getInt(getNotificationIdKey(notificationId), 0)

    private fun getAssociatedNotificationIds(notificationSummaryId: Int): Set<Int> =
        sharedPreferences.getStringSet(getNotificationSummaryIdKey(notificationSummaryId), null).orEmpty()
            .map(String::toInt).toSet()

    private fun getNotificationIdKey(notificationId: Int) = KEY_PREFIX_NOTIFICATION_ID + notificationId
    private fun getNotificationSummaryIdKey(notificationSummaryId: Int) =
        KEY_PREFIX_NOTIFICATION_SUMMARY_ID + notificationSummaryId

    private fun getNotificationWithoutSummaryIds(): Set<Int> =
        sharedPreferences.getStringSet(KEY_NOTIFICATION_WITHOUT_SUMMARY_IDS, null).orEmpty().map(String::toInt).toSet()

    private fun clearNotificationWithoutSummaryIds() {
        sharedPreferences.edit { remove(KEY_NOTIFICATION_WITHOUT_SUMMARY_IDS) }
    }

    private companion object {
        private const val SHARED_PREFERENCES_NAME = "stream_notifications.sp"
        private const val KEY_PREFIX_NOTIFICATION_ID = "nId-"
        private const val KEY_PREFIX_NOTIFICATION_SUMMARY_ID = "nSId-"
        private const val KEY_NOTIFICATION_SUMMARY_IDS = "notification_summary_ids"
        private const val KEY_NOTIFICATION_WITHOUT_SUMMARY_IDS = "notification_without_summary_ids"
    }
}
