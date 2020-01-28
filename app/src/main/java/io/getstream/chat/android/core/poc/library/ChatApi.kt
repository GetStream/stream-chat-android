package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.rest.*
import io.getstream.chat.android.core.poc.library.socket.ConnectionData

interface ChatApi {

    fun setConnection(connection:ConnectionData)

    fun addDevice(request: AddDeviceRequest): ChatCall<Unit>
    fun deleteDevice(deviceId: String): ChatCall<Unit>
    fun getDevices(): ChatCall<List<Device>>
    fun searchMessages(request: SearchMessagesRequest): ChatCall<List<Message>>
    fun getRepliesMore(messageId: String, firstId: String, limit: Int): ChatCall<List<Message>>
    fun getReplies(messageId: String, limit: Int): ChatCall<List<Message>>
    fun getReactions(messageId: String, offset: Int, limit: Int): ChatCall<List<Reaction>>
    fun deleteReaction(messageId: String, reactionType: String): ChatCall<Message>
    fun deleteMessage(messageId: String): ChatCall<Message>
    fun sendAction(request: SendActionRequest): ChatCall<Message>
    fun getMessage(messageId: String): ChatCall<Message>
    fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): ChatCall<Message>

    fun updateMessage(
        message: Message
    ): ChatCall<Message>

    fun queryChannels(query: QueryChannelsRequest): ChatCall<QueryChannelsResponse>
    fun stopWatching(
        channelType: String,
        channelId: String
    ): ChatCall<Unit>

    fun queryChannel(
        channelType: String,
        channelId: String = "",
        query: ChannelQueryRequest
    ): ChatCall<Channel>

    fun updateChannel(
        channelType: String,
        channelId: String,
        request: UpdateChannelRequest
    ): ChatCall<ChannelResponse>

    fun markRead(channelType: String, channelId: String, messageId: String): ChatCall<Unit>
    fun showChannel(channelType: String, channelId: String): ChatCall<Unit>
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): ChatCall<Unit>

    fun rejectInvite(channelType: String, channelId: String): ChatCall<Channel>
    fun acceptInvite(channelType: String, channelId: String, message: String): ChatCall<Channel>
    fun deleteChannel(channelType: String, channelId: String): ChatCall<Channel>
    fun markAllRead(): ChatCall<EventResponse>
}