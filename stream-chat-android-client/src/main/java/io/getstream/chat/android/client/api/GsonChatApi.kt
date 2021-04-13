package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.AcceptInviteRequest
import io.getstream.chat.android.client.api.models.AddDeviceRequest
import io.getstream.chat.android.client.api.models.AddMembersRequest
import io.getstream.chat.android.client.api.models.BanUserRequest
import io.getstream.chat.android.client.api.models.BannedUserResponse
import io.getstream.chat.android.client.api.models.ChannelResponse
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.GetSyncHistory
import io.getstream.chat.android.client.api.models.GuestUserRequest
import io.getstream.chat.android.client.api.models.HideChannelRequest
import io.getstream.chat.android.client.api.models.MarkReadRequest
import io.getstream.chat.android.client.api.models.MessageRequest
import io.getstream.chat.android.client.api.models.MuteChannelRequest
import io.getstream.chat.android.client.api.models.MuteUserRequest
import io.getstream.chat.android.client.api.models.QueryBannedUsersRequest
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryMembersRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.ReactionRequest
import io.getstream.chat.android.client.api.models.RejectInviteRequest
import io.getstream.chat.android.client.api.models.RemoveMembersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.SendEventRequest
import io.getstream.chat.android.client.api.models.TranslateMessageRequest
import io.getstream.chat.android.client.api.models.UpdateChannelPartialRequest
import io.getstream.chat.android.client.api.models.UpdateChannelRequest
import io.getstream.chat.android.client.api.models.UpdateCooldownRequest
import io.getstream.chat.android.client.api.models.UpdateUsersRequest
import io.getstream.chat.android.client.api.models.toDomain
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.call.toUnitCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.BannedUsersSort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.GuestUser
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.UuidGenerator
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.Date
import kotlin.collections.set

