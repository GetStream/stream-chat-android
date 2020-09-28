package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.UpdateChannelRequest
import io.getstream.chat.android.client.call.Call
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
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File
import java.util.Date

internal interface ChatApi {

    fun setConnection(userId: String, connectionId: String)

    //region CDN calls

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String>

    fun sendImage(
        channelType: String,
        channelId: String,
        file: File
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
    fun sendReaction(messageId: String, reactionType: String): Call<Reaction>
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
        query: QueryChannelRequest
    ): Call<Channel>

    fun updateChannel(
        channelType: String,
        channelId: String,
        request: UpdateChannelRequest
    ): Call<Channel>

    fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int
    ): Call<Channel>

    fun disableSlowMode(
        channelType: String,
        channelId: String
    ): Call<Channel>

    fun markRead(channelType: String, channelId: String, messageId: String = ""): Call<Unit>
    fun showChannel(channelType: String, channelId: String): Call<Unit>
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): Call<Unit>

    fun rejectInvite(channelType: String, channelId: String): Call<Channel>
    fun acceptInvite(channelType: String, channelId: String, message: String): Call<Channel>
    fun deleteChannel(channelType: String, channelId: String): Call<Channel>
    fun markAllRead(): Call<Unit>

    fun updateUsers(users: List<User>): Call<List<User>>

    fun getGuestUser(userId: String, userName: String): Call<GuestUser>

    fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>>

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

    fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort,
        members: List<Member>
    ): Call<List<Member>>

    fun muteCurrentUser(): Call<Mute>

    fun muteUser(userId: String): Call<Mute>

    fun unmuteUser(userId: String): Call<Mute>

    fun unmuteCurrentUser(): Call<Mute>

    @Deprecated(
        message = "We are going to replace with flagUser()",
        replaceWith = ReplaceWith("this.flagUser(userId)")
    )
    fun flag(userId: String): Call<Flag>

    fun flagUser(userId: String): Call<Flag>

    fun flagMessage(messageId: String): Call<Flag>

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

    fun translate(messageId: String, language: String): Call<Message>

    fun getSyncHistory(channelIds: List<String>, lastSyncAt: Date): Call<List<ChatEvent>>

    fun muteChannel(channelType: String, channelId: String): Call<Unit>

    fun unMuteChannel(channelType: String, channelId: String): Call<Unit>

    fun warmUp()
}
