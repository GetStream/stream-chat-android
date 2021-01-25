package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.ChatApi
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

internal class MoshiChatApi : ChatApi {
    override fun setConnection(userId: String, connectionId: String) {
        TODO("Not yet implemented")
    }

    override fun sendFile(channelType: String, channelId: String, file: File, callback: ProgressCallback) {
        TODO("Not yet implemented")
    }

    override fun sendImage(channelType: String, channelId: String, file: File, callback: ProgressCallback) {
        TODO("Not yet implemented")
    }

    override fun sendFile(channelType: String, channelId: String, file: File): Call<String> {
        TODO("Not yet implemented")
    }

    override fun sendImage(channelType: String, channelId: String, file: File): Call<String> {
        TODO("Not yet implemented")
    }

    override fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun addDevice(firebaseToken: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteDevice(firebaseToken: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun getDevices(): Call<List<Device>> {
        TODO("Not yet implemented")
    }

    override fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> {
        TODO("Not yet implemented")
    }

    override fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction> {
        TODO("Not yet implemented")
    }

    override fun sendReaction(messageId: String, reactionType: String, enforceUnique: Boolean): Call<Reaction> {
        TODO("Not yet implemented")
    }

    override fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        TODO("Not yet implemented")
    }

    override fun deleteMessage(messageId: String): Call<Message> {
        TODO("Not yet implemented")
    }

    override fun sendAction(request: SendActionRequest): Call<Message> {
        TODO("Not yet implemented")
    }

    override fun getMessage(messageId: String): Call<Message> {
        TODO("Not yet implemented")
    }

    override fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message> {
        TODO("Not yet implemented")
    }

    override fun muteChannel(channelType: String, channelId: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun unMuteChannel(channelType: String, channelId: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun updateMessage(message: Message): Call<Message> {
        TODO("Not yet implemented")
    }

    override fun stopWatching(channelType: String, channelId: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {
        TODO("Not yet implemented")
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        TODO("Not yet implemented")
    }

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun updateChannel(channelType: String, channelId: String, request: UpdateChannelRequest): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun enableSlowMode(channelType: String, channelId: String, cooldownTimeInSeconds: Int): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun disableSlowMode(channelType: String, channelId: String): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun markRead(channelType: String, channelId: String, messageId: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun showChannel(channelType: String, channelId: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun hideChannel(channelType: String, channelId: String, clearHistory: Boolean): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun muteCurrentUser(): Call<Mute> {
        TODO("Not yet implemented")
    }

    override fun unmuteCurrentUser(): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun acceptInvite(channelType: String, channelId: String, message: String?): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun markAllRead(): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun getGuestUser(userId: String, userName: String): Call<GuestUser> {
        TODO("Not yet implemented")
    }

    override fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>> {
        TODO("Not yet implemented")
    }

    override fun addMembers(channelType: String, channelId: String, members: List<String>): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun removeMembers(channelType: String, channelId: String, members: List<String>): Call<Channel> {
        TODO("Not yet implemented")
    }

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        TODO("Not yet implemented")
    }

    override fun muteUser(userId: String): Call<Mute> {
        TODO("Not yet implemented")
    }

    override fun unmuteUser(userId: String): Call<Unit> {
        TODO("Not yet implemented")
    }

    override fun flagUser(userId: String): Call<Flag> {
        TODO("Not yet implemented")
    }

    override fun flagMessage(messageId: String): Call<Flag> {
        TODO("Not yet implemented")
    }

    override fun banUser(
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<CompletableResponse> {
        TODO("Not yet implemented")
    }

    override fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<CompletableResponse> {
        TODO("Not yet implemented")
    }

    override fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
    ): Call<ChatEvent> {
        TODO("Not yet implemented")
    }

    override fun translate(messageId: String, language: String): Call<Message> {
        TODO("Not yet implemented")
    }

    override fun getSyncHistory(channelIds: List<String>, lastSyncAt: Date): Call<List<ChatEvent>> {
        TODO("Not yet implemented")
    }

    override fun warmUp() {
        TODO("Not yet implemented")
    }
}
