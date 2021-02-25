package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.mapping.toDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.requests.AddDeviceRequest
import io.getstream.chat.android.client.api2.model.requests.MessageRequest
import io.getstream.chat.android.client.api2.model.requests.MuteChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MuteUserRequest
import io.getstream.chat.android.client.api2.model.requests.ReactionRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction

internal class MoshiChatApi(
    private val apiKey: String,
    private val legacyApiDelegate: ChatApi,
    private val messageApi: MessageApi,
    private val deviceApi: DeviceApi,
    private val moderationApi: ModerationApi,
) : ChatApi by legacyApiDelegate {

    val logger = ChatLogger.get("MoshiChatApi")

    private var userId: String = ""
        get() {
            if (field == "") {
                logger.logE("userId accessed before being set")
            }
            return field
        }
    private var connectionId: String = ""
        get() {
            if (field == "") {
                logger.logE("connectionId accessed before being set")
            }
            return field
        }

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
            connectionId = connectionId,
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

    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>> {
        return messageApi.getReactions(
            messageId = messageId,
            apiKey = apiKey,
            connectionId = connectionId,
            offset = offset,
            limit = limit,
        ).map { response -> response.reactions.map(DownstreamReactionDto::toDomain) }
    }

    override fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction> {
        return messageApi.sendReaction(
            messageId = reaction.messageId,
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
            request = ReactionRequest(
                reaction = reaction.toDto(),
                enforce_unique = enforceUnique,
            ),
        ).map { response -> response.reaction.toDomain() }
    }

    override fun deleteReaction(
        messageId: String,
        reactionType: String,
    ): Call<Message> {
        return messageApi.deleteReaction(
            messageId = messageId,
            reactionType = reactionType,
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
        ).map { response -> response.message.toDomain() }
    }

    override fun addDevice(firebaseToken: String): Call<Unit> {
        return deviceApi.addDevices(
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
            request = AddDeviceRequest(id = firebaseToken),
        ).toUnitCall()
    }

    override fun deleteDevice(firebaseToken: String): Call<Unit> {
        return deviceApi.deleteDevice(
            deviceId = firebaseToken,
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
        ).toUnitCall()
    }

    override fun getDevices(): Call<List<Device>> {
        return deviceApi.getDevices(
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
        ).map { response -> response.devices.map(DeviceDto::toDomain) }
    }

    override fun muteCurrentUser(): Call<Mute> {
        return muteUser(userId)
    }

    override fun unmuteCurrentUser(): Call<Unit> {
        return unmuteUser(userId)
    }

    override fun muteUser(userId: String): Call<Mute> {
        return moderationApi.muteUser(
            apiKey = apiKey,
            userId = this.userId,
            connectionId = connectionId,
            body = MuteUserRequest(userId, this.userId),
        ).map { response -> response.mute.toDomain() }
    }

    override fun unmuteUser(userId: String): Call<Unit> {
        return moderationApi.unMuteUser(
            apiKey = apiKey,
            userId = this.userId,
            connectionId = this.connectionId,
            body = MuteUserRequest(userId, this.userId),
        ).toUnitCall()
    }

    override fun muteChannel(channelType: String, channelId: String): Call<Unit> {
        return moderationApi.muteChannel(
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
            body = MuteChannelRequest("$channelType:$channelId"),
        ).toUnitCall()
    }

    override fun unMuteChannel(channelType: String, channelId: String): Call<Unit> {
        return moderationApi.unMuteChannel(
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
            body = MuteChannelRequest("$channelType:$channelId"),
        ).toUnitCall()
    }

    private fun Call<*>.toUnitCall() = map {}
}
