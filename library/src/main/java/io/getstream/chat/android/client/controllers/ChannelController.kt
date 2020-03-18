package io.getstream.chat.android.client.controllers

import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File

interface ChannelController {
    fun watch(request: ChannelWatchRequest): Call<Channel>
    fun watch(): Call<Channel>
    fun stopWatching(): Call<Unit>
    fun sendMessage(message: Message): Call<Message>
    fun addMembers(members: List<String>): Call<Channel>
    fun removeMembers(members: List<String>): Call<Channel>
    fun banUser(targetId: String, reason: String, timout: Int): Call<Unit>
    fun unBanUser(targetId: String, reason: String, timout: Int): Call<Unit>
    fun sendFile(file: File, mimeType: String, callback: ProgressCallback)
    fun sendFile(file: File, mimeType: String): Call<String>
    fun markMessageRead(messageId: String): Call<Unit>
    fun markRead(): Call<ChatEvent>
    fun delete(): Call<Channel>
    fun show(): Call<Unit>
    fun hide(clearHistory: Boolean = false): Call<Unit>
}