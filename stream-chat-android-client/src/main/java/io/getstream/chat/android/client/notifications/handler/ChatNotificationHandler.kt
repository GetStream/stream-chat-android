package io.getstream.chat.android.client.notifications.handler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.FirebaseMessageParser
import io.getstream.chat.android.client.notifications.FirebaseMessageParserImpl
import io.getstream.chat.android.client.notifications.NotificationLoadDataListener
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver

public open class ChatNotificationHandler @JvmOverloads constructor(
    protected val context: Context,
    public val config: NotificationConfig = NotificationConfig(),
) {
    private val logger = ChatLogger.get("ChatNotificationHandler")
    private val firebaseMessageParserImpl: FirebaseMessageParser by lazy { FirebaseMessageParserImpl(config) }

    public open fun onChatEvent(event: ChatEvent): Boolean {
        return false
    }

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

    // Deprecate 4 functions
    public open fun getSmallIcon(): Int = config.smallIcon
    public open fun getFirebaseMessageIdKey(): String = config.firebaseMessageIdKey
    public open fun getFirebaseChannelIdKey(): String = config.firebaseChannelIdKey
    public open fun getFirebaseChannelTypeKey(): String = config.firebaseChannelTypeKey

    public open fun getErrorCaseNotificationTitle(): String =
        context.getString(config.errorCaseNotificationTitle)

    public open fun getErrorCaseNotificationContent(): String =
        context.getString(config.errorCaseNotificationContent)

    public open fun buildErrorCaseNotification(): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = getNotificationBuilder()
        val intent = PendingIntent.getActivity(
            context,
            getRequestCode(),
            getErrorCaseIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return notificationBuilder.setContentTitle(getErrorCaseNotificationTitle())
            .setContentText(getErrorCaseNotificationContent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShowWhen(true)
            .setContentIntent(intent)
            .setSound(defaultSoundUri)
            .build()
    }

    public open fun buildNotification(
        notificationId: Int,
        channelName: String,
        messageText: String,
        messageId: String,
        channelType: String,
        channelId: String,
    ): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = getNotificationBuilder()

        val intent = PendingIntent.getActivity(
            context,
            getRequestCode(),
            getNewMessageIntent(messageId, channelType, channelId),
            PendingIntent.FLAG_UPDATE_CURRENT,
        )

        notificationBuilder.setContentTitle(channelName)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShowWhen(true)
            .setContentIntent(intent)
            .setSound(defaultSoundUri)

        notificationBuilder.apply {
            addAction(
                getReadAction(
                    prepareActionPendingIntent(
                        notificationId,
                        messageId,
                        channelId,
                        channelType,
                        NotificationMessageReceiver.ACTION_READ,
                    )
                )
            )
            addAction(
                getReplyAction(
                    prepareActionPendingIntent(
                        notificationId,
                        messageId,
                        channelId,
                        channelType,
                        NotificationMessageReceiver.ACTION_REPLY,
                    )
                )
            )
        }

        return notificationBuilder.build()
    }

    private fun getRequestCode(): Int {
        return 1220999987
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

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, getNotificationChannelId())
            .setAutoCancel(true)
            .setSmallIcon(getSmallIcon())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
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

    public open fun getFirebaseInstanceId(): FirebaseInstanceId? =
        if (config.useProvidedFirebaseInstance && FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseInstanceId.getInstance()
        } else {
            null
        }
}
