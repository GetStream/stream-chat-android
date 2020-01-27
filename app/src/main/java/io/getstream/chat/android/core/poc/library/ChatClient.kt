package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.rest.AddDeviceRequest
import io.getstream.chat.android.core.poc.library.rest.ChannelQueryRequest
import io.getstream.chat.android.core.poc.library.rest.SearchMessagesRequest
import io.getstream.chat.android.core.poc.library.rest.SendActionRequest
import io.getstream.chat.android.core.poc.library.socket.ChatObservable
import io.getstream.chat.android.core.poc.library.socket.ConnectionData

interface ChatClient {

    fun setUser(
        user: User,
        provider: TokenProvider,
        callback: (Result<ConnectionData>) -> Unit
    )

    fun disconnect()

    //region Events

    fun events(): ChatObservable

    //endregion

    //region Api calls

    fun getState(): ClientState
    fun fromCurrentUser(entity: UserEntity): Boolean
    fun getUserId(): String
    fun getClientId(): String
    fun getDevices(): ChatCall<List<Device>>
    fun deleteDevice(deviceId: String): ChatCall<Unit>
    fun addDevice(request: AddDeviceRequest): ChatCall<Unit>
    fun searchMessages(request: SearchMessagesRequest): ChatCall<List<Message>>
    fun getReplies(messageId: String, firstId: String, limit: Int): ChatCall<List<Message>>
    fun getReplies(messageId: String, limit: Int): ChatCall<List<Message>>
    fun deleteReaction(messageId: String, reactionType: String): ChatCall<Message>
    fun sendAction(request: SendActionRequest): ChatCall<Message>
    fun deleteMessage(messageId: String): ChatCall<Message>
    fun getMessage(messageId: String): ChatCall<Message>
    fun sendMessage(channelType: String, channelId: String, message: Message): ChatCall<Message>
    fun updateMessage(message: Message): ChatCall<Message>

    fun queryChannel(
        channelType: String,
        channelId: String,
        request: ChannelQueryRequest
    ): ChatCall<Channel>

    fun markRead(channelType: String, channelId: String, messageId: String): ChatCall<Unit>
    fun showChannel(channelType: String, channelId: String): ChatCall<Unit>
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): ChatCall<Unit>

    fun stopWatching(channelType: String, channelId: String): ChatCall<Unit>
    fun queryChannels(request: QueryChannelsRequest): ChatCall<List<Channel>>

    fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message,
        channelExtraData: Map<String, Any> = emptyMap()
    ): ChatCall<Channel>

    fun rejectInvite(channelType: String, channelId: String): ChatCall<Channel>
    fun acceptInvite(channelType: String, channelId: String, message: String): ChatCall<Channel>
    fun markAllRead(): ChatCall<Event>
    fun deleteChannel(channelType: String, channelId: String): ChatCall<Channel>
    //endregion

}