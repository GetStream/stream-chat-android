package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.events.ConnectionEvent
import io.getstream.chat.android.core.poc.library.requests.QueryUsers
import io.getstream.chat.android.core.poc.library.events.ConnectedEvent
import io.getstream.chat.android.core.poc.library.rest.*
import io.getstream.chat.android.core.poc.library.socket.ChatObservable
import io.getstream.chat.android.core.poc.library.socket.ChatSocket


internal class ChatClientImpl constructor(
    private val api: ChatApi,
    private val socket: ChatSocket,
    private val config:ChatClientBuilder.ChatConfig
) : ChatClient {

    private val state = ClientState()

    init {
        socket.events().subscribe {
            if (it is ConnectedEvent) {
                state.user = it.me
                state.connectionId = it.connectionId
                api.setConnection(it.me.id, it.connectionId)
            }
        }
    }

    override fun setUser(user: User, provider: TokenProvider) {
        socket.connect(user, provider)
    }

    override fun setAnonymousUser(): ChatObservable {
        eventsSub = socket.events().subscribe {
            if (it is ConnectionEvent) {
                state.user = it.me
                state.connectionId = it.connectionId
                api.setConnection(it.me.id, it.connectionId)
            }
        }

        return socket.connect()
    }

    override fun setGuestUser(user: User): ChatObservable? {
        config.isAnonimous = true

        eventsSub = socket.events().subscribe {
            if (it is ConnectionEvent) {
                state.user = it.me
                state.connectionId = it.connectionId
                api.setConnection(it.me.id, it.connectionId)
            }
        }

        var chatObservable: ChatObservable? = null

        api.setGuestUser(
            userId = user.id,
            userName = user.name
        ).enqueue { result ->
            if (result.isSuccess) {
                state.user = user
                val provider = object : TokenProvider {
                    override fun getToken(listener: TokenProvider.TokenProviderListener) {
                        listener.onSuccess(result.data().access_token)
                    }
                }

                chatObservable = socket.connect(user, provider)
            }
        }

        return chatObservable
    }

    override fun events(): ChatObservable {
        return socket.events()
    }

    override fun getState(): ClientState {
        return state
    }

    override fun fromCurrentUser(entity: UserEntity): Boolean {
        val otherUserId = entity.getUserId()
        return if (getUser() == null) false else TextUtils.equals(getUserId(), otherUserId)
    }

    override fun getUserId(): String {
        return state.user?.id!!
    }

    override fun getClientId(): String {
        return state.connectionId!!
    }

    override fun disconnect() {
        socket.disconnect()
        state.reset()
    }

    //region: api calls

    override fun getDevices(): ChatCall<List<Device>> {
        return api.getDevices()
    }

    override fun deleteDevice(deviceId: String): ChatCall<Unit> {
        return api.deleteDevice(deviceId)
    }

    override fun addDevice(request: AddDeviceRequest): ChatCall<Unit> {
        return api.addDevice(request)
    }

    override fun searchMessages(request: SearchMessagesRequest): ChatCall<List<Message>> {
        return api.searchMessages(request)
    }

    override fun getReplies(messageId: String, limit: Int): ChatCall<List<Message>> {
        return api.getReplies(messageId, limit)
    }

    override fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int
    ): ChatCall<List<Message>> {
        return api.getRepliesMore(messageId, firstId, limit)
    }

    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int
    ): ChatCall<List<Reaction>> {
        return api.getReactions(messageId, offset, limit)
    }

    override fun deleteReaction(messageId: String, reactionType: String): ChatCall<Message> {
        return api.deleteReaction(messageId, reactionType)
    }

    override fun sendAction(request: SendActionRequest): ChatCall<Message> {
        return api.sendAction(request)
    }

    override fun deleteMessage(messageId: String): ChatCall<Message> {
        return api.deleteMessage(messageId)
    }

    override fun getMessage(messageId: String): ChatCall<Message> {
        return api.getMessage(messageId)
    }

    override fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): ChatCall<Message> {
        return api.sendMessage(channelType, channelId, message)
    }

    override fun updateMessage(
        message: Message
    ): ChatCall<Message> {
        return api.updateMessage(message)
    }

    override fun queryChannel(
        channelType: String,
        channelId: String,
        request: ChannelQueryRequest
    ): ChatCall<Channel> {
        return api.queryChannel(channelType, channelId, request).map { attachClient(it) }
    }

    override fun deleteChannel(channelType: String, channelId: String): ChatCall<Channel> {
        return api.deleteChannel(channelType, channelId)
    }

    override fun markRead(
        channelType: String,
        channelId: String,
        messageId: String
    ): ChatCall<Unit> {
        return api.markRead(channelType, channelId, messageId)
    }

    override fun showChannel(channelType: String, channelId: String): ChatCall<Unit> {
        return api.showChannel(channelType, channelId)
    }

    override fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean
    ): ChatCall<Unit> {
        return api.hideChannel(channelType, channelId, clearHistory)
    }

    override fun stopWatching(channelType: String, channelId: String): ChatCall<Unit> {
        return api.stopWatching(channelType, channelId)
    }

    override fun queryChannels(
        request: QueryChannelsRequest
    ): ChatCall<List<Channel>> {
        return api.queryChannels(request)
            .map { response -> response.getChannels() }
            .map { attachClient(it) }
    }

    override fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message,
        channelExtraData: Map<String, Any>
    ): ChatCall<Channel> {

        val toMutableMap = channelExtraData.toMutableMap()
        toMutableMap.remove("members")

        val request = UpdateChannelRequest(channelExtraData, updateMessage)
        return api.updateChannel(channelType, channelId, request)
            .map { response -> response.channel }
            .map { attachClient(it) }
    }

    override fun rejectInvite(channelType: String, channelId: String): ChatCall<Channel> {
        return api.rejectInvite(channelType, channelId).map { attachClient(it) }
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String
    ): ChatCall<Channel> {
        return api.acceptInvite(channelType, channelId, message).map { attachClient(it) }
    }

    override fun markAllRead(): ChatCall<ChatEvent> {
        return api.markAllRead().map {
            it.event
        }
    }

    override fun getUsers(query: QueryUsers): ChatCall<List<User>> {
        return api.getUsers(
            queryUser = query
        ).map { it.users }
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): ChatCall<ChannelResponse> {
        return api.addMembers(
            channelType = channelType,
            channelId = channelId,
            members = members
        )
    }

    override fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ) = api.removeMembers(
        channelType = channelType,
        channelId = channelId,
        members = members
    )

    override fun muteUser(targetId: String) = api.muteUser(
        targetId = targetId
    )

    override fun unMuteUser(targetId: String) = api.unMuteUser(
        targetId = targetId
    )

    override fun flag(targetId: String) = api.flag(
        targetId = targetId
    )

    override fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String?,
        timeout: Int?
    ): ChatCall<CompletableResponse> = api.banUser(
        targetId, timeout, reason, channelType, channelId
    )

    override fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ) = api.unBanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId
    )

    //endregion

    private fun attachClient(channels: List<Channel>): List<Channel> {
        channels.forEach { attachClient(it) }
        return channels
    }

    private fun attachClient(channel: Channel): Channel {
        channel.client = this
        return channel
    }

    private fun getUser(): User? {
        return state.user
    }
}