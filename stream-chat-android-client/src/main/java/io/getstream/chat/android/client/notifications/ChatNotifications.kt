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
import io.getstream.chat.android.client.call.zipWith
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
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
    fun onChatEvent(event: ChatEvent)
    fun cancelLoadDataWork()
    suspend fun displayNotificationWithData(
        channelType: String,
        channelId: String,
        messageId: String,
        notificationId: Int,
    )

    fun removeStoredDevice()
    fun showedNotifications(): Set<Triple<String, String, Int>>
}

internal class ChatNotificationsImpl constructor(
    override val handler: ChatNotificationHandler,
    private val client: ChatApi,
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
) : ChatNotifications {
    private val logger = ChatLogger.get("ChatNotifications")

    private val pushTokenUpdateHandler = PushTokenUpdateHandler(context, handler)
    private val showedNotifications = mutableSetOf<Triple<String, String, Int>>()
    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(handler.createNotificationChannel())
    }

    override fun showedNotifications(): Set<Triple<String, String, Int>> = showedNotifications

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

    override fun onChatEvent(event: ChatEvent) {
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

    override fun cancelLoadDataWork() {
        LoadNotificationDataWorker.cancel(context)
    }

    private fun handlePushMessage(message: PushMessage) {
        if (!wasNotificationDisplayed(message.messageId)) {
            val notificationId = System.currentTimeMillis().toInt()

            showedNotifications.add(Triple(message.messageId, message.channelId, notificationId))
            LoadNotificationDataWorker.start(
                context = context,
                channelId = message.channelId,
                channelType = message.channelType,
                messageId = message.messageId,
                notificationId = notificationId,
                notificationChannelName = context.getString(handler.config.loadNotificationDataChannelName),
                notificationIcon = handler.config.loadNotificationDataIcon,
                notificationTitle = context.getString(handler.config.loadNotificationDataTitle),
            )
        }
    }

    private fun handleEvent(event: NewMessageEvent) {
        val messageId = event.message.id
        val notificationId = System.currentTimeMillis().toInt()

        if (!wasNotificationDisplayed(messageId)) {
            showedNotifications.add(Triple(event.message.id, event.channelId, notificationId))
            scope.launch(DispatcherProvider.Main) {
                val result = client.queryChannel(event.channelType, event.channelId, QueryChannelRequest()).await()
                if (result.isSuccess) {
                    showNotification(
                        channel = result.data(),
                        message = event.message,
                        shouldShowInForeground = true,
                        notificationId = notificationId
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

    private fun wasNotificationDisplayed(messageId: String): Boolean {
        return showedNotifications.any { (notificationMessageId, _) ->
            notificationMessageId == messageId
        }
    }

    override suspend fun displayNotificationWithData(channelType: String, channelId: String, messageId: String, notificationId: Int) {
        val getMessage = client.getMessage(messageId)
        val getChannel = client.queryChannel(channelType, channelId, QueryChannelRequest())

        val result = getChannel.zipWith(getMessage).await()
        if (result.isSuccess) {
            val (channel, message) = result.data()
            showNotification(channel = channel, message = message, notificationId = notificationId)
        } else {
            showErrorNotification(messageId = messageId, error = result.error())
        }
    }

    override fun removeStoredDevice() {
        scope.launch {
            pushTokenUpdateHandler.removeStoredDevice()
        }
    }

    private fun showNotification(
        channel: Channel,
        message: Message,
        shouldShowInForeground: Boolean = false,
        notificationId: Int,
    ) {
        logger.logD("Showing notification with loaded data")
        handler.getDataLoadListener()?.onLoadSuccess(channel, message)
        handler.buildNotification(notificationId = notificationId, channel = channel, message = message).build()
            .let { notification ->
                showedNotifications.add(Triple(message.id, channel.id, notificationId))
                showNotification(
                    notificationId = notificationId,
                    messageId = message.id,
                    notification = notification,
                    shouldShowInForeground = shouldShowInForeground,
                )
            }

        if (handler.config.shouldGroupNotifications) {
            handler.buildNotificationGroupSummary(channel = channel, message = message).build().let { notification ->
                showNotification(
                    notificationId = notificationId,
                    messageId = message.id,
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
            messageId = "",
            notification = handler.buildErrorCaseNotification(),
            shouldShowInForeground = shouldShowInForeground,
        )

        if (handler.config.shouldGroupNotifications) {
            showNotification(
                notificationId = handler.getErrorNotificationGroupSummaryId(),
                messageId = "",
                notification = handler.buildErrorNotificationGroupSummary(),
                shouldShowInForeground = shouldShowInForeground,
            )
        }
    }

    private fun showNotification(
        notificationId: Int,
        messageId: String,
        notification: Notification,
        shouldShowInForeground: Boolean,
    ) {
        if (shouldShowInForeground || !isForeground()) {
            notificationManager.notify(messageId, notificationId, notification)
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

    override fun onChatEvent(event: ChatEvent) = Unit
    override fun cancelLoadDataWork() = Unit
    override suspend fun displayNotificationWithData(channelType: String, channelId: String, messageId: String, notificationId: Int) = Unit
    override fun removeStoredDevice() = Unit
    override fun showedNotifications(): Set<Triple<String, String, Int>> = emptySet()
}
