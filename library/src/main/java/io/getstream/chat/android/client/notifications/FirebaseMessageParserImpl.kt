package io.getstream.chat.android.client.notifications

import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.utils.containsKeys
import io.getstream.chat.android.client.utils.isNullOrEmpty

internal class FirebaseMessageParserImpl(val handler: ChatNotificationHandler) : FirebaseMessageParser {

    private val messageIdKey = handler.getFirebaseMessageIdKey()
    private val channelTypeKey = handler.getFirebaseChannelTypeKey()
    private val channelIdKey = handler.getFirebaseChannelIdKey()

    override fun isValid(message: RemoteMessage): Boolean {
        return verifyPayload(message)
    }

    override fun parse(message: RemoteMessage): FirebaseMessageParser.Data {
        val messageId = message.data[messageIdKey]!!
        val channelId = message.data[channelIdKey]!!
        val channelType = message.data[channelTypeKey]!!

        return FirebaseMessageParser.Data(messageId, channelType, channelId)
    }

    private fun verifyPayload(message: RemoteMessage): Boolean {

        val messageId = message.data[messageIdKey]
        val channelId = message.data[channelIdKey]
        val channelType = message.data[channelTypeKey]

        return message.data.containsKeys(messageIdKey, channelTypeKey, channelIdKey) && !isNullOrEmpty(
            messageId,
            channelId,
            channelType
        )
    }
}
