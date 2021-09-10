package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal interface ChatNotifications {
    val handler: ChatNotificationHandler
    fun onSetUser()
    fun setDevice(device: Device)
    fun onPushMessage(message: PushMessage, pushNotificationReceivedListener: PushNotificationReceivedListener)
    fun onNewMessageEvent(newMessageEvent: NewMessageEvent)
    fun cancelLoadDataWork()
    fun displayNotification(channel: Channel, message: Message)
    fun removeStoredDevice()
}

internal class ChatNotificationsImpl constructor(
    override val handler: ChatNotificationHandler,
    private val client: ChatApi,
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
) : ChatNotifications {
    private val logger = ChatLogger.get("ChatNotifications")

    private val pushTokenUpdateHandler = PushTokenUpdateHandler(context, handler)
    private val showedMessages = mutableSetOf<String>()
    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(handler.createNotificationChannel())
    }

    override fun onSetUser() {
        handler.onCreateDevice(::setDevice)
    }

    override fun setDevice(device: Device) {
        scope.launch {
            pushTokenUpdateHandler.updateDeviceIfNecessary(device)
        }
    }

    override fun onPushMessage(
        message: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) {
        logger.logI("onReceivePushMessage: $message")

        pushNotificationReceivedListener.onPushNotificationReceived(message.channelType, message.channelId)

        if (!handler.onPushMessage(message)) {
            if (isForeground()) return
            handlePushMessage(message)
        }
    }

    override fun onNewMessageEvent(newMessageEvent: NewMessageEvent) {
        val currentUserId = ChatClient.instance().getCurrentUser()?.id
        if (newMessageEvent.message.user.id == currentUserId) return

        logger.logD("Handling $newMessageEvent")
        if (!handler.onChatEvent(newMessageEvent)) {
            logger.logI("Handling $newMessageEvent internally")
            handleEvent(newMessageEvent)
        }
    }

    override fun cancelLoadDataWork() {
        LoadNotificationDataWorker.cancel(context)
    }

    private fun handlePushMessage(message: PushMessage) {
        if (!wasNotificationDisplayed(message.messageId)) {
            showedMessages.add(message.messageId)
            LoadNotificationDataWorker.start(
                context = context,
                channelId = message.channelId,
                channelType = message.channelType,
                messageId = message.messageId,
                notificationChannelName = context.getString(handler.config.loadNotificationDataChannelName),
                notificationIcon = handler.config.loadNotificationDataIcon,
                notificationTitle = context.getString(handler.config.loadNotificationDataTitle),
            )
        }
    }

    private fun handleEvent(event: NewMessageEvent) {
        val messageId = event.message.id

        if (!wasNotificationDisplayed(messageId)) {
            showedMessages.add(messageId)
            scope.launch(DispatcherProvider.Main) {
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

    private fun wasNotificationDisplayed(messageId: String) = showedMessages.contains(messageId)

    override fun displayNotification(channel: Channel, message: Message) {
        showNotification(channel, message)
    }

    override fun removeStoredDevice() {
        scope.launch {
            pushTokenUpdateHandler.removeStoredDevice()
        }
    }

    private fun showNotification(channel: Channel, message: Message, shouldShowInForeground: Boolean = false) {
        logger.logD("Showing notification with loaded data")
        val notificationId = System.currentTimeMillis().toInt()

        handler.buildNotification(notificationId = notificationId, channel = channel, message = message).build()
            .let { notification ->
                showedMessages.add(message.id)
                showNotification(
                    notificationId = notificationId,
                    notification = notification,
                    shouldShowInForeground = shouldShowInForeground,
                )
            }

        if (handler.config.shouldGroupNotifications) {
            handler.buildNotificationGroupSummary(channel = channel, message = message).build().let { notification ->
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
}

internal class NoOpChatNotifications(override val handler: ChatNotificationHandler) : ChatNotifications {
    override fun onSetUser() = Unit
    override fun setDevice(device: Device) = Unit
    override fun onPushMessage(
        message: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) = Unit

    override fun onNewMessageEvent(newMessageEvent: NewMessageEvent) = Unit
    override fun cancelLoadDataWork() = Unit
    override fun displayNotification(channel: Channel, message: Message) = Unit
    override fun removeStoredDevice() = Unit
}
