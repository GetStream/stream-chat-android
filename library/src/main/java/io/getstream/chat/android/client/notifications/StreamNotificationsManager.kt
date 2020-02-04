package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.aminography.redirectglide.RedirectGlideUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatApi
import io.getstream.chat.android.client.EventType
import io.getstream.chat.android.client.Message
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.safeLet
import io.getstream.chat.android.client.models.StreamNotification
import io.getstream.chat.android.client.poc.R
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver
import io.getstream.chat.android.client.rest.AddDeviceRequest
import io.getstream.chat.android.client.notifications.options.NotificationOptions
import java.util.*

class StreamNotificationsManager constructor(
    private val notificationOptions: NotificationOptions,
    private val registerListener: DeviceRegisteredListener? = null,
    private val api: ChatApi
) : NotificationsManager {

    companion object {
        const val DEFAULT_REQUEST_CODE = 999
    }

    private val TAG = StreamNotificationsManager::class.java.simpleName
    private val FIREBASE_MESSAGE_ID_KEY = "message_id"

    private val notificationsMap: HashMap<String, StreamNotification> = HashMap()
    private var failMessageListener: NotificationMessageLoadListener? = null

    override fun setFirebaseToken(firebaseToken: String, context: Context) {
        Log.d(TAG, "setFirebaseToken: $firebaseToken")

        api.addDevice(
            request = AddDeviceRequest(
                id = firebaseToken
            )
        ).enqueue { result ->
            if (result.isSuccess) {
                registerListener?.onDeviceRegisteredSuccess()
                Log.i(TAG, "DeviceRegisteredSuccess")
            } else {
                registerListener?.onDeviceRegisteredError(result.error())
                Log.e(TAG, "Error register device ${result.error().message}")
            }
        }
    }

    override fun onReceiveFirebaseMessage(remoteMessage: RemoteMessage, context: Context) {
        val payload: Map<String, String> = remoteMessage.data
        Log.d(TAG, "onLoadMessageFail: $remoteMessage data: $payload")

        handleRemoteMessage(context, remoteMessage)
    }

    override fun onReceiveWebSocketEvent(event: ChatEvent, context: Context) {
        Log.d(TAG, "onReceiveWebSocketEvent: $event")

        handleEvent(context, event)
    }

    override fun handleRemoteMessage(context: Context?, remoteMessage: RemoteMessage?) {
        val messageId = remoteMessage?.data?.get(FIREBASE_MESSAGE_ID_KEY)

        if (checkSentNotificationWithId(messageId)) {
            if (messageId != null && messageId.isNotEmpty()) {
                val notificationModel = StreamNotification(
                    System.currentTimeMillis().toInt(),
                    remoteMessage,
                    null
                )
                notificationsMap[messageId] = notificationModel
                loadMessage(context, messageId)
            } else {
                Log.e(TAG, "RemoteMessage: messageId = $messageId")
            }
        }
    }

    override fun handleEvent(context: Context?, event: ChatEvent?) {
        if (event?.getType() == EventType.MESSAGE_NEW) {

            if (checkSentNotificationWithId(event.message.id)) {
                val notificationModel =
                    StreamNotification(System.currentTimeMillis().toInt(), null, event)
                notificationsMap[event.message.id] = notificationModel
                loadMessage(context, event.message.id)
            } else {
                Log.i(TAG, "Notification with id:${event.message.id} already showed")
            }
        }
    }

    override fun setFailMessageListener(failMessageListener: NotificationMessageLoadListener) {
        this.failMessageListener = failMessageListener
    }

    private fun checkSentNotificationWithId(messageId: String?) =
        notificationsMap[messageId] == null

    private fun loadMessage(
        context: Context?,
        messageId: String
    ) {
        api.getMessage(messageId).enqueue { result ->
            if (result.isSuccess) {
                failMessageListener?.onLoadMessageSuccess(result.data())
                onMessageLoaded(context, result.data())
            } else {
                Log.e(TAG, "Can\'t load message. Error: ${result.error().message}")
                showDefaultNotification(context, messageId)
                failMessageListener?.onLoadMessageFail(messageId)
            }
        }
    }

    private fun onMessageLoaded(
        context: Context?,
        message: Message
    ) {
        val type: String = message.channel.type

        val id: String = message.channel.cid

        notificationsMap[message.id]?.let { notificationItem ->
            notificationItem.channelName = message.channel.getName()
            notificationItem.messageText = message.text
            notificationItem.pendingIntent = preparePendingIntent(
                context,
                id,
                type,
                notificationItem.notificationId
            )
            loadUserImage(context, message.id, message.user.image)
        }
    }

    private fun showDefaultNotification(
        context: Context?,
        messageId: String
    ) {
        val notificationItem = notificationsMap[messageId]
        if (notificationItem != null && !isForeground()) {
            notificationItem.channelName =
                context?.getString(R.string.stream_default_notification_title)
            notificationItem.messageText =
                context?.getString(R.string.stream_default_notification_message)
            val notification = prepareNotification(context, messageId, null, true)
            showNotification(notificationItem.notificationId, notification, context)
        }
    }

    private fun showNotification(
        notificationId: Int,
        notification: Notification?,
        context: Context?
    ) {
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (!isForeground()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationOptions.getNotificationChannel(context)?.let { notificationChannel ->
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
        id: String,
        type: String,
        notificationId: Int
    ): PendingIntent? {
        val notifyIntent = Intent(context, NotificationMessageReceiver::class.java)
        notifyIntent.apply {
            putExtra(NotificationMessageReceiver.KEY_NOTIFICATION_ID, notificationId)
            putExtra(NotificationMessageReceiver.KEY_CHANNEL_ID, id)
            putExtra(NotificationMessageReceiver.KEY_CHANNEL_TYPE, type)
            action = NotificationMessageReceiver.ACTION_REPLY
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun loadUserImage(
        context: Context?,
        messageId: String,
        photoUrl: String
    ) {
        val notificationItem = notificationsMap[messageId]
        if (notificationItem != null) {
            context?.let {
                Glide.with(it)
                    .asBitmap()
                    .load(RedirectGlideUrl(photoUrl, 10))
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                        ) {
                            val notification = prepareNotification(
                                context,
                                messageId,
                                resource,
                                false
                            )
                            showNotification(
                                notificationItem.notificationId,
                                notification,
                                context
                            )
                            removeNotificationItem(messageId)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            val notification = prepareNotification(
                                context,
                                messageId,
                                null,
                                false
                            )
                            showNotification(
                                notificationItem.notificationId,
                                notification,
                                context
                            )
                            removeNotificationItem(messageId)
                        }
                    })
            }
        }
    }

    private fun prepareNotification(
        context: Context?,
        messageId: String,
        image: Bitmap?,
        defaultNotification: Boolean
    ): Notification? {
        val defaultSoundUri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_NOTIFICATION
        )
        val notificationItem = notificationsMap[messageId]
        val notificationBuilder = notificationOptions.getNotificationBuilder(context)
        if (notificationItem != null) {
            val contentIntent =
                getContentIntent(context, notificationItem, defaultNotification)
            notificationBuilder!!.setContentTitle(notificationItem.channelName)
                .setContentText(notificationItem.messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setShowWhen(true)
                .setContentIntent(contentIntent)
                .setSound(defaultSoundUri)
            if (notificationItem.pendingIntent != null) {
                notificationBuilder.addAction(
                    getReadAction(
                        context,
                        notificationItem.pendingIntent
                    )
                )
                    .addAction(getReplyAction(context, notificationItem.pendingIntent))
            }
            if (image != null) {
                notificationBuilder.setLargeIcon(image)
            }
        }
        return notificationBuilder?.build()
    }

    private fun getContentIntent(
        context: Context?,
        item: StreamNotification,
        defaultNotification: Boolean
    ): PendingIntent? {
        if (defaultNotification) {
            return PendingIntent.getActivity(
                context,
                DEFAULT_REQUEST_CODE,
                notificationOptions.getDefaultLauncherIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        safeLet(item.event, context) { event, ctx ->
            return@safeLet notificationOptions.getNotificationIntentProvider()
                ?.getIntentForWebSocketEvent(ctx, event)
        }

        safeLet(item.remoteMessage, context) { remoteMessage, ctx ->
            return@safeLet notificationOptions.getNotificationIntentProvider()
                ?.getIntentForFirebaseMessage(ctx, remoteMessage)
        }

        return null
    }

    private fun getReadAction(
        context: Context?,
        pendingIntent: PendingIntent?
    ): NotificationCompat.Action? {
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_view,
            context?.getString(R.string.stream_default_notification_read), pendingIntent
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