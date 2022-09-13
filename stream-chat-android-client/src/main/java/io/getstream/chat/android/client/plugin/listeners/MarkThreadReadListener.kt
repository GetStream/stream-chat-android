package io.getstream.chat.android.client.plugin.listeners

/**
 * Listener used to notify when the newest message of the thread has been seen to mark it as read.
 */
public interface MarkThreadReadListener {

    public fun markThreadAsRead(channelType: String, channelId: String, parentMessageId: String)
}