package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.ChannelQueryRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig


class ChatNotificationsImpl(
    private val config: ChatNotificationConfig,
    private val client: ChatApi,
    private val context: Context
) : ChatNotifications {

    private val showedNotifications = mutableSetOf<String>()
    private val logger = ChatLogger.get(ChatNotifications::class.java)

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(config.createNotificationChannel())
    }

    override fun onSetUser() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (it.isSuccessful) {
                logger.logI("FirebaseInstanceId returned token successfully")
                setFirebaseToken(it.result!!.token)
            } else {
                logger.logI("Error: FirebaseInstanceId doesn't returned token")
            }
        }
    }

    override fun setFirebaseToken(firebaseToken: String) {

        logger.logI("setFirebaseToken: $firebaseToken")

        client.addDevice(firebaseToken).enqueue { result ->
            if (result.isSuccess) {
                config.getDeviceRegisteredListener()?.onDeviceRegisteredSuccess()
                logger.logI("DeviceRegisteredSuccess")
            } else {
                config.getDeviceRegisteredListener()?.onDeviceRegisteredError(result.error())
                logger.logE("Error register device ${result.error().message}")
            }
        }
    }

    override fun onFirebaseMessage(message: RemoteMessage) {

        logger.logI("onReceiveFirebaseMessage: payload: {$message.data}")

        if (!config.onFirebaseMessage(message)) {
            if (isForeground()) return
            handleRemoteMessage(message)
        }
    }

    override fun onChatEvent(event: ChatEvent) {

        logger.logI("onReceiveWebSocketEvent: {$event.type}")

        if (!config.onChatEvent(event)) {
            if (isForeground()) return
            logger.logI("onReceiveWebSocketEvent: $event")
            handleEvent(event)
        }

    }

    private fun handleRemoteMessage(message: RemoteMessage) {

        val firebaseParser = config.getFirebaseMessageParser()

        if (firebaseParser.isValid(message)) {
            val data = firebaseParser.parse(message)
            if (checkIfNotificationShowed(data.messageId)) {
                showedNotifications.add(data.messageId)
                loadRequiredData(data.channelType, data.channelId, data.messageId)
            }
        } else {
            logger.logE("Push payload is not configured correctly: {${message.data}}")
        }
    }

    private fun handleEvent(event: ChatEvent) {

        if (event is NewMessageEvent) {

            val channelType = event.cid.split(":")[0]
            val channelId = event.cid.split(":")[1]
            val messageId = event.message.id

            if (checkIfNotificationShowed(messageId)) {
                showedNotifications.add(messageId)
                loadRequiredData(channelType, channelId, messageId)
            }
        }
    }

    private fun checkIfNotificationShowed(messageId: String) = showedNotifications.contains(messageId)

    private fun loadRequiredData(channelType: String, channelId: String, messageId: String) {

        val getMessage = client.getMessage(messageId)
        val getChannel = client.queryChannel(channelType, channelId, ChannelQueryRequest())

        getChannel.zipWith(getMessage).enqueue { result ->
            if (result.isSuccess) {

                val channel = result.data().first
                val message = result.data().second

                config.getDataLoadListener()?.onLoadSuccess(channel, message)
                onRequiredDataLoaded(channel, message)
            } else {
                logger.logE("Error loading required data: ${result.error().message}", result.error())
                config.getDataLoadListener()?.onLoadFail(messageId, result.error())
                showErrorCaseNotification()
            }
        }
    }

    private fun onRequiredDataLoaded(channel: Channel, message: Message) {

        val messageId: String = message.id
        val channelId: String = channel.id

        val notificationId = System.currentTimeMillis().toInt()

        val notification = config.buildNotification(
            notificationId,
            channel.extraData.getOrDefault("name", "").toString(),
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
            config.buildErrorCaseNotification()
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


}