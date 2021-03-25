package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.mapping.toDto
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.api2.model.requests.AcceptInviteRequest
import io.getstream.chat.android.client.api2.model.requests.AddDeviceRequest
import io.getstream.chat.android.client.api2.model.requests.AddMembersRequest
import io.getstream.chat.android.client.api2.model.requests.BanUserRequest
import io.getstream.chat.android.client.api2.model.requests.GuestUserRequest
import io.getstream.chat.android.client.api2.model.requests.HideChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MarkReadRequest
import io.getstream.chat.android.client.api2.model.requests.MessageRequest
import io.getstream.chat.android.client.api2.model.requests.MuteChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MuteUserRequest
import io.getstream.chat.android.client.api2.model.requests.QueryBannedUsersRequest
import io.getstream.chat.android.client.api2.model.requests.ReactionRequest
import io.getstream.chat.android.client.api2.model.requests.RejectInviteRequest
import io.getstream.chat.android.client.api2.model.requests.RemoveMembersRequest
import io.getstream.chat.android.client.api2.model.requests.SendActionRequest
import io.getstream.chat.android.client.api2.model.requests.SendEventRequest
import io.getstream.chat.android.client.api2.model.requests.SyncHistoryRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateCooldownRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateUsersRequest
import io.getstream.chat.android.client.api2.model.response.BannedUserResponse
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.TranslateMessageRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.call.toUnitCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.logger.ChatLogger
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
import io.getstream.chat.android.client.parser.toMap
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import java.io.File
import java.util.Date
import io.getstream.chat.android.client.api.models.SendActionRequest as DomainSendActionRequest

