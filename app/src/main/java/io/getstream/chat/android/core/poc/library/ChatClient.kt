package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.requests.QueryUsers
import io.getstream.chat.android.core.poc.library.rest.*
import io.getstream.chat.android.core.poc.library.socket.ChatObservable

interface ChatClient {

    fun setUser(user: User, provider: TokenProvider)

    fun setGuestUser(user: User): ChatCall<TokenResponse>

    fun setAnonymousUser()

    fun disconnect()

    //region Events

    fun events(): ChatObservable

    //endregion

    //region Users

    fun getUsers(query: QueryUsers): ChatCall<List<User>>
    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): ChatCall<ChannelResponse>

    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): ChatCall<ChannelResponse>

    fun muteUser(targetId: String): ChatCall<MuteUserResponse>
    fun unMuteUser(targetId: String): ChatCall<MuteUserResponse>
    fun flag(targetId: String): ChatCall<FlagResponse>
    fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String? = null,
        timeout: Int? = null
    ): ChatCall<CompletableResponse>

    fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): ChatCall<CompletableResponse>

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
    fun getReplies(messageId: String, limit: Int): ChatCall<List<Message>>
    fun getRepliesMore(messageId: String, firstId: String, limit: Int): ChatCall<List<Message>>
    fun getReactions(messageId: String, offset: Int, limit: Int): ChatCall<List<Reaction>>
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
    fun markAllRead(): ChatCall<ChatEvent>
    fun deleteChannel(channelType: String, channelId: String): ChatCall<Channel>
    //endregion

}