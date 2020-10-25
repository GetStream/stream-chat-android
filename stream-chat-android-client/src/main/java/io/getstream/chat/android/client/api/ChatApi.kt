package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.AcceptInviteRequest
import io.getstream.chat.android.client.api.models.AddDeviceRequest
import io.getstream.chat.android.client.api.models.AddMembersRequest
import io.getstream.chat.android.client.api.models.BanUserRequest
import io.getstream.chat.android.client.api.models.ChannelResponse
import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.GetSyncHistory
import io.getstream.chat.android.client.api.models.GuestUserRequest
import io.getstream.chat.android.client.api.models.HideChannelRequest
import io.getstream.chat.android.client.api.models.MarkReadRequest
import io.getstream.chat.android.client.api.models.MessageRequest
import io.getstream.chat.android.client.api.models.MuteChannelRequest
import io.getstream.chat.android.client.api.models.MuteUserRequest
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
import io.getstream.chat.android.client.api.models.UpdateChannelRequest
import io.getstream.chat.android.client.api.models.UpdateCooldownRequest
import io.getstream.chat.android.client.api.models.UpdateUsersRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
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
import io.getstream.chat.android.client.uploader.FileUploaderCall
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.UuidGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import java.io.File
import java.util.Date
import kotlin.collections.set

