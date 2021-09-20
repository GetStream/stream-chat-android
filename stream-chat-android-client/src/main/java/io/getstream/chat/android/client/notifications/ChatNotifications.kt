package io.getstream.chat.android.client.notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import io.getstream.chat.android.client.ChatClient
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
    fun onDismissNotification(notificationId: Int)
}

internal class ChatNotificationsImpl constructor(
    override val handler: ChatNotificationHandler,
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

    override fun onDismissNotification(notificationId: Int) {
        handler.onDismissNotification(notificationId)
    }

    private fun handlePushMessage(message: PushMessage) {
        obtainNotifactionData(message.channelId, message.channelType, message.messageId)
    }

    private fun obtainNotifactionData(channelId: String, channelType: String, messageId: String) {
        LoadNotificationDataWorker.start(
            context = context,
            channelId = channelId,
            channelType = channelType,
            messageId = messageId,
            notificationChannelName = context.getString(handler.config.loadNotificationDataChannelName),
            notificationIcon = handler.config.loadNotificationDataIcon,
            notificationTitle = context.getString(handler.config.loadNotificationDataTitle),
        )
    }

    private fun handleEvent(event: NewMessageEvent) {
        obtainNotifactionData(event.channelId, event.channelType, event.message.id)
    }

    private fun wasNotificationDisplayed(messageId: String) = showedMessages.contains(messageId)

    override fun displayNotification(channel: Channel, message: Message) {
        logger.logD("Showing notification with loaded data")
        if (!wasNotificationDisplayed(message.id)) {
            showedMessages.add(message.id)
            handler.showNotification(channel, message)
        }
    }

    override fun removeStoredDevice() {
        scope.launch {
            pushTokenUpdateHandler.removeStoredDevice()
        }
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
    override fun onDismissNotification(notificationId: Int) = Unit
}
