package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.call.zipWith
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler

internal class ChatNotifications private constructor(
    val handler: ChatNotificationHandler,
    private val client: ChatApi,
    private val context: Context
) {

    private val showedNotifications = mutableSetOf<String>()
    private val logger = ChatLogger.get("ChatNotifications")

    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(handler.createNotificationChannel())
    }

    fun onSetUser() {
        handler.getFirebaseInstanceId()?.instanceId?.addOnCompleteListener {
            if (it.isSuccessful) {
                logger.logI("FirebaseInstanceId returned token successfully")
                setFirebaseToken(it.result!!.token)
            } else {
                logger.logI("Error: FirebaseInstanceId doesn't returned token")
            }
        }
    }

    fun setFirebaseToken(firebaseToken: String) {
        logger.logI("setFirebaseToken: $firebaseToken")

        client.addDevice(firebaseToken).enqueue { result ->
            if (result.isSuccess) {
                handler.getDeviceRegisteredListener()?.onDeviceRegisteredSuccess()
                logger.logI("DeviceRegisteredSuccess")
            } else {
                handler.getDeviceRegisteredListener()?.onDeviceRegisteredError(result.error())
                logger.logE("Error register device ${result.error().message}")
            }
        }
    }

    fun onFirebaseMessage(message: RemoteMessage) {
        logger.logI("onReceiveFirebaseMessage: payload: {$message.data}")

        if (!handler.onFirebaseMessage(message)) {
            if (isForeground()) return
            handleRemoteMessage(message)
        }
    }

    fun onChatEvent(event: ChatEvent) {
        if (event is NewMessageEvent) {
            logger.logI("onChatEvent: {$event.type}")

            if (!handler.onChatEvent(event)) {
                if (isForeground()) return
                logger.logI("onReceiveWebSocketEvent: $event")
                handleEvent(event)
            }
        }
    }

    private fun handleRemoteMessage(message: RemoteMessage) {
        if (isValidRemoteMessage(message)) {
            val firebaseParser = handler.getFirebaseMessageParser()
            val data = firebaseParser.parse(message)
            if (!wasNotificationDisplayed(data.messageId)) {
                showedNotifications.add(data.messageId)
                loadRequiredData(data.channelType, data.channelId, data.messageId)
            }
        } else {
            logger.logE("Push payload is not configured correctly: {${message.data}}")
        }
    }

    fun isValidRemoteMessage(message: RemoteMessage) = handler.isValidRemoteMessage(message)

    private fun handleEvent(event: NewMessageEvent) {
        val messageId = event.message.id

        if (!wasNotificationDisplayed(messageId)) {
            showedNotifications.add(messageId)
            loadRequiredData(event.channelType, event.channelId, messageId)
        }
    }

    private fun wasNotificationDisplayed(messageId: String) = showedNotifications.contains(messageId)

    private fun loadRequiredData(channelType: String, channelId: String, messageId: String) {
        val getMessage = client.getMessage(messageId)
        val getChannel = client.queryChannel(channelType, channelId, QueryChannelRequest())

        getChannel.zipWith(getMessage).enqueue { result ->
            if (result.isSuccess) {
                val (channel, message) = result.data()

                handler.getDataLoadListener()?.onLoadSuccess(channel, message)
                onRequiredDataLoaded(channel, message)
            } else {
                logger.logE("Error loading required data: ${result.error().message}", result.error())
                handler.getDataLoadListener()?.onLoadFail(messageId, result.error())
                showErrorCaseNotification()
            }
        }
    }

    private fun onRequiredDataLoaded(channel: Channel, message: Message) {
        val messageId: String = message.id
        val channelId: String = channel.id
        val notificationId = System.currentTimeMillis().toInt()
        val channelName = channel.extraData["name"] ?: ""

        val notification = handler.buildNotification(
            notificationId,
            channelName.toString(),
            message.text,
            messageId,
            channel.type,
            channelId
        )

        showedNotifications.add(messageId)

        showNotification(notificationId, notification)
    }

    private fun showErrorCaseNotification() {
        showNotification(
            System.currentTimeMillis().toInt(),
            handler.buildErrorCaseNotification()
        )
    }

    private fun showNotification(notificationId: Int, notification: Notification) {

        if (!isForeground()) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.notify(
                notificationId,
                notification
            )
        }
    }

    private fun isForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    companion object {
        fun create(
            handler: ChatNotificationHandler,
            client: ChatApi,
            context: Context
        ) = ChatNotifications(handler, client, context).apply(ChatNotifications::init)
    }
}
