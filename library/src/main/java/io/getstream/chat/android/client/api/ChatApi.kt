package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.*
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File

interface ChatApi {

    fun setConnection(userId: String, connectionId: String)

    //region CDN calls

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        mimeType: String,
        callback: ProgressCallback
    )

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        mimeType: String
    ): Call<String>

    fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit>

    fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit>

    //endregion

    //region Device calls

    fun addDevice(firebaseToken: String): Call<Unit>
    fun deleteDevice(firebaseToken: String): Call<Unit>
    fun getDevices(): Call<List<Device>>

    //endregion

    fun searchMessages(request: SearchMessagesRequest): Call<List<Message>>
    fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>>
    fun getReplies(messageId: String, limit: Int): Call<List<Message>>
    fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>>
    fun sendReaction(reaction: Reaction): Call<Reaction>
    fun sendReaction(messageId: String, reactionType:String): Call<Reaction>
    fun deleteReaction(messageId: String, reactionType: String): Call<Message>
    fun deleteMessage(messageId: String): Call<Message>
    fun sendAction(request: SendActionRequest): Call<Message>
    fun getMessage(messageId: String): Call<Message>
    fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): Call<Message>

    fun updateMessage(
        message: Message
    ): Call<Message>

    fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>>
    fun stopWatching(
        channelType: String,
        channelId: String
    ): Call<Unit>

    fun queryChannel(
        channelType: String,
        channelId: String = "",
        query: ChannelQueryRequest
    ): Call<Channel>

    fun updateChannel(
        channelType: String,
        channelId: String,
        request: UpdateChannelRequest
    ): Call<Channel>

    fun markRead(channelType: String, channelId: String, messageId: String): Call<Unit>
    fun showChannel(channelType: String, channelId: String): Call<Unit>
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): Call<Unit>

    fun rejectInvite(channelType: String, channelId: String): Call<Channel>
    fun acceptInvite(channelType: String, channelId: String, message: String): Call<Channel>
    fun deleteChannel(channelType: String, channelId: String): Call<Channel>
    fun markAllRead(): Call<EventResponse>

    fun setGuestUser(userId: String, userName: String): Call<TokenResponse>

    fun getUsers(queryUsers: QueryUsersRequest): Call<List<User>>

    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel>

    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel>

    fun muteUser(
        targetId: String
    ): Call<MuteUserResponse>

    fun unMuteUser(
        targetId: String
    ): Call<MuteUserResponse>

    fun flag(
        targetId: String
    ): Call<FlagResponse>

    fun banUser(
        targetId: String,
        timeout: Int,
        reason: String,
        channelType: String,
        channelId: String
    ): Call<CompletableResponse>

    fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): Call<CompletableResponse>

    fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any> = emptyMap()
    ): Call<ChatEvent>
}