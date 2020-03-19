package io.getstream.chat.android.client.controllers

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.ChannelQueryRequest
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File

internal class ChannelControllerImpl(
    val channelType: String,
    val channelId: String,
    val client: ChatClient
) : ChannelController {

    override fun query(request: ChannelQueryRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    override fun watch(request: ChannelWatchRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    override fun watch(): Call<Channel> {
        return client.queryChannel(channelType, channelId, ChannelWatchRequest())
    }

    override fun stopWatching(): Call<Unit> {
        return client.stopWatching(channelType, channelId)
    }

    override fun sendMessage(message: Message): Call<Message> {
        return client.sendMessage(channelType, channelId, message)
    }

    override fun addMembers(members: List<String>): Call<Channel> {
        return client.addMembers(channelType, channelId, members)
    }

    override fun removeMembers(members: List<String>): Call<Channel> {
        return client.removeMembers(channelType, channelId, members)
    }

    override fun banUser(targetId: String, reason: String, timout: Int): Call<Unit> {
        return client.banUser(targetId, channelType, channelId, reason, timout)
    }

    override fun unBanUser(targetId: String, reason: String, timout: Int): Call<Unit> {
        return client.unBanUser(targetId, channelType, channelId)
    }

    override fun sendFile(file: File, mimeType: String, callback: ProgressCallback) {
        client.sendFile(channelType, channelId, file, mimeType, callback)
    }

    override fun sendFile(file: File, mimeType: String): Call<String> {
        return client.sendFile(channelType, channelId, file, mimeType)
    }

    override fun markMessageRead(messageId: String): Call<Unit> {
        return client.markMessageRead(channelType, channelId, messageId)
    }

    override fun markRead(): Call<ChatEvent> {
        return client.markAllRead()
    }

    override fun delete(): Call<Channel> {
        return client.deleteChannel(channelType, channelId)
    }

    override fun show(): Call<Unit> {
        return client.showChannel(channelType, channelId)
    }

    override fun hide(clearHistory: Boolean): Call<Unit> {
        return client.hideChannel(channelType, channelId, clearHistory)
    }
}