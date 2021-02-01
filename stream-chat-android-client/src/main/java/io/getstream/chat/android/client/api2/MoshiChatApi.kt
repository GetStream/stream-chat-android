package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.mapping.toDto
import io.getstream.chat.android.client.api2.model.requests.MessageRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.models.Message

internal class MoshiChatApi(
    private val apiKey: String,
    private val legacyApiDelegate: ChatApi,
    private val messageApi: MessageApi,
) : ChatApi by legacyApiDelegate {

    private lateinit var userId: String
    private lateinit var connectionId: String

    override fun setConnection(userId: String, connectionId: String) {
        this.userId = userId
        this.connectionId = connectionId
    }

    override fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message> {
        return messageApi.sendMessage(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
            message = MessageRequest(message.toDto()),
        ).map { response -> response.message.toDomain() }
    }

    override fun updateMessage(message: Message): Call<Message> {
        return messageApi.updateMessage(
            messageId = message.id,
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
            message = MessageRequest(message.toDto()),
        ).map { response -> response.message.toDomain() }
    }

    override fun getMessage(messageId: String): Call<Message> {
        return messageApi.getMessage(
            messageId = messageId,
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId
        ).map { response -> response.message.toDomain() }
    }

    override fun deleteMessage(messageId: String): Call<Message> {
        return messageApi.deleteMessage(
            messageId = messageId,
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
        ).map { response -> response.message.toDomain() }
    }
}
