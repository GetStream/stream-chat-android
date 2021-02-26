package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.mapping.toDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.requests.AcceptInviteRequest
import io.getstream.chat.android.client.api2.model.requests.AddDeviceRequest
import io.getstream.chat.android.client.api2.model.requests.AddMembersRequest
import io.getstream.chat.android.client.api2.model.requests.BanUserRequest
import io.getstream.chat.android.client.api2.model.requests.HideChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MarkReadRequest
import io.getstream.chat.android.client.api2.model.requests.MessageRequest
import io.getstream.chat.android.client.api2.model.requests.MuteChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MuteUserRequest
import io.getstream.chat.android.client.api2.model.requests.ReactionRequest
import io.getstream.chat.android.client.api2.model.requests.RejectInviteRequest
import io.getstream.chat.android.client.api2.model.requests.RemoveMembersRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateCooldownRequest
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import java.io.File

internal class MoshiChatApi(
    private val apiKey: String,
    private val legacyApiDelegate: ChatApi,
    private val fileUploader: FileUploader,
    private val messageApi: MessageApi,
    private val channelApi: ChannelApi,
    private val deviceApi: DeviceApi,
    private val moderationApi: ModerationApi,
    private val coroutineScope: CoroutineScope = GlobalScope,
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

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback?,
    ): Call<String> {
        return CoroutineCall(coroutineScope) {
            val result = if (callback != null) {
                fileUploader.sendFile(
                    channelType = channelType,
                    channelId = channelId,
                    userId = userId,
                    connectionId = connectionId,
                    file = file,
                    callback
                )
            } else {
                fileUploader.sendFile(
                    channelType = channelType,
                    channelId = channelId,
                    userId = userId,
                    connectionId = connectionId,
                    file = file,
                )
            }

            if (result != null) {
                Result(result)
            } else {
                Result(ChatError("Upload failed"))
            }
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback?,
    ): Call<String> {
        return CoroutineCall(coroutineScope) {
            val result = if (callback != null) {
                fileUploader.sendImage(
                    channelType = channelType,
                    channelId = channelId,
                    userId = userId,
                    connectionId = connectionId,
                    file = file,
                    callback
                )
            } else {
                fileUploader.sendImage(
                    channelType = channelType,
                    channelId = channelId,
                    userId = userId,
                    connectionId = connectionId,
                    file = file
                )
            }

            if (result != null) {
                Result(result)
            } else {
                Result(ChatError("Upload failed"))
            }
        }
    }

    override fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return CoroutineCall(coroutineScope) {
            fileUploader.deleteFile(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                connectionId = connectionId,
                url = url
            )
            Result(Unit)
        }
    }

    override fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return CoroutineCall(coroutineScope) {
            fileUploader.deleteImage(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                connectionId = connectionId,
                url = url
            )
            Result(Unit)
        }
    }

    override fun flagUser(userId: String): Call<Flag> =
        flag(mutableMapOf("target_user_id" to userId))

    override fun flagMessage(messageId: String): Call<Flag> =
        flag(mutableMapOf("target_message_id" to messageId))

    private fun flag(body: MutableMap<String, String>): Call<Flag> {
        return moderationApi.flag(
            apiKey,
            userId,
            connectionId,
            body
        ).map { response -> response.flag.toDomain() }
    }

    override fun banUser(
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return moderationApi.banUser(
            apiKey = apiKey,
            connectionId = connectionId,
            body = BanUserRequest(
                target_user_id = targetId,
                timeout = timeout,
                reason = reason,
                type = channelType,
                id = channelId,
                shadow = shadow,
            )
        ).toUnitCall()
    }

    override fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return moderationApi.unBanUser(
            apiKey = apiKey,
            connectionId = connectionId,
            targetUserId = targetId,
            channelId = channelId,
            channelType = channelType,
            shadow = shadow,
        ).toUnitCall()
    }

    override fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> = updateCooldown(
        channelType = channelType,
        channelId = channelId,
        cooldownTimeInSeconds = cooldownTimeInSeconds,
    )

    override fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel> = updateCooldown(
        channelType = channelType,
        channelId = channelId,
        cooldownTimeInSeconds = 0,
    )

    private fun updateCooldown(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> {
        return channelApi.updateCooldown(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            clientID = connectionId,
            body = UpdateCooldownRequest(cooldownTimeInSeconds),
        ).map(this::flattenChannel)
    }

    override fun stopWatching(channelType: String, channelId: String): Call<Unit> {
        return channelApi.stopWatching(
            channelType,
            channelId,
            apiKey,
            connectionId,
            emptyMap(),
        ).toUnitCall()
    }

    override fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel> {
        return channelApi.updateChannel(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            clientID = connectionId,
            body = UpdateChannelRequest(extraData, updateMessage?.toDto()),
        ).map(this::flattenChannel)
    }

    override fun showChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit> {
        return channelApi.showChannel(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            clientID = connectionId,
            body = emptyMap(),
        ).toUnitCall()
    }

    override fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Call<Unit> {
        return channelApi.hideChannel(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            clientID = connectionId,
            body = HideChannelRequest(clearHistory),
        ).toUnitCall()
    }

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return channelApi.rejectInvite(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            clientID = connectionId,
            body = RejectInviteRequest(),
        ).map(this::flattenChannel)
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel> {
        return channelApi.acceptInvite(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            clientID = connectionId,
            body = AcceptInviteRequest.create(userId = userId, message = message),
        ).map(this::flattenChannel)
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return channelApi.deleteChannel(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            clientID = connectionId,
        ).map(this::flattenChannel)
    }

    override fun markRead(channelType: String, channelId: String, messageId: String): Call<Unit> {
        return channelApi.markRead(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
            request = MarkReadRequest(messageId),
        ).toUnitCall()
    }

    override fun markAllRead(): Call<Unit> {
        return channelApi.markAllRead(
            apiKey = apiKey,
            userId = userId,
            connectionId = connectionId,
        ).toUnitCall()
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel> {
        return channelApi.addMembers(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            connectionId = connectionId,
            body = AddMembersRequest(members),
        ).map(this::flattenChannel)
    }

    override fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel> {
        return channelApi.removeMembers(
            channelType = channelType,
            channelId = channelId,
            apiKey = apiKey,
            connectionId = connectionId,
            body = RemoveMembersRequest(members),
        ).map(this::flattenChannel)
    }
    private fun flattenChannel(response: ChannelResponse): Channel {
        return response.channel.toDomain().apply {
            watcherCount = response.watcher_count
            read = response.read.map(DownstreamChannelUserRead::toDomain)
            members = response.members.map(DownstreamMemberDto::toDomain)
            messages = response.messages.map { it.toDomain().enrichWithCid(cid) }
            watchers = response.watchers.map(DownstreamUserDto::toDomain)
            hidden = response.hidden
            hiddenMessagesBefore = response.hide_messages_before
        }
    }

    private fun Call<*>.toUnitCall() = map {}
}
