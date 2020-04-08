package io.getstream.chat.android.client.controllers

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import java.io.File

interface ChannelController {

    val channelType: String
    val channelId: String
    val cid: String

    fun query(request: QueryChannelRequest): Call<Channel>
    fun watch(request: WatchChannelRequest): Call<Channel>
    fun watch(): Call<Channel>
    fun stopWatching(): Call<Unit>
    fun sendMessage(message: Message): Call<Message>
    fun updateMessage(message: Message): Call<Message>
    fun deleteMessage(messageId: String): Call<Message>
    fun getMessage(messageId: String): Call<Message>
    fun banUser(targetId: String, reason: String, timout: Int): Call<Unit>
    fun unBanUser(targetId: String, reason: String, timout: Int): Call<Unit>
    fun markMessageRead(messageId: String): Call<Unit>
    fun markRead(): Call<ChatEvent>
    fun delete(): Call<Channel>
    fun show(): Call<Unit>
    fun hide(clearHistory: Boolean = false): Call<Unit>
    fun sendFile(file: File): Call<String>
    fun sendImage(file: File): Call<String>
    fun sendFile(file: File, callback: ProgressCallback): Call<String>
    fun sendImage(file: File, callback: ProgressCallback): Call<String>
    fun sendReaction(reaction: Reaction): Call<Reaction>
    fun deleteReaction(messageId: String, reactionType: String): Call<Message>
    fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>>
    fun getReactions(messageId: String, firstReactionId: String, limit: Int): Call<List<Message>>
    fun events(): ChatObservable
    fun update(message: Message, extraData: Map<String, Any> = emptyMap()): Call<Channel>
    fun addMembers(vararg userIds: String): Call<Channel>
    fun removeMembers(vararg userIds: String): Call<Channel>
    fun acceptInvite(message: String): Call<Channel>
    fun rejectInvite(): Call<Channel>
    fun muteCurrentUser(): Call<Mute>
    fun muteUser(userId: String): Call<Mute>
    fun unmuteUser(userId: String): Call<Mute>
    fun unmuteCurrentUser(): Call<Mute>
    fun watch(data: Map<String, Any>): Call<Channel>
    fun stopTyping(): Call<ChatEvent>
    fun keystroke(): Call<ChatEvent>
}