internal class MoshiChatApi(
    private val fileUploader: FileUploader,
    private val userApi: UserApi,
    private val guestApi: GuestApi,
    private val messageApi: MessageApi,
    private val channelApi: ChannelApi,
    private val deviceApi: DeviceApi,
    private val moderationApi: ModerationApi,
    private val generalApi: GeneralApi,
    private val coroutineScope: CoroutineScope = GlobalScope,
) : ChatApi {

    val logger = ChatLogger.get("MoshiChatApi")

    lateinit var userId: String
    lateinit var connectionId: String

    override fun setConnection(userId: String, connectionId: String) {
        this.userId = userId
        this.connectionId = connectionId
    }

    override fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message> {
        return messageApi.sendMessage(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            message = MessageRequest(message.toDto()),
        ).map { response -> response.message.toDomain() }
    }

    override fun updateMessage(message: Message): Call<Message> {
        return messageApi.updateMessage(
            messageId = message.id,
            connectionId = connectionId,
            message = MessageRequest(message.toDto()),
        ).map { response -> response.message.toDomain() }
    }

    override fun getMessage(messageId: String): Call<Message> {
        return messageApi.getMessage(
            messageId = messageId,
            connectionId = connectionId,
        ).map { response -> response.message.toDomain() }
    }

    override fun deleteMessage(messageId: String): Call<Message> {
        return messageApi.deleteMessage(
            messageId = messageId,
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
            connectionId = connectionId,
            offset = offset,
            limit = limit,
        ).map { response -> response.reactions.map(DownstreamReactionDto::toDomain) }
    }

    override fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction> {
        return messageApi.sendReaction(
            messageId = reaction.messageId,
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
            connectionId = connectionId,
        ).map { response -> response.message.toDomain() }
    }

    override fun addDevice(firebaseToken: String): Call<Unit> {
        return deviceApi.addDevices(
            connectionId = connectionId,
            request = AddDeviceRequest(id = firebaseToken),
        ).toUnitCall()
    }

    override fun deleteDevice(firebaseToken: String): Call<Unit> {
        return deviceApi.deleteDevice(
            deviceId = firebaseToken,
            connectionId = connectionId,
        ).toUnitCall()
    }

    override fun getDevices(): Call<List<Device>> {
        return deviceApi.getDevices(
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
            connectionId = connectionId,
            body = MuteUserRequest(userId, this.userId),
        ).map { response -> response.mute.toDomain() }
    }

    override fun unmuteUser(userId: String): Call<Unit> {
        return moderationApi.unmuteUser(
            connectionId = this.connectionId,
            body = MuteUserRequest(userId, this.userId),
        ).toUnitCall()
    }

    override fun muteChannel(channelType: String, channelId: String): Call<Unit> {
        return moderationApi.muteChannel(
            connectionId = connectionId,
            body = MuteChannelRequest("$channelType:$channelId"),
        ).toUnitCall()
    }

    override fun unmuteChannel(channelType: String, channelId: String): Call<Unit> {
        return moderationApi.unmuteChannel(
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

    override fun unflagUser(userId: String): Call<Flag> =
        unflag(mutableMapOf("target_user_id" to userId))

    override fun flagMessage(messageId: String): Call<Flag> =
        flag(mutableMapOf("target_message_id" to messageId))

    override fun unflagMessage(messageId: String): Call<Flag> =
        unflag(mutableMapOf("target_message_id" to messageId))

    private fun flag(body: MutableMap<String, String>): Call<Flag> {
        return moderationApi.flag(
            connectionId = connectionId,
            body = body
        ).map { response -> response.flag.toDomain() }
    }

    private fun unflag(body: MutableMap<String, String>): Call<Flag> {
        return moderationApi.unflag(
            connectionId = connectionId,
            body = body
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

    override fun unbanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return moderationApi.unbanUser(
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
        return moderationApi.queryBannedUsers(
            connectionId = connectionId,
            payload = QueryBannedUsersRequest(
                filter_conditions = filter.toMap(),
                sort = sort.toDto(),
                offset = offset,
                limit = limit,
                created_at_after = createdAtAfter,
                created_at_after_or_equal = createdAtAfterOrEqual,
                created_at_before = createdAtBefore,
                created_at_before_or_equal = createdAtBeforeOrEqual,
            )
        ).map { response -> response.bans.map(BannedUserResponse::toDomain) }
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
            connectionId = connectionId,
            body = UpdateCooldownRequest(cooldownTimeInSeconds),
        ).map(this::flattenChannel)
    }

    override fun stopWatching(channelType: String, channelId: String): Call<Unit> {
        return channelApi.stopWatching(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = emptyMap(),
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
            connectionId = connectionId,
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
            connectionId = connectionId,
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
            connectionId = connectionId,
            body = HideChannelRequest(clearHistory),
        ).toUnitCall()
    }

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return channelApi.rejectInvite(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
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
            connectionId = connectionId,
            body = AcceptInviteRequest.create(userId = userId, message = message),
        ).map(this::flattenChannel)
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return channelApi.deleteChannel(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
        ).map(this::flattenChannel)
    }

    override fun markRead(channelType: String, channelId: String, messageId: String): Call<Unit> {
        return channelApi.markRead(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            request = MarkReadRequest(messageId),
        ).toUnitCall()
    }

    override fun markAllRead(): Call<Unit> {
        return channelApi.markAllRead(
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

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return messageApi.getReplies(
            messageId = messageId,
            connectionId = connectionId,
            limit = limit,
        ).map { response -> response.messages.map(DownstreamMessageDto::toDomain) }
    }

    override fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>> {
        return messageApi.getRepliesMore(
            messageId = messageId,
            connectionId = connectionId,
            limit = limit,
            firstId = firstId,
        ).map { response -> response.messages.map(DownstreamMessageDto::toDomain) }
    }

    override fun sendAction(request: DomainSendActionRequest): Call<Message> {
        return messageApi.sendAction(
            messageId = request.messageId,
            connectionId = connectionId,
            request = SendActionRequest(
                channel_id = request.channelId,
                message_id = request.messageId,
                type = request.type,
                form_data = request.formData,
            ),
        ).map { response -> response.message.toDomain() }
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        val map: Map<String, UpstreamUserDto> = users.associateBy({ it.id }, User::toDto)
        return userApi.updateUsers(
            connectionId = connectionId,
            body = UpdateUsersRequest(map),
        ).map { response ->
            response.users.values.map(DownstreamUserDto::toDomain)
        }
    }

    override fun getGuestUser(userId: String, userName: String): Call<GuestUser> {
        return guestApi.getGuestUser(
            body = GuestUserRequest.create(userId, userName),
        ).map { response -> GuestUser(response.user.toDomain(), response.access_token) }
    }

    override fun translate(messageId: String, language: String): Call<Message> {
        return messageApi.translate(
            messageId = messageId,
            connectionId = connectionId,
            request = TranslateMessageRequest(language),
        ).map { response -> response.message.toDomain() }
    }

    override fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        val newRequest = io.getstream.chat.android.client.api2.model.requests.SearchMessagesRequest(
            offset = request.offset,
            limit = request.limit,
            filter_conditions = request.channelFilter.toMap(),
            message_filter_conditions = request.messageFilter.toMap(),
        )
        return generalApi.searchMessages(connectionId, newRequest)
            .map { response ->
                response.results.map { resp ->
                    resp.message.toDomain().apply {
                        (cid.takeUnless(CharSequence::isBlank) ?: channelInfo?.cid)
                            ?.let(::enrichWithCid)
                    }
                }
            }
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {
        if (connectionId.isEmpty()) return noConnectionIdError()

        val request = io.getstream.chat.android.client.api2.model.requests.QueryChannelsRequest(
            filter_conditions = query.filter.toMap(),
            offset = query.offset,
            limit = query.limit,
            querySort = query.sort,
            message_limit = query.messageLimit,
            member_limit = query.memberLimit,
            state = query.state,
            watch = query.watch,
            presence = query.presence,
        )

        return channelApi.queryChannels(
            connectionId = connectionId,
            payload = request,
        ).map { response -> response.channels.map(this::flattenChannel) }
    }

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        val request = io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest(
            state = query.state,
            watch = query.watch,
            presence = query.presence,
            messages = query.messages,
            watchers = query.watchers,
            members = query.members,
            data = query.data,
        )

        return if (channelId.isEmpty()) {
            channelApi.queryChannel(
                channelType = channelType,
                connectionId = connectionId,
                request = request,
            )
        } else {
            channelApi.queryChannel(
                channelType = channelType,
                channelId = channelId,
                connectionId = connectionId,
                request = request,
            )
        }.map(::flattenChannel)
    }

    override fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>> {
        val request = io.getstream.chat.android.client.api2.model.requests.QueryUsersRequest(
            filter_conditions = queryUsers.filter.toMap(),
            offset = queryUsers.offset,
            limit = queryUsers.limit,
            sort = queryUsers.sort,
            presence = queryUsers.presence,
        )
        return userApi.queryUsers(
            connectionId,
            request,
        ).map { response -> response.users.map(DownstreamUserDto::toDomain) }
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
        val request = io.getstream.chat.android.client.api2.model.requests.QueryMembersRequest(
            type = channelType,
            id = channelId,
            filter_conditions = filter.toMap(),
            offset = offset,
            limit = limit,
            sort = sort.toDto(),
            members = members.map(Member::toDto),
        )

        return generalApi.queryMembers(
            connectionId,
            request,
        ).map { response -> response.members.map(DownstreamMemberDto::toDomain) }
    }

    override fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
    ): Call<ChatEvent> {
        val map = mutableMapOf<Any, Any>("type" to eventType)
        map.putAll(extraData)

        return channelApi.sendEvent(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            request = SendEventRequest(map),
        ).map { response -> response.event.toDomain() }
    }

    override fun getSyncHistory(
        channelIds: List<String>,
        lastSyncAt: Date,
    ): Call<List<ChatEvent>> {
        return generalApi.getSyncHistory(
            body = SyncHistoryRequest(channelIds, lastSyncAt),
            connectionId = connectionId,
        ).map { response -> response.events.map(ChatEventDto::toDomain) }
    }

    override fun warmUp() {
        generalApi.warmUp().enqueue()
    }

    private fun <T : Any> noConnectionIdError(): ErrorCall<T> {
        return ErrorCall(ChatError("setUser is either not called or not finished"))
    }
}
