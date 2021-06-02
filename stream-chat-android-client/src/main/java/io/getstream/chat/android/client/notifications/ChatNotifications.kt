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
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal interface ChatNotifications {
    val handler: ChatNotificationHandler
    fun onSetUser()
    fun setFirebaseToken(firebaseToken: String)
    fun onFirebaseMessage(message: RemoteMessage, pushNotificationReceivedListener: PushNotificationReceivedListener)
    fun onChatEvent(event: ChatEvent)
    fun isValidRemoteMessage(message: RemoteMessage): Boolean
    fun cancelLoadDataWork()
    suspend fun displayNotificationWithData(channelType: String, channelId: String, messageId: String)
}

internal class ChatNotificationsImpl constructor(
    override val handler: ChatNotificationHandler,
    private val client: ChatApi,
    private val context: Context,
) : ChatNotifications {
    private val logger = ChatLogger.get("ChatNotifications")

    private val pushTokenUpdateHandler = PushTokenUpdateHandler(context, handler)
    private val showedNotifications = mutableSetOf<String>()
    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(handler.createNotificationChannel())
    }

    override fun onSetUser() {
        handler.getFirebaseMessaging()?.token?.addOnCompleteListener {
            if (it.isSuccessful) {
                logger.logI("Firebase returned token successfully")
                setFirebaseToken(it.result!!)
            } else {
                logger.logI("Error: Firebase didn't returned token")
            }
        }
    }

    override fun setFirebaseToken(firebaseToken: String) {
        pushTokenUpdateHandler.updateTokenIfNecessary(firebaseToken)
    }

    override fun onFirebaseMessage(
        message: RemoteMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) {
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

    override fun isValidRemoteMessage(message: RemoteMessage) = handler.isValidRemoteMessage(message)

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

    override suspend fun displayNotificationWithData(channelType: String, channelId: String, messageId: String) {
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
        handler.buildNotification(notificationId = notificationId, channel = channel, message = message).build()
            .let { notification ->
                showedNotifications.add(message.id)
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
}

internal class NoOpChatNotifications(override val handler: ChatNotificationHandler) : ChatNotifications {
    override fun onSetUser() = Unit
    override fun setFirebaseToken(firebaseToken: String) = Unit
    override fun onFirebaseMessage(
        message: RemoteMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) = Unit

    override fun onChatEvent(event: ChatEvent) = Unit
    override fun isValidRemoteMessage(message: RemoteMessage) = false
    override fun cancelLoadDataWork() = Unit
    override suspend fun displayNotificationWithData(channelType: String, channelId: String, messageId: String) = Unit
}
