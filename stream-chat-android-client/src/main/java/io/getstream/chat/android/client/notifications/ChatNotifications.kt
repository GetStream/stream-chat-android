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
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.zipWith
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ChatNotifications private constructor(
    val handler: ChatNotificationHandler,
    private val client: ChatApi,
    private val context: Context,
) {
    private val logger = ChatLogger.get("ChatNotifications")

    private val pushTokenUpdateHandler = PushTokenUpdateHandler(context, handler)
    private val showedNotifications = mutableSetOf<String>()
    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(handler.createNotificationChannel())
    }

    fun onSetUser() {
        handler.getFirebaseMessaging()?.token?.addOnCompleteListener {
            if (it.isSuccessful) {
                logger.logI("Firebase returned token successfully")
                setFirebaseToken(it.result!!)
            } else {
                logger.logI("Error: Firebase didn't returned token")
            }
        }
    }

    fun setFirebaseToken(firebaseToken: String) {
        pushTokenUpdateHandler.updateTokenIfNecessary(firebaseToken)
    }

    fun onFirebaseMessage(message: RemoteMessage, pushNotificationReceivedListener: PushNotificationReceivedListener) {
        logger.logI("onReceiveFirebaseMessage: payload: ${message.data}")

        if (isValidRemoteMessage(message)) {
            val data = handler.getFirebaseMessageParser().run { parse(message) }
            pushNotificationReceivedListener.onPushNotificationReceived(data.channelType, data.channelId)
        }

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

    fun cancelLoadDataWork() {
        LoadNotificationDataWorker.cancel(context)
    }

    private fun handleRemoteMessage(message: RemoteMessage) {
        if (isValidRemoteMessage(message)) {
            val data = handler.getFirebaseMessageParser().run { parse(message) }

            if (!wasNotificationDisplayed(data.messageId)) {
                showedNotifications.add(data.messageId)
                LoadNotificationDataWorker.start(
                    context = context,
                    channelId = data.channelId,
                    channelType = data.channelType,
                    messageId = data.messageId,
                    notificationChannelName = context.getString(handler.config.loadNotificationDataChannelName),
                    notificationIcon = handler.config.loadNotificationDataIcon,
                    notificationTitle = context.getString(handler.config.loadNotificationDataTitle),
                )
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
            // Needs to be refactored in a separate task
            GlobalScope.launch(DispatcherProvider.Main) {
                displayNotificationWithData(event.channelType, event.channelId, messageId)
            }
        }
    }

    private fun wasNotificationDisplayed(messageId: String) = showedNotifications.contains(messageId)

    internal suspend fun displayNotificationWithData(channelType: String, channelId: String, messageId: String) {
        val getMessage = client.getMessage(messageId)
        val getChannel = client.queryChannel(channelType, channelId, QueryChannelRequest())

        val result = getChannel.zipWith(getMessage).await()
        if (result.isSuccess) {
            logger.logD("Notification data loaded")
            val (channel, message) = result.data()
            handler.getDataLoadListener()?.onLoadSuccess(channel, message)
            onRequiredDataLoaded(channel, message)
        } else {
            logger.logE("Error loading required data: ${result.error().message}", result.error())
            handler.getDataLoadListener()?.onLoadFail(messageId, result.error())
            showErrorCaseNotification()
        }
    }

    private fun onRequiredDataLoaded(channel: Channel, message: Message) {
        val notificationId = System.currentTimeMillis().toInt()

        handler.buildNotification(
            notificationId = notificationId,
            channelName = channel.name,
            messageText = message.text,
            messageId = message.id,
            channelType = channel.type,
            channelId = channel.id,
        ).let { notification ->
            showedNotifications.add(message.id)
            showNotification(notificationId = notificationId, notification = notification)
        }

        if (handler.config.shouldGroupNotifications) {
            handler.buildNotificationGroupSummary(
                channelType = channel.type,
                channelId = channel.id,
                channelName = channel.name,
                messageId = message.id,
            ).let { notification ->
                showNotification(
                    notificationId = handler.getNotificationGroupSummaryId(
                        channelType = channel.type,
                        channelId = channel.id,
                    ),
                    notification = notification,
                )
            }
        }
    }

    private fun showErrorCaseNotification() {
        showNotification(
            notificationId = System.currentTimeMillis().toInt(),
            notification = handler.buildErrorCaseNotification(),
        )

        if (handler.config.shouldGroupNotifications) {
            handler.buildErrorNotificationGroupSummary().let { notification ->
                showNotification(
                    notificationId = handler.getErrorNotificationGroupSummaryId(),
                    notification = notification,
                )
            }
        }
    }

    private fun showNotification(notificationId: Int, notification: Notification) {
        if (!isForeground()) {
            notificationManager.notify(notificationId, notification)
        }
    }

    private fun isForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    companion object {
        fun create(
            handler: ChatNotificationHandler,
            client: ChatApi,
            context: Context,
        ) = ChatNotifications(handler, client, context).apply(ChatNotifications::init)
    }
}