internal class ChatApi(
    private val apiKey: String,
    private val retrofitApi: RetrofitApi,
    private val retrofitAnonymousApi: RetrofitAnonymousApi,
    private val uuidGenerator: UuidGenerator,
    private val fileUploader: FileUploader,
    private val coroutineScope: CoroutineScope = GlobalScope
) {

    private var userId: String = ""
    private var connectionId: String = ""

    fun setConnection(userId: String, connectionId: String) {
        this.userId = userId
        this.connectionId = connectionId
    }

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ): Unit = fileUploader.sendFile(
        channelType = channelType,
        channelId = channelId,
        userId = userId,
        connectionId = connectionId,
        file = file,
        callback = callback
    )

    fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ): Unit = fileUploader.sendImage(
        channelType = channelType,
        channelId = channelId,
        userId = userId,
        connectionId = connectionId,
        file = file,
        callback = callback
    )

    fun sendFile(channelType: String, channelId: String, file: File): Call<String> {
        return FileUploaderCall(coroutineScope) {
            val result = fileUploader.sendFile(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                connectionId = connectionId,
                file = file
            )
            Result(result, null)
        }
    }

    fun sendImage(channelType: String, channelId: String, file: File): Call<String> {
        return FileUploaderCall(coroutineScope) {
            val result = fileUploader.sendImage(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                connectionId = connectionId,
                file = file
            )
            Result(result, null)
        }
    }

    fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return FileUploaderCall(coroutineScope) {
            fileUploader.deleteFile(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                connectionId = connectionId,
                url = url
            )
            Result(Unit, null)
        }
    }

    fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return FileUploaderCall(coroutineScope) {
            fileUploader.deleteImage(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                connectionId = connectionId,
                url = url
            )
            Result(Unit, null)
        }
    }

    fun addDevice(firebaseToken: String): Call<Unit> {
        return retrofitApi.addDevices(
            apiKey,
            userId,
            connectionId,
            AddDeviceRequest(firebaseToken)
        ).map { Unit }
    }

    fun deleteDevice(firebaseToken: String): Call<Unit> {
        return retrofitApi.deleteDevice(firebaseToken, apiKey, userId, connectionId)
            .map { Unit }
    }

    fun getDevices(): Call<List<Device>> {
        return retrofitApi.getDevices(apiKey, userId, connectionId)
            .map { it.devices }
    }

    fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        return retrofitApi.searchMessages(apiKey, connectionId, request)
            .map {
                it.results.map { resp ->
                    resp.message
                }
            }
    }

    fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int
    ): Call<List<Message>> {
        return retrofitApi.getRepliesMore(
            messageId,
            apiKey,
            userId,
            connectionId,
            limit,
            firstId
        ).map { it.messages }
    }

    fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return retrofitApi.getReplies(
            messageId,
            apiKey,
            userId,
            connectionId,
            limit
        ).map { it.messages }
    }

    fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int
    ): Call<List<Reaction>> {
        return retrofitApi.getReactions(
            messageId,
            apiKey,
            connectionId,
            offset,
            limit
        ).map { it.reactions }
    }

    fun sendReaction(reaction: Reaction): Call<Reaction> {
        return retrofitApi.sendReaction(
            reaction.messageId,
            apiKey,
            userId,
            connectionId,
            ReactionRequest(reaction)
        ).map { it.reaction }
    }

    fun sendReaction(messageId: String, reactionType: String): Call<Reaction> {
        return sendReaction(Reaction(messageId, reactionType, 0))
    }

    fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return retrofitApi.deleteReaction(
            messageId,
            reactionType,
            apiKey,
            userId,
            connectionId
        ).map { it.message }
    }

    fun deleteMessage(messageId: String): Call<Message> {
        return retrofitApi.deleteMessage(
            messageId,
            apiKey,
            userId,
            connectionId
        ).map { it.message }
    }

    fun sendAction(request: SendActionRequest): Call<Message> {
        return retrofitApi.sendAction(
            request.messageId,
            apiKey,
            userId,
            connectionId,
            request
        ).map { it.message }
    }

    fun getMessage(messageId: String): Call<Message> {
        return retrofitApi.getMessage(messageId, apiKey, userId, connectionId)
            .map { it.message }
    }

    fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): Call<Message> {

        verifyMessageId(message)

        return retrofitApi.sendMessage(
            channelType,
            channelId,
            apiKey,
            userId,
            connectionId,
            MessageRequest(message)
        ).map { it.message }
    }

    fun muteChannel(channelType: String, channelId: String): Call<Unit> {
        return retrofitApi.muteChannel(
            apiKey,
            userId,
            connectionId,
            MuteChannelRequest("$channelType:$channelId")
        ).map { Unit }
    }

    fun unMuteChannel(channelType: String, channelId: String): Call<Unit> {
        return retrofitApi.unMuteChannel(
            apiKey,
            userId,
            connectionId,
            MuteChannelRequest("$channelType:$channelId")
        ).map { Unit }
    }

    fun updateMessage(
        message: Message
    ): Call<Message> {
        return retrofitApi.updateMessage(
            message.id,
            apiKey,
            userId,
            connectionId,
            MessageRequest(message)
        ).map { it.message }
    }

    fun stopWatching(
        channelType: String,
        channelId: String
    ): Call<Unit> {
        return retrofitApi.stopWatching(
            channelType,
            channelId,
            apiKey,
            connectionId,
            emptyMap()
        ).map { Unit }
    }

    fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {

        if (connectionId.isEmpty()) return noConnectionIdError()

        return retrofitApi.queryChannels(
            apiKey,
            userId,
            connectionId,
            query
        ).map {
            flattenChannels(it.channels)
        }
    }

    fun updateUsers(users: List<User>): Call<List<User>> {

        val map = users.associateBy({ it.id }, { user -> user })

        return retrofitApi.updateUsers(
            connectionId,
            UpdateUsersRequest(map)
        )
            .map { response ->
                response.users.flatMap {
                    listOf(it.value)
                }
            }
    }

    fun queryChannel(
        channelType: String,
        channelId: String = "",
        query: QueryChannelRequest
    ): Call<Channel> {

        if (connectionId.isEmpty()) {
            return noConnectionIdError()
        }

        if (channelId.isEmpty()) {
            return retrofitApi.queryChannel(
                channelType,
                apiKey,
                userId,
                connectionId,
                query
            ).map { flattenChannel(it) }
        } else {
            return retrofitApi.queryChannel(
                channelType,
                channelId,
                apiKey,
                userId,
                connectionId,
                query
            ).map { flattenChannel(it) }
        }
    }

    fun updateChannel(
        channelType: String,
        channelId: String,
        request: UpdateChannelRequest
    ): Call<Channel> {
        return retrofitApi.updateChannel(
            channelType,
            channelId,
            apiKey,
            connectionId,
            request
        ).map { flattenChannel(it) }
    }

    fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int
    ): Call<Channel> = updateCooldown(channelType, channelId, cooldownTimeInSeconds)

    fun disableSlowMode(
        channelType: String,
        channelId: String
    ): Call<Channel> = updateCooldown(channelType, channelId, 0)

    private fun updateCooldown(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int
    ): Call<Channel> =
        retrofitApi.updateCooldown(
            channelType,
            channelId,
            apiKey,
            connectionId,
            UpdateCooldownRequest(cooldownTimeInSeconds)
        ).map { flattenChannel(it) }

    fun markRead(
        channelType: String,
        channelId: String,
        messageId: String = ""
    ): Call<Unit> {
        return retrofitApi.markRead(
            channelType,
            channelId,
            apiKey,
            userId,
            connectionId,
            MarkReadRequest(messageId)
        ).map { Unit }
    }

    fun showChannel(channelType: String, channelId: String): Call<Unit> {
        return retrofitApi.showChannel(channelType, channelId, apiKey, connectionId, emptyMap())
            .map { Unit }
    }

    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean
    ): Call<Unit> {
        return retrofitApi.hideChannel(
            channelType,
            channelId,
            apiKey,
            connectionId,
            HideChannelRequest(clearHistory)
        ).map { Unit }
    }

    fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return retrofitApi.rejectInvite(
            channelType,
            channelId,
            apiKey,
            connectionId,
            RejectInviteRequest()
        ).map { flattenChannel(it) }
    }

    fun muteCurrentUser(): Call<Mute> {
        return muteUser(userId)
    }

    fun unmuteCurrentUser(): Call<Mute> {
        return unmuteUser(userId)
    }

    fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String
    ): Call<Channel> {
        return retrofitApi.acceptInvite(
            channelType,
            channelId,
            apiKey,
            connectionId,
            AcceptInviteRequest(
                User().apply { id = userId },
                AcceptInviteRequest.AcceptInviteMessage(message)
            )
        ).map { flattenChannel(it) }
    }

    fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return retrofitApi.deleteChannel(channelType, channelId, apiKey, connectionId)
            .map { flattenChannel(it) }
    }

    fun markAllRead(): Call<Unit> {
        return retrofitApi.markAllRead(
            apiKey,
            userId,
            connectionId
        ).map { Unit }
    }

    fun getGuestUser(userId: String, userName: String): Call<GuestUser> {
        return retrofitAnonymousApi.getGuestUser(apiKey, GuestUserRequest(userId, userName))
            .map {
                GuestUser(it.user, it.accessToken)
            }
    }

    fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>> {
        return retrofitApi.queryUsers(
            apiKey,
            connectionId,
            queryUsers
        ).map { it.users }
    }

    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel> {
        return retrofitApi.addMembers(
            channelType,
            channelId,
            apiKey,
            connectionId,
            AddMembersRequest(members)
        ).map { flattenChannel(it) }
    }

    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel> {
        return retrofitApi.removeMembers(
            channelType,
            channelId,
            apiKey,
            connectionId,
            RemoveMembersRequest(members)
        ).map { flattenChannel(it) }
    }

    fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort,
        members: List<Member>
    ): Call<List<Member>> {
        return retrofitApi.queryMembers(
            apiKey,
            connectionId,
            QueryMembersRequest(
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

    fun muteUser(
        userId: String
    ): Call<Mute> {
        return retrofitApi.muteUser(
            apiKey,
            this.userId,
            connectionId,
            MuteUserRequest(userId, this.userId)
        ).map { it.mute }
    }

    fun unmuteUser(
        userId: String
    ): Call<Mute> {
        return retrofitApi.unMuteUser(
            apiKey,
            this.userId,
            connectionId,
            MuteUserRequest(userId, this.userId)
        ).map { it.mute }
    }

    @Deprecated(
        message = "We are going to replace with flagUser()",
        replaceWith = ReplaceWith("this.flagUser(userId)")
    )
    fun flag(targetId: String): Call<Flag> = flagUser(targetId)

    fun flagUser(userId: String): Call<Flag> =
        flag(mutableMapOf("target_user_id" to userId))

    fun flagMessage(messageId: String): Call<Flag> =
        flag(mutableMapOf("target_message_id" to messageId))

    private fun flag(body: MutableMap<String, String>): Call<Flag> {
        return retrofitApi.flag(
            apiKey,
            userId,
            connectionId,
            body
        ).map { it.flag }
    }

    fun banUser(
        targetId: String,
        timeout: Int,
        reason: String,
        channelType: String,
        channelId: String
    ): Call<CompletableResponse> {
        return retrofitApi.banUser(
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
    }

    fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): Call<CompletableResponse> {
        return retrofitApi.unBanUser(
            apiKey = apiKey,
            connectionId = connectionId,
            targetUserId = targetId,
            channelId = channelId,
            channelType = channelType
        )
    }

    fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>
    ): Call<ChatEvent> {
        val map = mutableMapOf<Any, Any>()
        map["type"] = eventType
        map.putAll(extraData)

        return retrofitApi.sendEvent(
            channelType,
            channelId,
            apiKey,
            userId,
            connectionId,
            SendEventRequest(map)
        ).map { it.event }
    }

    fun translate(messageId: String, language: String): Call<Message> {
        return retrofitApi.translate(
            messageId,
            apiKey,
            userId,
            connectionId,
            TranslateMessageRequest(language)
        ).map { it.message }
    }

    fun getSyncHistory(channelIds: List<String>, lastSyncAt: Date): Call<List<ChatEvent>> {
        return retrofitApi.getSyncHistory(
            GetSyncHistory(channelIds, lastSyncAt),
            apiKey,
            userId,
            connectionId
        ).map { it.events }
    }

    fun warmUp() {
        retrofitApi.warmUp().enqueue { }
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
        response.channel.messages = response.messages.orEmpty()
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
