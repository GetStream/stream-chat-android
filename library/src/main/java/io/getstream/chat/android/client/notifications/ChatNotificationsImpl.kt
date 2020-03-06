package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.ChannelQueryRequest
import io.getstream.chat.android.client.bitmaps.BitmapsLoader
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChatNotification
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver
import io.getstream.chat.android.client.utils.containsKeys
import io.getstream.chat.android.client.utils.isNullOrEmpty


class ChatNotificationsImpl(
    private val config: ChatNotificationConfig,
    private val client: ChatApi,
    private val bitmapsLoader: BitmapsLoader,
    private val context: Context
) : ChatNotifications {

    private val TAG = ChatNotifications::class.java.simpleName
    private val notificationsMap = mutableMapOf<String, ChatNotification>()
    private val logger: ChatLogger = ChatLogger.instance

    override fun onSetUser() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (it.isSuccessful) {
                logger.logI(TAG, "FirebaseInstanceId returned token successfully")
                setFirebaseToken(it.result!!.token)
            } else {
                logger.logI(TAG, "Error: FirebaseInstanceId doesn't returned token")
            }
        }
    }

    override fun setFirebaseToken(firebaseToken: String) {
        logger.logI(TAG, "setFirebaseToken: $firebaseToken")

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

    override fun onReceiveFirebaseMessage(message: RemoteMessage) {
        ChatLogger.instance.logI(TAG, "onReceiveFirebaseMessage: {$message.data}")
        if (isForeground()) return
        val payload: Map<String, String> = message.data
        logger.logI(TAG, "onReceiveFirebaseMessage: payload: $payload")
        handleRemoteMessage(message)
    }

    override fun onReceiveWebSocketEvent(event: ChatEvent) {
        ChatLogger.instance.logI(TAG, "onReceiveWebSocketEvent: {$event.type}")
        if (isForeground()) return
        logger.logI(TAG, "onReceiveWebSocketEvent: $event")
        handleEvent(event)
    }

    private fun handleRemoteMessage(message: RemoteMessage) {

        if (!verifyPayload(message)) {
            return
        }

        val messageIdKey = config.getFirebaseMessageIdKey()
        val channelIdKey = config.getFirebaseChannelIdKey()
        val channelTypeKey = config.getFirebaseChannelTypeKey()

        val messageId = message.data[messageIdKey]!!
        val channelId = message.data[channelIdKey]!!
        val channelType = message.data[channelTypeKey]!!

        if (checkSentNotificationWithId(messageId)) {
            val notificationModel =
                ChatNotification(System.currentTimeMillis().toInt(), message, null)
            notificationsMap[messageId] = notificationModel
            loadChannelAndMessage(channelType, channelId, messageId, context)
        }
    }

    private fun handleEvent(event: ChatEvent) {

        if (event is NewMessageEvent) {

            val channelType = event.cid.split(":")[0]
            val channelId = event.cid.split(":")[1]

            if (checkSentNotificationWithId(event.message.id)) {
                val currentTime = System.currentTimeMillis().toInt()
                val notificationModel = ChatNotification(currentTime, null, event)
                notificationsMap[event.message.id] = notificationModel
                loadChannelAndMessage(channelType, channelId, event.message.id, context)
            }
        }
    }

    private fun verifyPayload(message: RemoteMessage): Boolean {

        val messageIdKey = config.getFirebaseMessageIdKey()
        val channelTypeKey = config.getFirebaseChannelIdKey()
        val channelIdKey = config.getFirebaseChannelIdKey()

        val messageId = message.data[messageIdKey]
        val channelId = message.data[channelIdKey]
        val channelType = message.data[channelTypeKey]

        return if (message.data.containsKeys(messageIdKey, channelTypeKey, channelIdKey) ||
            !isNullOrEmpty(messageId, channelId, channelType)
        ) {
            logger.logE(
                TAG,
                "Push payload is not configured correctly. Required $messageIdKey = $messageId " +
                        "required $channelIdKey = $channelId, " +
                        "required $channelTypeKey = $channelType"
            )
            false
        } else {
            true
        }
    }

    private fun checkSentNotificationWithId(messageId: String?) =
        notificationsMap[messageId] == null

    private fun loadChannelAndMessage(
        channelType: String,
        channelId: String,
        messageId: String,
        context: Context
    ) {
        val getMessage = client.getMessage(messageId)
        val getChannel = client.queryChannel(channelType, channelId, ChannelQueryRequest())

        getChannel.zipWith(getMessage).enqueue { result ->
            if (result.isSuccess) {

                val channel = result.data().first
                val message = result.data().second

                config.getDataLoadListener()?.onLoadSuccess(channel, message)
                onChannelAndMessageLoaded(channel, message, context)
            } else {
                logger?.logE(TAG, "Can\'t load message. Error: ${result.error().message}")
                showDefaultNotification(context, messageId)
                config.getDataLoadListener()?.onLoadFail(messageId, result.error())
            }
        }
    }

    private fun onChannelAndMessageLoaded(
        channel: Channel,
        message: Message,
        context: Context
    ) {
        val type: String = channel.type

        val messageId: String = message.id
        val channelId: String = channel.id

        notificationsMap[message.id]?.let { notificationItem ->
            notificationItem.channelName =
                channel.extraData.getOrDefault("name", "").toString()
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
        photoUrl: String
    ) {
        val notificationItem = notificationsMap[messageId]
        if (notificationItem != null) {

            bitmapsLoader.load(photoUrl) {
                val notification = prepareNotification(
                    context,
                    messageId,
                    null,
                    //getBitmapFromUrl(photoUrl),
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

            notificationBuilder.setContentTitle(notificationItem.channelName)
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


}