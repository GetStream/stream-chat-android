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
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationType
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal interface ChatNotifications {
    fun onSetUser()
    fun setDevice(device: Device)
    fun onPushMessage(message: PushMessage, pushNotificationReceivedListener: PushNotificationReceivedListener)
    fun onNewMessageEvent(newMessageEvent: NewMessageEvent)
    suspend fun onLogout(flushPersistence: Boolean)
    fun displayNotification(type: String, channel: Channel, message: Message)
    fun dismissChannelNotifications(channelType: String, channelId: String)
}

@Suppress("TooManyFunctions")
internal class ChatNotificationsImpl constructor(
    private val handler: NotificationHandler,
    private val notificationConfig: NotificationConfig,
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
) : ChatNotifications {
    private val logger by taggedLogger("Chat:Notifications")

    private val pushTokenUpdateHandler = PushTokenUpdateHandler(context)
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

    override fun onSetUser() {
        logger.i { "[onSetUser] no args" }
        permissionManager
            .takeIf { notificationConfig.requestPermissionOnAppLaunch() }
            ?.start()
        notificationConfig.pushDeviceGenerators.firstOrNull { it.isValidForThisDevice() }
            ?.let {
                it.onPushDeviceGeneratorSelected()
                it.asyncGeneratePushDevice { setDevice(it.toDevice()) }
            }
    }

    override fun setDevice(device: Device) {
        logger.i { "[setDevice] device: $device" }
        scope.launch {
            pushTokenUpdateHandler.updateDeviceIfNecessary(device)
        }
    }

    override fun onPushMessage(
        message: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) {
        logger.i { "[onReceivePushMessage] message: $message" }

        pushNotificationReceivedListener.onPushNotificationReceived(message.channelType, message.channelId)

        if (notificationConfig.shouldShowNotificationOnPush() && !handler.onPushMessage(message)) {
            handlePushMessage(message)
        }
    }

    override fun onNewMessageEvent(newMessageEvent: NewMessageEvent) {
        val currentUserId = ChatClient.instance().getCurrentUser()?.id
        if (newMessageEvent.message.user.id == currentUserId) return

        logger.d { "[onNewMessageEvent] event: $newMessageEvent" }
        if (!handler.onChatEvent(newMessageEvent)) {
            logger.i { "[onNewMessageEvent] handle event internally" }
            handleEvent(newMessageEvent)
        }
    }

    override suspend fun onLogout(flushPersistence: Boolean) {
        logger.i { "[onLogout] flusPersistence: $flushPersistence" }
        permissionManager.stop()
        handler.dismissAllNotifications()
        cancelLoadDataWork()
        if (flushPersistence) { removeStoredDevice() }
    }

    private fun cancelLoadDataWork() {
        LoadNotificationDataWorker.cancel(context)
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
        obtainNotificationData(type, message.channelId, message.channelType, message.messageId)
    }

    private fun obtainNotificationData(type: String, channelId: String, channelType: String, messageId: String) {
        logger.d { "[obtainNotificationData] type: $type, channelCid: $channelId:$channelType, messageId: $messageId" }
        LoadNotificationDataWorker.start(
            context = context,
            type = type,
            channelId = channelId,
            channelType = channelType,
            messageId = messageId,
        )
    }

    private fun handleEvent(event: NewMessageEvent) {
        obtainNotificationData(event.type, event.channelId, event.channelType, event.message.id)
    }

    private fun wasNotificationDisplayed(messageId: String) = showedMessages.contains(messageId)

    override fun displayNotification(type: String, channel: Channel, message: Message) {
        logger.d { "[displayNotification] type: $type, channel.cid: ${channel.cid}, message.cid: ${message.cid}" }
        when (type) {
            NotificationType.NOTIFICATION_REMINDER_DUE -> {
                handler.showNotification(type, channel, message)
            }
            else -> {
                if (!wasNotificationDisplayed(message.id)) {
                    showedMessages.add(message.id)
                    handler.showNotification(type, channel, message)
                }
            }
        }
    }

    private suspend fun removeStoredDevice() {
        pushTokenUpdateHandler.removeStoredDevice()
    }
}

internal object NoOpChatNotifications : ChatNotifications {
    override fun onSetUser() = Unit
    override fun setDevice(device: Device) = Unit
    override fun onPushMessage(
        message: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) = Unit

    override fun onNewMessageEvent(newMessageEvent: NewMessageEvent) = Unit
    override suspend fun onLogout(flushPersistence: Boolean) = Unit
    override fun displayNotification(type: String, channel: Channel, message: Message) = Unit
    override fun dismissChannelNotifications(channelType: String, channelId: String) = Unit
}
