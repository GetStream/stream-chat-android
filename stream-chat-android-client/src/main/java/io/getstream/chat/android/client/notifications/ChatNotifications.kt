/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.notifications

import android.app.Application
import android.content.Context
import io.getstream.android.push.permissions.NotificationPermissionManager
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationReminderDueEvent
import io.getstream.chat.android.client.notifications.handler.ChatNotification
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal interface ChatNotifications {
    fun onSetUser(user: User)
    fun setDevice(device: Device)
    suspend fun deleteDevice()
    fun onPushMessage(
        pushMessage: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener =
            PushNotificationReceivedListener { _, _ -> },
    )

    fun onChatEvent(event: ChatEvent)
    suspend fun onLogout()
    fun displayNotification(notification: ChatNotification)
    fun dismissChannelNotifications(channelType: String, channelId: String)
}

@Suppress("TooManyFunctions")
internal class ChatNotificationsImpl(
    private val handler: NotificationHandler,
    private val notificationConfig: NotificationConfig,
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
    private val chatClientProvider: () -> ChatClient = { ChatClient.instance() },
) : ChatNotifications {
    private val logger by taggedLogger("Chat:Notifications")

    private val chatClient: ChatClient by lazy { chatClientProvider() }
    private val pushTokenUpdateHandler = PushTokenUpdateHandler()
    private val showedMessages = mutableSetOf<String>()
    private val permissionManager: NotificationPermissionManager =
        NotificationPermissionManager.createNotificationPermissionsManager(
            application = context.applicationContext as Application,
            requestPermissionOnAppLaunch = notificationConfig.requestPermissionOnAppLaunch,
            onPermissionStatus = { status ->
                logger.i { "[onPermissionStatus] status: $status" }
                handler.onNotificationPermissionStatus(status)
            },
        )

    init {
        logger.i { "<init> no args" }
    }

    override fun onSetUser(user: User) {
        logger.i { "[onSetUser] user: $user" }
        permissionManager
            .takeIf { notificationConfig.requestPermissionOnAppLaunch() }
            ?.start()
        notificationConfig.pushDeviceGenerators.firstOrNull { it.isValidForThisDevice() }
            ?.let {
                it.onPushDeviceGeneratorSelected()
                it.asyncGeneratePushDevice { pushDevice ->
                    setDeviceForUser(user, pushDevice.toDevice())
                }
            }
    }

    override fun setDevice(device: Device) {
        logger.i { "[setDevice] device: $device" }
        // If no user is passed, we assume the device is NOT already registered
        setDeviceForUser(null, device)
    }

    override suspend fun deleteDevice() {
        pushTokenUpdateHandler.deleteDevice()
    }

    override fun onPushMessage(
        pushMessage: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) {
        logger.i { "[onPushMessage] message: $pushMessage" }

        pushNotificationReceivedListener.onPushNotificationReceived(pushMessage.channelType, pushMessage.channelId)

        val message = Message(
            id = pushMessage.messageId,
            cid = "${pushMessage.channelType}:${pushMessage.channelId}",
        )
        scope.launch { chatClient.messageReceiptManager.markMessageAsDelivered(message) }

        if (notificationConfig.shouldShowNotificationOnPush() && !handler.onPushMessage(pushMessage)) {
            handlePushMessage(pushMessage)
        }
    }

    override fun onChatEvent(event: ChatEvent) {
        logger.d { "[onChatEvent] event: $event" }
        when (event) {
            is NewMessageEvent -> onNewMessageEvent(event)
            is NotificationReminderDueEvent -> onNotificationReminderDueEvent(event)
            else -> {
                logger.d { "[onChatEvent] ChatEvent not supported: $event" }
            }
        }
    }

    override suspend fun onLogout() {
        logger.i { "[onLogout]" }
        permissionManager.stop()
        handler.dismissAllNotifications()
        cancelLoadDataWork()
    }

    /**
     * Dismiss notification associated to the [channelType] and [channelId] received on the params.
     *
     * @param channelType String that represent the channel type of the channel you want to dismiss notifications.
     * @param channelId String that represent the channel id of the channel you want to dismiss notifications.
     *
     */
    override fun dismissChannelNotifications(channelType: String, channelId: String) {
        handler.dismissChannelNotifications(channelType, channelId)
    }

    private fun handlePushMessage(message: PushMessage) {
        val type = message.type.orEmpty()
        obtainNotificationData(type, message.channelId, message.channelType, message.messageId, message.extraData)
    }

    private fun obtainNotificationData(
        type: String,
        channelId: String,
        channelType: String,
        messageId: String,
        payload: Map<String, Any?> = emptyMap(),
    ) {
        logger.d { "[obtainNotificationData] type: $type, channelCid: $channelId:$channelType, messageId: $messageId" }
        LoadNotificationDataWorker.start(
            context = context,
            type = type,
            channelId = channelId,
            channelType = channelType,
            messageId = messageId,
            payload = payload,
        )
    }

    private fun onNewMessageEvent(event: NewMessageEvent) {
        val currentUserId = ChatClient.instance().getCurrentUser()?.id
        if (event.message.user.id == currentUserId) return

        logger.d { "[onNewMessageEvent] event: $event" }
        if (!handler.onChatEvent(event)) {
            logger.i { "[onNewMessageEvent] handle event internally" }
            obtainNotificationData(event.type, event.channelId, event.channelType, event.message.id)
        }
    }

    private fun onNotificationReminderDueEvent(event: NotificationReminderDueEvent) {
        logger.d { "[onNotificationReminderDueEvent] event: $event" }
        if (!handler.onNotificationReminderDueEvent(event)) {
            logger.i { "[onNotificationReminderDueEvent] handle event internally" }
            obtainNotificationData(event.type, event.channelId, event.channelType, event.messageId)
        }
    }

    private fun cancelLoadDataWork() {
        LoadNotificationDataWorker.cancel(context)
    }

    private fun wasNotificationDisplayed(messageId: String) = showedMessages.contains(messageId)

    override fun displayNotification(notification: ChatNotification) {
        logger.d { "[displayNotification] notification: $notification" }
        when (notification) {
            is ChatNotification.MessageNew -> {
                if (!wasNotificationDisplayed(notification.message.id)) {
                    showedMessages.add(notification.message.id)
                    handler.showNotification(notification)
                }
            }

            else -> handler.showNotification(notification)
        }
    }

    private fun setDeviceForUser(user: User?, device: Device) {
        logger.i { "[setDeviceForUser] userId: ${user?.id}, device: $device" }
        scope.launch {
            pushTokenUpdateHandler.addDevice(user, device)
        }
    }
}

internal object NoOpChatNotifications : ChatNotifications {
    override fun onSetUser(user: User) = Unit
    override fun setDevice(device: Device) = Unit
    override suspend fun deleteDevice() = Unit
    override fun onPushMessage(
        pushMessage: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) = Unit

    override fun onChatEvent(event: ChatEvent) = Unit
    override suspend fun onLogout() = Unit
    override fun displayNotification(notification: ChatNotification) = Unit
    override fun dismissChannelNotifications(channelType: String, channelId: String) = Unit
}
