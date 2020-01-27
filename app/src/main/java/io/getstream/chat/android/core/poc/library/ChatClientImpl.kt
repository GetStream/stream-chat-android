package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.rest.*
import io.getstream.chat.android.core.poc.library.socket.ChatObservable
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ConnectionData


internal class ChatClientImpl constructor(
    private val api: ChatApi,
    private val socket: ChatSocket
) : ChatClient {

    private val state = ClientState()

    override fun setUser(
        user: User,
        provider: TokenProvider,
        callback: (Result<ConnectionData>) -> Unit
    ) {

        state.user = user

        socket.connect(user, provider).enqueue { result ->

            if (result.isSuccess) {
                api.setConnection(result.data())
            }

            callback(result)
        }
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

    override fun getReplies(
        messageId: String,
        firstId: String,
        limit: Int
    ): ChatCall<List<Message>> {
        return api.getRepliesMore(messageId, firstId, limit)
    }

    override fun getReplies(messageId: String, limit: Int): ChatCall<List<Message>> {
        return api.getReplies(messageId, limit)
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

    override fun deleteChannel(channelType: String, channelId: String): ChatCall<Channel>{
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

    override fun markAllRead(): ChatCall<Event> {
        return api.markAllRead().map {
            it.event
        }
    }

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