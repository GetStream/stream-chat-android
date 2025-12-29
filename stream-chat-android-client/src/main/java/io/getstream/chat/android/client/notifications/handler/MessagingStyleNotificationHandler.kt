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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.content.edit
import io.getstream.android.push.permissions.NotificationPermissionHandler
import io.getstream.android.push.permissions.NotificationPermissionStatus
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger

/**
 * Class responsible for displaying chat notifications using [NotificationCompat.MessagingStyle].
 * Notification channel should only be accessed if Build.VERSION.SDK_INT >= Build.VERSION_CODES.O.
 */
@RequiresApi(Build.VERSION_CODES.M)
@Suppress(
    "TooManyFunctions",
    "LongParameterList",
)
internal class MessagingStyleNotificationHandler(
    private val context: Context,
    private val newMessageIntent: (message: Message, channel: Channel) -> Intent,
    private val notificationChannel: () -> NotificationChannel,
    private val userIconBuilder: UserIconBuilder,
    private val permissionHandler: NotificationPermissionHandler?,
    private val notificationTextFormatter: (currentUser: User?, message: Message) -> CharSequence,
    private val actionsProvider: (notificationId: Int, channel: Channel, message: Message) -> List<Action>,
    notificationBuilderTransformer: (NotificationCompat.Builder, ChatNotification) -> NotificationCompat.Builder,
    private val onNewPushMessage: (pushMessage: PushMessage) -> Boolean,
) : NotificationHandler {

    private val logger by taggedLogger("Chat:MsnHandler")

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    private val notificationManager: NotificationManager by lazy {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).also { notificationManager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(notificationChannel())
            }
        }
    }

    private val factory: MessagingStyleNotificationFactory by lazy {
        MessagingStyleNotificationFactory(
            context = context,
            notificationManager = notificationManager,
            notificationChannelId = getNotificationChannelId(),
            userIconBuilder = userIconBuilder,
            newMessageIntent = newMessageIntent,
            notificationTextFormatter = notificationTextFormatter,
            actionsProvider = actionsProvider,
            notificationBuilderTransformer = notificationBuilderTransformer,
        )
    }

    override fun onNotificationPermissionStatus(status: NotificationPermissionStatus) {
        when (status) {
            NotificationPermissionStatus.REQUESTED -> permissionHandler?.onPermissionRequested()
            NotificationPermissionStatus.GRANTED -> permissionHandler?.onPermissionGranted()
            NotificationPermissionStatus.DENIED -> permissionHandler?.onPermissionDenied()
            NotificationPermissionStatus.RATIONALE_NEEDED -> permissionHandler?.onPermissionRationale()
        }
    }

    override fun showNotification(notification: ChatNotification) {
        logger.d { "[showNotification] notification: $notification" }
        showNotificationInternal(notification)
    }

    override fun showNotification(channel: Channel, message: Message) {
        logger.d { "[showNotification] channel.cid: ${channel.cid}, message.cid: ${message.cid}" }
        // Only possible type is message.new
        showNotificationInternal(ChatNotification.MessageNew(channel, message))
    }

    override fun dismissChannelNotifications(channelType: String, channelId: String) {
        val notificationId = factory.createChannelNotificationId(channelType, channelId)
        dismissNotification(notificationId)
    }

    override fun dismissAllNotifications() {
        getShownNotifications().forEach(::dismissNotification)
    }

    override fun onPushMessage(message: PushMessage): Boolean = onNewPushMessage(message)

    private fun showNotificationInternal(chatNotification: ChatNotification) {
        ChatClient.instance().launch {
            val notificationId = factory.createNotificationId(chatNotification)
            val notification = factory.createNotification(chatNotification)
            if (notification != null) {
                addNotificationId(notificationId)
                notificationManager.notify(notificationId, notification)
            } else {
                logger.w { "[showNotificationInternal] Failed to create notification for: $chatNotification" }
            }
        }
    }

    private fun dismissNotification(notificationId: Int) {
        removeNotificationId(notificationId)
        notificationManager.cancel(notificationId)
    }

    private fun addNotificationId(notificationId: Int) {
        sharedPreferences.edit {
            putStringSet(KEY_NOTIFICATIONS_SHOWN, (getShownNotifications() + notificationId).map(Int::toString).toSet())
        }
    }

    private fun removeNotificationId(notificationId: Int) {
        sharedPreferences.edit {
            putStringSet(KEY_NOTIFICATIONS_SHOWN, (getShownNotifications() - notificationId).map(Int::toString).toSet())
        }
    }

    private fun getShownNotifications(): Set<Int> =
        sharedPreferences.getStringSet(KEY_NOTIFICATIONS_SHOWN, null).orEmpty().map(String::toInt).toSet()

    private fun getNotificationChannelId(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel().id
        } else {
            ""
        }
    }

    private companion object {
        private const val SHARED_PREFERENCES_NAME = "stream_notifications.sp"
        private const val KEY_NOTIFICATIONS_SHOWN = "KEY_NOTIFICATIONS_SHOWN"
    }
}
