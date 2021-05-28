package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.zipWith
import io.getstream.chat.android.client.errors.ChatError
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
            val currentUserId = ChatClient.instance().getCurrentUser()?.id
            if (event.message.user.id == currentUserId) return

            logger.logD("Handling $event")
            if (!handler.onChatEvent(event)) {
                logger.logI("Handling $event internally")
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
            GlobalScope.launch(DispatcherProvider.Main) {
                val result = client.queryChannel(event.channelType, event.channelId, QueryChannelRequest()).await()
                if (result.isSuccess) {
                    showNotification(
                        channel = result.data(),
                        message = event.message,
                        shouldShowInForeground = true,
                    )
                } else {
                    showErrorNotification(
                        messageId = event.message.id,
                        error = result.error(),
                        shouldShowInForeground = true,
                    )
                }
            }
        }
    }

    private fun wasNotificationDisplayed(messageId: String) = showedNotifications.contains(messageId)

    internal suspend fun displayNotificationWithData(channelType: String, channelId: String, messageId: String) {
        val getMessage = client.getMessage(messageId)
        val getChannel = client.queryChannel(channelType, channelId, QueryChannelRequest())

        val result = getChannel.zipWith(getMessage).await()
        if (result.isSuccess) {
            val (channel, message) = result.data()
            showNotification(channel = channel, message = message)
        } else {
            showErrorNotification(messageId = messageId, error = result.error())
        }
    }

    private fun showNotification(channel: Channel, message: Message, shouldShowInForeground: Boolean = false) {
        logger.logD("Showing notification with loaded data")
        val notificationId = System.currentTimeMillis().toInt()

        handler.getDataLoadListener()?.onLoadSuccess(channel, message)
        handler.buildNotification(
            notificationId = notificationId,
            channelName = channel.name,
            messageText = message.text,
            messageId = message.id,
            channelType = channel.type,
            channelId = channel.id,
        ).let { notification ->
            showedNotifications.add(message.id)
            showNotification(
                notificationId = notificationId,
                notification = notification,
                shouldShowInForeground = shouldShowInForeground,
            )
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
                    shouldShowInForeground = shouldShowInForeground,
                )
            }
        }
    }

    private fun showErrorNotification(messageId: String, error: ChatError, shouldShowInForeground: Boolean = false) {
        logger.logE("Error loading required data: ${error.message}", error)
        handler.getDataLoadListener()?.onLoadFail(messageId, error)

        showNotification(
            notificationId = System.currentTimeMillis().toInt(),
            notification = handler.buildErrorCaseNotification(),
            shouldShowInForeground = shouldShowInForeground,
        )

        if (handler.config.shouldGroupNotifications) {
            showNotification(
                notificationId = handler.getErrorNotificationGroupSummaryId(),
                notification = handler.buildErrorNotificationGroupSummary(),
                shouldShowInForeground = shouldShowInForeground,
            )
        }
    }

    private fun showNotification(notificationId: Int, notification: Notification, shouldShowInForeground: Boolean) {
        if (shouldShowInForeground || !isForeground()) {
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
