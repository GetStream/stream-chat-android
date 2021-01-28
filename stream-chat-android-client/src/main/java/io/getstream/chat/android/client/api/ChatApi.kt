package io.getstream.chat.android.client.api

import androidx.annotation.CheckResult
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

    @CheckResult
    fun sendFile(channelType: String, channelId: String, file: File, callback: ProgressCallback? = null): Call<String>

    @CheckResult
    fun sendImage(channelType: String, channelId: String, file: File, callback: ProgressCallback? = null): Call<String>

    @CheckResult
    fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit>

    @CheckResult
    fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit>

    @CheckResult
    fun addDevice(firebaseToken: String): Call<Unit>

    @CheckResult
    fun deleteDevice(firebaseToken: String): Call<Unit>

    @CheckResult
    fun getDevices(): Call<List<Device>>

    @CheckResult
    fun searchMessages(request: SearchMessagesRequest): Call<List<Message>>

    @CheckResult
    fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>>

    @CheckResult
    fun getReplies(messageId: String, limit: Int): Call<List<Message>>

    @CheckResult
    fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>>

    @CheckResult
    fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction>

    @CheckResult
    fun sendReaction(messageId: String, reactionType: String, enforceUnique: Boolean): Call<Reaction>

    @CheckResult
    fun deleteReaction(messageId: String, reactionType: String): Call<Message>

    @CheckResult
    fun deleteMessage(messageId: String): Call<Message>

    @CheckResult
    fun sendAction(request: SendActionRequest): Call<Message>

    @CheckResult
    fun getMessage(messageId: String): Call<Message>

    @CheckResult
    fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Call<Message>

    @CheckResult
    fun muteChannel(channelType: String, channelId: String): Call<Unit>

    @CheckResult
    fun unMuteChannel(channelType: String, channelId: String): Call<Unit>

    @CheckResult
    fun updateMessage(
        message: Message,
    ): Call<Message>

    @CheckResult
    fun stopWatching(
        channelType: String,
        channelId: String,
    ): Call<Unit>

    @CheckResult
    fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>>

    @CheckResult
    fun updateUsers(users: List<User>): Call<List<User>>

    fun queryChannel(
        channelType: String,
        channelId: String = "",
        query: QueryChannelRequest,
    ): Call<Channel>

    @CheckResult
    fun updateChannel(
        channelType: String,
        channelId: String,
        request: UpdateChannelRequest,
    ): Call<Channel>

    @CheckResult
    fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel>

    @CheckResult
    fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel>

    @CheckResult
    fun markRead(
        channelType: String,
        channelId: String,
        messageId: String = "",
    ): Call<Unit>

    @CheckResult
    fun showChannel(channelType: String, channelId: String): Call<Unit>

    @CheckResult
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Call<Unit>

    @CheckResult
    fun rejectInvite(channelType: String, channelId: String): Call<Channel>

    @CheckResult
    fun muteCurrentUser(): Call<Mute>

    @CheckResult
    fun unmuteCurrentUser(): Call<Unit>

    @CheckResult
    fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel>

    @CheckResult
    fun deleteChannel(channelType: String, channelId: String): Call<Channel>

    @CheckResult
    fun markAllRead(): Call<Unit>

    @CheckResult
    fun getGuestUser(userId: String, userName: String): Call<GuestUser>

    @CheckResult
    fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>>

    @CheckResult
    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel>

    @CheckResult
    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel>

    @CheckResult
    fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>>

    @CheckResult
    fun muteUser(
        userId: String,
    ): Call<Mute>

    @CheckResult
    fun unmuteUser(
        userId: String,
    ): Call<Unit>

    @CheckResult
    fun flagUser(userId: String): Call<Flag>

    @CheckResult
    fun flagMessage(messageId: String): Call<Flag>

    @CheckResult
    fun banUser(
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<CompletableResponse>

    @CheckResult
    fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<CompletableResponse>

    @CheckResult
    fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
    ): Call<ChatEvent>

    @CheckResult
    fun translate(messageId: String, language: String): Call<Message>

    @CheckResult
    fun getSyncHistory(channelIds: List<String>, lastSyncAt: Date): Call<List<ChatEvent>>

    fun warmUp()
}
