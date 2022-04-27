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

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@InternalStreamChatApi
public interface ChatNotifications {
    public fun onSetUser()
    public fun setDevice(device: Device)
    public fun onPushMessage(message: PushMessage, pushNotificationReceivedListener: PushNotificationReceivedListener)
    public fun onNewMessageEvent(newMessageEvent: NewMessageEvent)
    public fun onLogout()
    public fun displayNotification(channel: Channel, message: Message)
    public fun dismissChannelNotifications(channelType: String, channelId: String)
}

@Suppress("TooManyFunctions")
internal class ChatNotificationsImpl constructor(
    private val handler: NotificationHandler,
    private val notificationConfig: NotificationConfig,
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
) : ChatNotifications {
    private val logger = ChatLogger.get("ChatNotifications")

    private val pushTokenUpdateHandler = PushTokenUpdateHandler(context)
    private val showedMessages = mutableSetOf<String>()

    override fun onSetUser() {
        notificationConfig.pushDeviceGenerators.firstOrNull { it.isValidForThisDevice(context) }
            ?.asyncGenerateDevice(::setDevice)
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

        if (notificationConfig.shouldShowNotificationOnPush() && !handler.onPushMessage(message)) {
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

    override fun onLogout() {
        handler.dismissAllNotifications()
        removeStoredDevice()
        cancelLoadDataWork()
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
        obtainNotifactionData(message.channelId, message.channelType, message.messageId)
    }

    private fun obtainNotifactionData(channelId: String, channelType: String, messageId: String) {
        LoadNotificationDataWorker.start(
            context = context,
            channelId = channelId,
            channelType = channelType,
            messageId = messageId,
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

    private fun removeStoredDevice() {
        scope.launch {
            pushTokenUpdateHandler.removeStoredDevice()
        }
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
    override fun onLogout() = Unit
    override fun displayNotification(channel: Channel, message: Message) = Unit
    override fun dismissChannelNotifications(channelType: String, channelId: String) = Unit
}