internal class GsonChatApi(
    private val retrofitApi: RetrofitApi,
    private val retrofitAnonymousApi: RetrofitAnonymousApi,
    private val uuidGenerator: UuidGenerator,
    private val fileUploader: FileUploader,
    private val coroutineScope: CoroutineScope,
) : ChatApi {

    private var userId: String = ""
    private var connectionId: String = ""

    override fun setConnection(userId: String, connectionId: String) {
        this.userId = userId
        this.connectionId = connectionId
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

    override fun addDevice(firebaseToken: String): Call<Unit> {
        return retrofitApi.addDevices(
            connectionId = connectionId,
            request = AddDeviceRequest(firebaseToken)
        ).toUnitCall()
    }

    override fun deleteDevice(firebaseToken: String): Call<Unit> {
        return retrofitApi.deleteDevice(
            deviceId = firebaseToken,
            connectionId = connectionId
        ).toUnitCall()
    }

    override fun getDevices(): Call<List<Device>> {
        return retrofitApi.getDevices(
            connectionId = connectionId
        ).map { it.devices }
    }

    override fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        return retrofitApi.searchMessages(
            connectionId = connectionId,
            payload = request
        )
            .map {
                it.results.map { resp ->
                    resp.message.apply {
                        (cid.takeUnless(CharSequence::isBlank) ?: channelInfo?.cid)
                            ?.let(::enrichWithCid)
                    }
                }
            }
    }

    override fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>> {
        return retrofitApi.getRepliesMore(
            messageId = messageId,
            connectionId = connectionId,
            limit = limit,
            firstId = firstId
        ).map { it.messages }
    }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return retrofitApi.getReplies(
            messageId = messageId,
            connectionId = connectionId,
            limit = limit
        ).map { it.messages }
    }

    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>> {
        return retrofitApi.getReactions(
            messageId = messageId,
            connectionId = connectionId,
            offset = offset,
            limit = limit
        ).map { it.reactions }
    }

    override fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction> {
        return retrofitApi.sendReaction(
            messageId = reaction.messageId,
            connectionId = connectionId,
            request = ReactionRequest(reaction, enforceUnique)
        ).map { it.reaction }
    }

    override fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return retrofitApi.deleteReaction(
            messageId = messageId,
            reactionType = reactionType,
            connectionId = connectionId
        ).map { it.message }
    }

    override fun deleteMessage(messageId: String): Call<Message> {
        return retrofitApi.deleteMessage(
            messageId = messageId,
            connectionId = connectionId
        ).map { it.message }
    }

    override fun sendAction(request: SendActionRequest): Call<Message> {
        return retrofitApi.sendAction(
            messageId = request.messageId,
            connectionId = connectionId,
            request = request
        ).map { it.message }
    }

    override fun getMessage(messageId: String): Call<Message> {
        return retrofitApi.getMessage(
            messageId = messageId,
            connectionId = connectionId
        ).map { it.message }
    }

    override fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Call<Message> {
        verifyMessageId(message)

        return retrofitApi.sendMessage(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            message = MessageRequest(message)
        ).map { it.message }
    }

    override fun muteChannel(channelType: String, channelId: String): Call<Unit> {
        return retrofitApi.muteChannel(
            connectionId = connectionId,
            body = MuteChannelRequest("$channelType:$channelId")
        ).toUnitCall()
    }

    override fun unmuteChannel(channelType: String, channelId: String): Call<Unit> {
        return retrofitApi.unmuteChannel(
            connectionId = connectionId,
            body = MuteChannelRequest("$channelType:$channelId")
        ).toUnitCall()
    }

    override fun updateMessage(
        message: Message,
    ): Call<Message> {
        return retrofitApi.updateMessage(
            messageId = message.id,
            connectionId = connectionId,
            message = MessageRequest(message)
        ).map { it.message }
    }

    override fun stopWatching(
        channelType: String,
        channelId: String,
    ): Call<Unit> {
        return retrofitApi.stopWatching(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = emptyMap()
        ).toUnitCall()
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {
        if (connectionId.isEmpty()) return noConnectionIdError()

        return retrofitApi.queryChannels(
            connectionId = connectionId,
            payload = query
        ).map {
            flattenChannels(it.channels)
        }
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        val map = users.associateBy({ it.id }, { user -> user })

        return retrofitApi.updateUsers(
            connectionId = connectionId,
            body = UpdateUsersRequest(map)
        )
            .map { response ->
                response.users.flatMap {
                    listOf(it.value)
                }
            }
    }

    override fun queryChannel(
        channelType: String,
        channelId: String,
        query: QueryChannelRequest,
    ): Call<Channel> {
        return if (channelId.isEmpty()) {
            retrofitApi.queryChannel(
                channelType = channelType,
                connectionId = connectionId,
                request = query
            )
        } else {
            retrofitApi.queryChannel(
                channelType = channelType,
                channelId = channelId,
                connectionId = connectionId,
                request = query
            )
        }.map(::flattenChannel)
    }

    override fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel> {
        return retrofitApi.updateChannel(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = UpdateChannelRequest(extraData, updateMessage)
        ).map { flattenChannel(it) }
    }

    override fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Channel> {
        return retrofitApi.updateChannelPartial(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = UpdateChannelPartialRequest(set, unset)
        ).map { flattenChannel(it) }
    }

    override fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> {
        return updateCooldown(
            channelType = channelType,
            channelId = channelId,
            cooldownTimeInSeconds = cooldownTimeInSeconds
        )
    }

    override fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel> {
        return updateCooldown(
            channelType = channelType,
            channelId = channelId,
            cooldownTimeInSeconds = 0
        )
    }

    private fun updateCooldown(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> =
        retrofitApi.updateCooldown(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = UpdateCooldownRequest(cooldownTimeInSeconds)
        ).map { flattenChannel(it) }

    override fun markRead(
        channelType: String,
        channelId: String,
        messageId: String,
    ): Call<Unit> {
        return retrofitApi.markRead(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            request = MarkReadRequest(messageId)
        ).toUnitCall()
    }

    override fun showChannel(channelType: String, channelId: String): Call<Unit> {
        return retrofitApi.showChannel(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = emptyMap()
        ).toUnitCall()
    }

    override fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Call<Unit> {
        return retrofitApi.hideChannel(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = HideChannelRequest(clearHistory)
        ).toUnitCall()
    }

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return retrofitApi.rejectInvite(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = RejectInviteRequest()
        ).map { flattenChannel(it) }
    }

    override fun muteCurrentUser(): Call<Mute> {
        return muteUser(userId)
    }

    override fun unmuteCurrentUser(): Call<Unit> {
        return unmuteUser(userId)
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel> {
        return retrofitApi.acceptInvite(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = AcceptInviteRequest(
                User().apply { id = userId },
                AcceptInviteRequest.AcceptInviteMessage(message)
            )
        ).map { flattenChannel(it) }
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return retrofitApi.deleteChannel(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId
        ).map { flattenChannel(it) }
    }

    override fun markAllRead(): Call<Unit> {
        return retrofitApi.markAllRead(
            connectionId = connectionId
        ).toUnitCall()
    }

    override fun getGuestUser(userId: String, userName: String): Call<GuestUser> {
        return retrofitAnonymousApi.getGuestUser(
            body = GuestUserRequest(userId, userName)
        ).map { GuestUser(it.user, it.accessToken) }
    }

    override fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>> {
        return retrofitApi.queryUsers(
            connectionId = connectionId,
            payload = queryUsers
        ).map { it.users }
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel> {
        return retrofitApi.addMembers(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = AddMembersRequest(members)
        ).map { flattenChannel(it) }
    }

    override fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel> {
        return retrofitApi.removeMembers(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = RemoveMembersRequest(members)
        ).map { flattenChannel(it) }
    }

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        return retrofitApi.queryMembers(
            connectionId = connectionId,
            payload = QueryMembersRequest(
                channelType,
                channelId,
                filter,
                offset,
                limit,
                sort,
                members
            )
        ).map { it.members }
    }

    override fun muteUser(
        userId: String,
    ): Call<Mute> {
        return retrofitApi.muteUser(
            connectionId = connectionId,
            body = MuteUserRequest(userId, this.userId)
        ).map { it.mute }
    }

    override fun unmuteUser(
        userId: String,
    ): Call<Unit> {
        return retrofitApi.unmuteUser(
            connectionId = connectionId,
            body = MuteUserRequest(userId, this.userId)
        ).toUnitCall()
    }

    override fun flagUser(userId: String): Call<Flag> {
        return flag(mutableMapOf("target_user_id" to userId))
    }

    override fun unflagUser(userId: String): Call<Flag> {
        return unflag(mutableMapOf("target_user_id" to userId))
    }

    override fun flagMessage(messageId: String): Call<Flag> {
        return flag(mutableMapOf("target_message_id" to messageId))
    }

    override fun unflagMessage(messageId: String): Call<Flag> {
        return unflag(mutableMapOf("target_message_id" to messageId))
    }

    private fun flag(body: MutableMap<String, String>): Call<Flag> {
        return retrofitApi.flag(
            connectionId = connectionId,
            body = body
        ).map { it.flag }
    }

    private fun unflag(body: MutableMap<String, String>): Call<Flag> {
        return retrofitApi.unflag(
            connectionId = connectionId,
            body = body
        ).map { it.flag }
    }

    override fun banUser(
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return retrofitApi.banUser(
            connectionId = connectionId,
            body = BanUserRequest(
                targetUserId = targetId,
                timeout = timeout,
                reason = reason,
                channelType = channelType,
                channelId = channelId,
                shadow = shadow,
            )
        ).toUnitCall()
    }

    override fun unbanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return retrofitApi.unbanUser(
            connectionId = connectionId,
            targetUserId = targetId,
            channelId = channelId,
            channelType = channelType,
            shadow = shadow,
        ).toUnitCall()
    }

    override fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySort<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
    ): Call<List<BannedUser>> {
        return retrofitApi.queryBannedUsers(
            connectionId = connectionId,
            payload = QueryBannedUsersRequest(
                filter = filter,
                sort = sort.toDto(),
                offset = offset,
                limit = limit,
                createdAtAfter = createdAtAfter,
                createdAtAfterOrEqual = createdAtAfterOrEqual,
                createdAtBefore = createdAtBefore,
                createdAtBeforeOrEqual = createdAtBeforeOrEqual,
            )
        ).map { it.bans.map(BannedUserResponse::toDomain) }
    }

    override fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
    ): Call<ChatEvent> {
        val map = mutableMapOf<Any, Any>()
        map["type"] = eventType
        map.putAll(extraData)

        return retrofitApi.sendEvent(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            request = SendEventRequest(map)
        ).map { it.event }
    }

    override fun translate(messageId: String, language: String): Call<Message> {
        return retrofitApi.translate(
            messageId = messageId,
            connectionId = connectionId,
            request = TranslateMessageRequest(language)
        ).map { it.message }
    }

    override fun getSyncHistory(channelIds: List<String>, lastSyncAt: Date): Call<List<ChatEvent>> {
        return retrofitApi.getSyncHistory(
            body = GetSyncHistory(channelIds, lastSyncAt),
            connectionId = connectionId
        ).map { it.events }
    }

    override fun warmUp() {
        retrofitApi.warmUp().enqueue()
    }

    private fun <T : Any> noConnectionIdError(): ErrorCall<T> {
        return ErrorCall(ChatError("setUser is either not called or not finished"))
    }

    private fun flattenChannels(responses: List<ChannelResponse>): List<Channel> {
        return responses.map {
            flattenChannel(it)
        }
    }

    private fun flattenChannel(response: ChannelResponse): Channel {
        response.channel.watcherCount = response.watcher_count
        response.channel.read = response.read.orEmpty()
        response.channel.members = response.members.orEmpty()
        response.channel.messages = response.messages.orEmpty().map { it.enrichWithCid(response.channel.cid) }
        response.channel.watchers = response.watchers.orEmpty()
        response.channel.hidden = response.hidden
        response.channel.hiddenMessagesBefore = response.hide_messages_before
        return response.channel
    }

    private fun verifyMessageId(message: Message) {
        if (message.id.isEmpty()) {
            message.id = userId + "-" + uuidGenerator.generate()
        }
    }
}
