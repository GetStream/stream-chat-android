package io.getstream.chat.android.client.plugin.listeners

// TODO
public interface MarkThreadReadListener {

    public fun markThreadAsRead(channelType: String, channelId: String, parentMessageId: String)
}