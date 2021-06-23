package io.getstream.chat.android.client.notifications.handler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.FirebaseMessageParser
import io.getstream.chat.android.client.notifications.FirebaseMessageParserImpl
import io.getstream.chat.android.client.notifications.NotificationLoadDataListener
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver

/**
 * Class responsible for handling chat notifications.
 */
public open class ChatNotificationHandler @JvmOverloads constructor(
    protected val context: Context,
    public val config: NotificationConfig = NotificationConfig(),
) {
    private val firebaseMessageParserImpl: FirebaseMessageParser by lazy { FirebaseMessageParserImpl(config) }

    /**
     * Handles showing notification after receiving [NewMessageEvent] from other users.
     * Default implementation loads necessary data and displays notification even if app is in foreground.
     *
     * @return false if notification should be handled internally
     */
    public open fun onChatEvent(event: NewMessageEvent): Boolean {
        return true
    }

    /**
     * Handles showing notification after receiving [RemoteMessage].
     * Default implementation loads necessary data from the server and shows notification if application is not in foreground.
     *
     * @return false if remote message should be handled internally
     */
    public open fun onFirebaseMessage(message: RemoteMessage): Boolean {
        return false
    }

    public open fun getDeviceRegisteredListener(): DeviceRegisteredListener? {
        return null
    }

    public open fun getDataLoadListener(): NotificationLoadDataListener? {
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public open fun createNotificationChannel(): NotificationChannel {
        return NotificationChannel(
            getNotificationChannelId(),
            getNotificationChannelName(),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            setShowBadge(true)
            importance = NotificationManager.IMPORTANCE_HIGH
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(
                100,
                200,
                300,
                400,
                500,
                400,
                300,
                200,
                400,
            )
        }
    }

    public open fun getNotificationChannelId(): String = context.getString(config.notificationChannelId)

    public open fun getNotificationChannelName(): String =
        context.getString(config.notificationChannelName)

    @Deprecated(
        message = "Use NotificationConfig.smallIcon instead",
        replaceWith = ReplaceWith("NotificationConfig.smallIcon"),
        level = DeprecationLevel.ERROR,
    )
    public open fun getSmallIcon(): Int = config.smallIcon

    @Deprecated(
        message = "Use NotificationConfig.firebaseMessageIdKey instead",
        replaceWith = ReplaceWith("NotificationConfig.firebaseMessageIdKey"),
        level = DeprecationLevel.ERROR,
    )
    public open fun getFirebaseMessageIdKey(): String = config.firebaseMessageIdKey

    @Deprecated(
        message = "Use NotificationConfig.firebaseChannelIdKey instead",
        replaceWith = ReplaceWith("NotificationConfig.firebaseChannelIdKey"),
        level = DeprecationLevel.ERROR,
    )
    public open fun getFirebaseChannelIdKey(): String = config.firebaseChannelIdKey

    @Deprecated(
        message = "Use NotificationConfig.firebaseChannelTypeKey instead",
        replaceWith = ReplaceWith("NotificationConfig.firebaseChannelTypeKey"),
        level = DeprecationLevel.ERROR,
    )
    public open fun getFirebaseChannelTypeKey(): String = config.firebaseChannelTypeKey

    public open fun getErrorCaseNotificationTitle(): String =
        context.getString(config.errorCaseNotificationTitle)

    public open fun getErrorCaseNotificationContent(): String =
        context.getString(config.errorCaseNotificationContent)

    public open fun buildErrorCaseNotification(): Notification {
        return getNotificationBuilder(
            contentTitle = getErrorCaseNotificationTitle(),
            contentText = getErrorCaseNotificationContent(),
            groupKey = getErrorNotificationGroupKey(),
            intent = getErrorCaseIntent(),
        ).build()
    }

    public open fun buildNotification(
        notificationId: Int,
        channel: Channel,
        message: Message
    ): NotificationCompat.Builder {
        return getNotificationBuilder(
            contentTitle = channel.name,
            contentText = message.text,
            groupKey = getNotificationGroupKey(channelType = channel.type, channelId = channel.id),
            intent = getNewMessageIntent(messageId = message.id, channelType = channel.type, channelId = channel.id),
        ).apply {
            addAction(
                getReadAction(
                    prepareActionPendingIntent(
                        notificationId,
                        message.id,
                        channel.id,
                        channel.type,
                        NotificationMessageReceiver.ACTION_READ,
                    )
                )
            )
            addAction(
                getReplyAction(
                    prepareActionPendingIntent(
                        notificationId,
                        message.id,
                        channel.id,
                        channel.type,
                        NotificationMessageReceiver.ACTION_REPLY,
                    )
                )
            )
        }
    }

    public open fun buildNotificationGroupSummary(channel: Channel, message: Message): NotificationCompat.Builder {
        return getNotificationBuilder(
            contentTitle = channel.name,
            contentText = context.getString(config.notificationGroupSummaryContentText),
            groupKey = getNotificationGroupKey(channelType = channel.type, channelId = channel.id),
            intent = getNewMessageIntent(messageId = message.id, channelType = channel.type, channelId = channel.id),
        ).apply {
            setGroupSummary(true)
        }
    }

    public open fun buildErrorNotificationGroupSummary(): Notification {
        return getNotificationBuilder(
            contentTitle = context.getString(config.errorNotificationGroupSummaryTitle),
            contentText = context.getString(config.errorNotificationGroupSummaryContentText),
            groupKey = getErrorNotificationGroupKey(),
            getErrorCaseIntent(),
        ).apply {
            setGroupSummary(true)
        }.build()
    }

    public open fun getNotificationGroupKey(channelType: String, channelId: String): String {
        return "$channelType:$channelId"
    }

    public open fun getNotificationGroupSummaryId(channelType: String, channelId: String): Int {
        return getNotificationGroupKey(channelType = channelType, channelId = channelId).hashCode()
    }

    public open fun getErrorNotificationGroupKey(): String = ERROR_NOTIFICATION_GROUP_KEY

    public open fun getErrorNotificationGroupSummaryId(): Int = getErrorNotificationGroupKey().hashCode()

    private fun getRequestCode(): Int {
        return System.currentTimeMillis().toInt()
    }

    public open fun getNewMessageIntent(
        messageId: String,
        channelType: String,
        channelId: String,
    ): Intent {
        return context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
    }

    public open fun getErrorCaseIntent(): Intent {
        return context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
    }

    public open fun getFirebaseMessageParser(): FirebaseMessageParser = firebaseMessageParserImpl
    internal fun isValidRemoteMessage(message: RemoteMessage): Boolean =
        getFirebaseMessageParser().isValidRemoteMessage(message)

    private fun getNotificationBuilder(
        contentTitle: String,
        contentText: String,
        groupKey: String,
        intent: Intent,
    ): NotificationCompat.Builder {
        val contentIntent = PendingIntent.getActivity(
            context,
            getRequestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(context, getNotificationChannelId())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setSmallIcon(config.smallIcon)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShowWhen(true)
            .setContentIntent(contentIntent)
            .apply {
                if (config.shouldGroupNotifications) {
                    setGroup(groupKey)
                }
            }
    }

    private fun getReadAction(pendingIntent: PendingIntent): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_view,
            context.getString(R.string.stream_chat_notification_read),
            pendingIntent,
        ).build()
    }

    private fun getReplyAction(replyPendingIntent: PendingIntent): NotificationCompat.Action {
        val remoteInput =
            RemoteInput.Builder(NotificationMessageReceiver.KEY_TEXT_REPLY)
                .setLabel(context.getString(R.string.stream_chat_notification_type_hint))
                .build()
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            context.getString(R.string.stream_chat_notification_reply),
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun prepareActionPendingIntent(
        notificationId: Int,
        messageId: String,
        channelId: String,
        type: String,
        actionType: String,
    ): PendingIntent {
        val notifyIntent = Intent(context, NotificationMessageReceiver::class.java)

        notifyIntent.apply {
            putExtra(NotificationMessageReceiver.KEY_NOTIFICATION_ID, notificationId)
            putExtra(NotificationMessageReceiver.KEY_MESSAGE_ID, messageId)
            putExtra(NotificationMessageReceiver.KEY_CHANNEL_ID, channelId)
            putExtra(NotificationMessageReceiver.KEY_CHANNEL_TYPE, type)
            action = actionType
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    public open fun getFirebaseMessaging(): FirebaseMessaging? =
        if (config.useProvidedFirebaseInstance && FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseMessaging.getInstance()
        } else {
            null
        }

    private companion object {
        private const val ERROR_NOTIFICATION_GROUP_KEY = "error_notification_group_key"
    }
}
