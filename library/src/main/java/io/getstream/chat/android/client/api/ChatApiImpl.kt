package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.*
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.UuidGenerator
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

class ChatApiImpl(
    private val apiKey: String,
    private val retrofitApi: RetrofitApi,
    private val retrofitCdnApi: RetrofitCdnApi,
    private val parser: ChatParser,
    private val uuidGenerator: UuidGenerator
) : ChatApi {

    private var userId: String = ""
    private var connectionId: String = ""
    private val callMapper = RetrofitCallMapper(parser)

    override fun setConnection(userId: String, connectionId: String) {
        this.userId = userId
        this.connectionId = connectionId
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ) {
        val body = ProgressRequestBody(
            file,
            null,
            callback
        )
        val part = createFormData("file", file.name, body)

        retrofitCdnApi.sendFile(
            channelType,
            channelId,
            part,
            apiKey,
            userId,
            connectionId
        ).enqueue(RetroProgressCallback(callback))
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ) {
        val body = ProgressRequestBody(
            file,
            null,
            callback
        )
        val part = createFormData("file", file.name, body)

        retrofitCdnApi.sendImage(
            channelType,
            channelId,
            part,
            apiKey,
            userId,
            connectionId
        ).enqueue(RetroProgressCallback(callback))
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String> {

        val part = createFormData("file", file.name, file.asRequestBody())

        return callMapper.map(
            retrofitCdnApi.sendFile(
                channelType,
                channelId,
                part,
                apiKey,
                userId,
                connectionId
            )
        ).map { it.file }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String> {

        val part = createFormData("file", file.name, file.asRequestBody())

        return callMapper.map(
            retrofitCdnApi.sendImage(
                channelType,
                channelId,
                part,
                apiKey,
                userId,
                connectionId
            )
        ).map { it.file }
    }

    override fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return callMapper.map(
            retrofitCdnApi.deleteFile(channelType, channelId, apiKey, connectionId, url)
        ).map {
            Unit
        }
    }

    override fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return callMapper.map(
            retrofitCdnApi.deleteImage(channelType, channelId, apiKey, connectionId, url)
        ).map {
            Unit
        }
    }

    override fun addDevice(firebaseToken: String): Call<Unit> {
        return callMapper.map(
            retrofitApi.addDevices(
                apiKey,
                userId,
                connectionId,
                AddDeviceRequest(firebaseToken)
            )
        ).map {
            Unit
        }
    }

    override fun deleteDevice(firebaseToken: String): Call<Unit> {
        return callMapper.map(
            retrofitApi.deleteDevice(
                firebaseToken, apiKey, userId, connectionId
            )
        ).map {
            Unit
        }
    }

    override fun getDevices(): Call<List<Device>> {
        return callMapper.map(
            retrofitApi.getDevices(apiKey, userId, connectionId)
        ).map {
            it.devices
        }
    }

    override fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        return callMapper.map(
            retrofitApi.searchMessages(apiKey, connectionId, request)
        ).map {
            it.results.map { resp ->
                resp.message
            }
        }
    }

    override fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int
    ): Call<List<Message>> {
        return callMapper.map(
            retrofitApi.getRepliesMore(
                messageId,
                apiKey,
                userId,
                connectionId,
                limit,
                firstId
            )
        ).map {
            it.messages
        }
    }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return callMapper.map(
            retrofitApi.getReplies(
                messageId,
                apiKey,
                userId,
                connectionId,
                limit
            )
        ).map {
            it.messages
        }
    }

    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int
    ): Call<List<Reaction>> {
        return callMapper.map(
            retrofitApi.getReactions(
                messageId,
                apiKey,
                connectionId,
                offset,
                limit
            )
        ).map {
            it.reactions
        }
    }

    override fun sendReaction(reaction: Reaction): Call<Reaction> {

        return callMapper.map(
            retrofitApi.sendReaction(
                reaction.messageId, apiKey, userId, connectionId, ReactionRequest(reaction)
            )
        ).map {
            it.reaction
        }
    }

    override fun sendReaction(messageId: String, reactionType: String): Call<Reaction> {
        return sendReaction(Reaction(messageId, reactionType, 0))
    }

    override fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return callMapper.map(
            retrofitApi.deleteReaction(
                messageId,
                reactionType,
                apiKey,
                userId,
                connectionId
            )
        ).map {
            it.message
        }
    }

    override fun deleteMessage(messageId: String): Call<Message> {
        return callMapper.map(
            retrofitApi.deleteMessage(
                messageId,
                apiKey,
                userId,
                connectionId
            )
        ).map {
            it.message
        }
    }

    override fun sendAction(request: SendActionRequest): Call<Message> {
        return callMapper.map(
            retrofitApi.sendAction(
                request.messageId, apiKey, userId, connectionId, request
            )
        ).map {
            it.message
        }
    }

    override fun getMessage(messageId: String): Call<Message> {
        return callMapper.map(
            retrofitApi.getMessage(messageId, apiKey, userId, connectionId)
        ).map {
            it.message
        }
    }

    override fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): Call<Message> {

        verifyMessageId(message)

        return callMapper.map(
            retrofitApi.sendMessage(
                channelType,
                channelId,
                apiKey,
                userId,
                connectionId,
                MessageRequest(message)
            )
        ).map { it.message }
    }

    override fun updateMessage(
        message: Message
    ): Call<Message> {
        return callMapper.map(
            retrofitApi.updateMessage(
                message.id,
                apiKey,
                userId,
                connectionId,
                MessageRequest(message)
            )
        ).map {
            it.message
        }
    }


    override fun stopWatching(
        channelType: String,
        channelId: String
    ): Call<Unit> {
        return callMapper.map(
            retrofitApi.stopWatching(
                channelType,
                channelId,
                apiKey,
                connectionId,
                emptyMap()
            )
        ).map {
            Unit
        }
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {

        if (connectionId.isEmpty()) return noConnectionIdError()

        return callMapper.map(
            retrofitApi.queryChannels(
                apiKey,
                userId,
                connectionId,
                query
            )
        ).map {
            flattenChannels(it.channels)
        }
    }

    override fun queryChannel(
        channelType: String,
        channelId: String,
        query: ChannelQueryRequest
    ): Call<Channel> {

        if (connectionId.isEmpty()) {
            return noConnectionIdError()
        }

        if (channelId.isEmpty()) {
            return callMapper.map(
                retrofitApi.queryChannel(
                    channelType,
                    apiKey,
                    userId,
                    connectionId,
                    query
                )
            ).map { flattenChannel(it) }
        } else {
            return callMapper.map(
                retrofitApi.queryChannel(
                    channelType,
                    channelId,
                    apiKey,
                    userId,
                    connectionId,
                    query
                )
            ).map { flattenChannel(it) }
        }
    }

    override fun updateChannel(
        channelType: String,
        channelId: String,
        request: UpdateChannelRequest
    ): Call<Channel> {
        return callMapper.map(
            retrofitApi.updateChannel(
                channelType,
                channelId,
                apiKey,
                connectionId,
                request
            )
        ).map { flattenChannel(it) }
    }

    override fun markRead(
        channelType: String,
        channelId: String,
        messageId: String
    ): Call<Unit> {
        return callMapper.map(
            retrofitApi.markRead(
                channelType,
                channelId,
                apiKey,
                userId,
                connectionId,
                MarkReadRequest(messageId)
            )
        ).map {
            Unit
        }
    }

    override fun showChannel(channelType: String, channelId: String): Call<Unit> {
        return callMapper.map(
            retrofitApi.showChannel(channelType, channelId, apiKey, connectionId, emptyMap())
        ).map {
            Unit
        }
    }

    override fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean
    ): Call<Unit> {
        return callMapper.map(
            retrofitApi.hideChannel(
                channelType,
                channelId,
                apiKey,
                connectionId,
                HideChannelRequest(clearHistory)
            )
        ).map {
            Unit
        }
    }

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return callMapper.map(
            retrofitApi.rejectInvite(
                channelType, channelId, apiKey, connectionId, RejectInviteRequest()
            )
        ).map {
            flattenChannel(it)
        }
    }

    override fun muteCurrentUser(): Call<Mute> {
        return muteUser(userId)
    }

    override fun unmuteCurrentUser(): Call<Mute> {
        return unmuteUser(userId)
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String
    ): Call<Channel> {
        return callMapper.map(
            retrofitApi.acceptInvite(
                channelType,
                channelId,
                apiKey,
                connectionId,
                AcceptInviteRequest(
                    User().apply { id = userId },
                    AcceptInviteRequest.AcceptInviteMessage(message)
                )
            )
        ).map {
            flattenChannel(it)
        }
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return callMapper.map(
            retrofitApi.deleteChannel(channelType, channelId, apiKey, connectionId)
        ).map {
            flattenChannel(it)
        }
    }

    override fun markAllRead(): Call<EventResponse> {
        return callMapper.map(
            retrofitApi.markAllRead(
                apiKey,
                userId,
                connectionId
            )
        )
    }

    override fun getGuestUser(userId: String, userName: String): Call<GuestUser> {
        return callMapper.map(
            retrofitApi.getGuestUser(apiKey, GuestUserRequest(userId, userName))
        ).map {
            GuestUser(it.user, it.accessToken)
        }
    }

    override fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>> {
        return callMapper.map(
            retrofitApi.queryUsers(
                apiKey,
                connectionId,
                queryUsers
            )
        ).map { it.users }
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ) = callMapper.map(
        retrofitApi.addMembers(
            apiKey,
            connectionId,
            channelType,
            channelId,
            AddMembersRequest(members)
        )
    ).map { flattenChannel(it) }

    override fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ) = callMapper.map(
        retrofitApi.removeMembers(
            channelType,
            channelId,
            apiKey,
            connectionId,
            RemoveMembersRequest(members)
        )
    ).map { flattenChannel(it) }

    override fun muteUser(
        userId: String
    ): Call<Mute> {
        return callMapper.map(
            retrofitApi.muteUser(
                apiKey,
                this.userId,
                connectionId,
                MuteUserRequest(userId, this.userId)
            )
        ).map { it.mute }
    }

    override fun unmuteUser(
        userId: String
    ): Call<Mute> {

        return callMapper.map(
            retrofitApi.unMuteUser(
                apiKey,
                this.userId,
                connectionId,
                MuteUserRequest(userId, this.userId)
            )
        ).map { it.mute }
    }

    override fun flag(
        targetId: String
    ): Call<Flag> {

        val body: MutableMap<String, String> = HashMap()
        body["target_user_id"] = targetId

        return callMapper.map(
            retrofitApi.flag(
                apiKey,
                userId,
                connectionId,
                body
            )
        ).map { it.flag }
    }

    override fun banUser(
        targetId: String,
        timeout: Int,
        reason: String,
        channelType: String,
        channelId: String
    ): Call<CompletableResponse> {

        return callMapper.map(
            retrofitApi.banUser(
                apiKey = apiKey,
                connectionId = connectionId,
                body = BanUserRequest(
                    targetUserId = targetId,
                    timeout = timeout,
                    reason = reason,
                    channelType = channelType,
                    channelId = channelId
                )
            )
        )
    }

    override fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): Call<CompletableResponse> {

        return callMapper.map(
            retrofitApi.unBanUser(
                apiKey = apiKey,
                connectionId = connectionId,
                targetUserId = targetId,
                channelId = channelId,
                channelType = channelType
            )
        )
    }

    override fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>
    ): Call<ChatEvent> {

        val map = mutableMapOf<Any, Any>()
        map["type"] = eventType
        map.putAll(extraData)

        return callMapper.map(
            retrofitApi.sendEvent(
                channelType,
                channelId,
                apiKey,
                userId,
                connectionId,
                SendEventRequest(map)
            )
        ).map {
            it.event
        }
    }

    private fun <T> noConnectionIdError(): ErrorCall<T> {
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
        response.channel.messages = response.messages.orEmpty()
        response.channel.watchers = response.watchers.orEmpty()
        return response.channel
    }

    private fun verifyMessageId(message: Message) {
        if (message.id.isEmpty()) {
            message.id = userId + "-" + uuidGenerator.generate()
        }
    }

}