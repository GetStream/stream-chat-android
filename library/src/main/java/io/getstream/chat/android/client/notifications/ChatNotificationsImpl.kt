package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.ChatNotification
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class ChatNotificationsImpl(
    private val config: ChatNotificationConfig,
    private val client: ChatApi,
    private val context: Context
) : ChatNotifications {

    private val TAG = ChatNotifications::class.java.simpleName
    private val notificationsMap = mutableMapOf<String, ChatNotification>()
    private val logger: ChatLogger? = ChatLogger.instance


    override fun setFirebaseToken(firebaseToken: String) {
        logger?.logI(TAG, "setFirebaseToken: $firebaseToken")

        client.addDevice(firebaseToken).enqueue { result ->
            if (result.isSuccess) {
                config.getDeviceRegisteredListener()?.onDeviceRegisteredSuccess()
                logger?.logI(TAG, "DeviceRegisteredSuccess")
            } else {
                config.getDeviceRegisteredListener()?.onDeviceRegisteredError(result.error())
                logger?.logE(TAG, "Error register device ${result.error().message}")
            }
        }
    }

    override fun onReceiveFirebaseMessage(remoteMessage: RemoteMessage) {
        val payload: Map<String, String> = remoteMessage.data
        logger?.logI(TAG, "onLoadMessageFail: $remoteMessage data: $payload")

        handleRemoteMessage(remoteMessage)
    }

    override fun onReceiveWebSocketEvent(event: ChatEvent) {
        logger?.logI(TAG, "onReceiveWebSocketEvent: $event")

        handleEvent(event)
    }

    private fun handleRemoteMessage(remoteMessage: RemoteMessage) {
        val messageId = remoteMessage.data[config.getFirebaseMessageKey()]

        if (checkSentNotificationWithId(messageId)) {
            if (messageId != null && messageId.isNotEmpty()) {
                val notificationModel = ChatNotification(
                    System.currentTimeMillis().toInt(),
                    remoteMessage,
                    null
                )
                notificationsMap[messageId] = notificationModel
                loadMessage(context, messageId)
            } else {
                logger?.logE(TAG, "RemoteMessage: messageId = $messageId")
            }
        }
    }

    private fun handleEvent(event: ChatEvent) {

        if (event is NewMessageEvent) {

            if (checkSentNotificationWithId(event.message.id)) {
                val currentTime = System.currentTimeMillis().toInt()
                val notificationModel = ChatNotification(currentTime, null, event)
                notificationsMap[event.message.id] = notificationModel
                loadMessage(context, event.message.id)
            } else {
                logger?.logI(TAG, "Notification with id:${event.message.id} already showed")
            }
        }
    }

    private fun checkSentNotificationWithId(messageId: String?) =
        notificationsMap[messageId] == null

    private fun loadMessage(
        context: Context,
        messageId: String
    ) {
        client.getMessage(messageId).enqueue { result ->
            if (result.isSuccess) {
                config.getFailMessageListener()?.onLoadMessageSuccess(result.data())
                onMessageLoaded(context, result.data())
            } else {
                logger?.logE(TAG, "Can\'t load message. Error: ${result.error().message}")
                showDefaultNotification(context, messageId)
                config.getFailMessageListener()?.onLoadMessageFail(messageId)
            }
        }
    }

    private fun onMessageLoaded(
        context: Context,
        message: Message
    ) {
        val type: String = message.channel.type

        val messageId: String = message.id
        val channelId: String = message.channel.id

        notificationsMap[message.id]?.let { notificationItem ->
            notificationItem.channelName =
                message.channel.extraData.getOrDefault("name", "").toString()
            notificationItem.messageText = message.text
            notificationItem.pendingReplyIntent = preparePendingIntent(
                context = context,
                messageId = messageId,
                channelId = channelId,
                type = type,
                notificationId = notificationItem.notificationId,
                actionType = NotificationMessageReceiver.ACTION_REPLY
            )
            notificationItem.pendingReadIntent = preparePendingIntent(
                context = context,
                messageId = messageId,
                channelId = channelId,
                type = type,
                notificationId = notificationItem.notificationId,
                actionType = NotificationMessageReceiver.ACTION_READ
            )
            loadUserImage(context, message.id, message.user.image)
        }
    }

    private fun showDefaultNotification(
        context: Context,
        messageId: String
    ) {
        val notificationItem = notificationsMap[messageId]
        if (notificationItem != null && !isForeground()) {
            notificationItem.channelName =
                context.getString(R.string.stream_default_notification_title)
            notificationItem.messageText =
                context.getString(R.string.stream_default_notification_message)
            val notification = prepareNotification(context, messageId, null, true)
            showNotification(notificationItem.notificationId, notification, context)
        }
    }

    private fun showNotification(
        notificationId: Int,
        notification: Notification?,
        context: Context
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (!isForeground()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                config.getNotificationChannel().let { notificationChannel ->
                    notificationChannel.apply {
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
                            400
                        )
                        notificationChannel.setShowBadge(false)
                    }
                    notificationManager?.createNotificationChannel(notificationChannel)
                }
            }
            notificationManager?.notify(notificationId, notification)
        }
    }

    private fun preparePendingIntent(
        context: Context?,
        messageId: String,
        channelId: String,
        type: String,
        notificationId: Int,
        actionType: String
    ): PendingIntent? {
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
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun loadUserImage(
        context: Context,
        messageId: String,
        photoUrl: String?
    ) {
        val notificationItem = notificationsMap[messageId]
        if (notificationItem != null) {
            val notification = prepareNotification(
                context,
                messageId,
                getBitmapFromURL(photoUrl),
                false
            )
            showNotification(
                notificationItem.notificationId,
                notification,
                context
            )
            removeNotificationItem(messageId)
        }
    }

    private fun prepareNotification(
        context: Context,
        messageId: String,
        image: Bitmap?,
        defaultNotification: Boolean
    ): Notification? {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationItem = notificationsMap[messageId]
        val notificationBuilder = config.getNotificationBuilder()
        if (notificationItem != null) {
            val contentIntent = getContentIntent(context, notificationItem, defaultNotification)

            notificationBuilder?.setContentTitle(notificationItem.channelName)
                ?.setContentText(notificationItem.messageText)
                ?.setPriority(NotificationCompat.PRIORITY_HIGH)
                ?.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                ?.setShowWhen(true)
                ?.setContentIntent(contentIntent)
                ?.setSound(defaultSoundUri)

            if (notificationItem.pendingReplyIntent != null) {
                notificationBuilder?.apply {
                    addAction(getReadAction(context, notificationItem.pendingReadIntent))
                    addAction(getReplyAction(context, notificationItem.pendingReplyIntent))
                }
            }
            if (image != null) {
                notificationBuilder.setLargeIcon(image)
            }
        }
        return notificationBuilder.build()
    }

    private fun getContentIntent(
        context: Context,
        notification: ChatNotification,
        defaultNotification: Boolean
    ): PendingIntent? {
        if (defaultNotification) {
            return PendingIntent.getActivity(
                context,
                config.getRequestCode(),
                config.getLauncherIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        if (notification.event != null) return config.getIntentForSocketEvent(notification.event)

        if (notification.remoteMessage != null) return config.getIntentForFirebaseMessage(
            notification.remoteMessage
        )

        return null
    }

    private fun getReadAction(
        context: Context?,
        pendingIntent: PendingIntent?
    ): NotificationCompat.Action? {
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_view,
            context?.getString(R.string.stream_default_notification_read),
            pendingIntent
        ).build()
    }

    private fun getReplyAction(
        context: Context?,
        replyPendingIntent: PendingIntent?
    ): NotificationCompat.Action? {
        val remoteInput =
            RemoteInput.Builder(NotificationMessageReceiver.KEY_TEXT_REPLY)
                .setLabel(context?.getString(R.string.stream_default_notification_type))
                .build()
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            context?.getString(R.string.stream_default_notification_reply),
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun removeNotificationItem(notificationId: String) {
        notificationsMap.remove(notificationId)
    }

    private fun isForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState
            .isAtLeast(Lifecycle.State.STARTED)
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) { // Log exception
            null
        }
    }
}