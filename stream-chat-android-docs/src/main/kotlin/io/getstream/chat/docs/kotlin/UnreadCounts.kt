package io.getstream.chat.docs.kotlin

import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.docs.StaticInstances.TAG

class UnreadCounts(val client: ChatClient, val channelController: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/unread/?language=kotlin">Unread</a>
     */
    inner class Unread {
        fun getUnreadCount() {
            client.setUser(
                User("user-id"),
                "{{ chat_user_token }}",
                object : InitConnectionListener() {
                    override fun onSuccess(data: ConnectionData) {
                        val user = data.user
                        val unreadChannels = user.unreadChannels
                        val totalUnreadCount = user.totalUnreadCount
                    }
                }
            )
        }

        fun markRead() {
            channelController.markRead().enqueue { result ->
                if (result.isSuccess) {
                    // Messages in the channel marked as read
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().cause)
                }
            }
        }

        fun listeningReadEvents() {
            channelController.subscribeFor(
                NewMessageEvent::class,
                NotificationMessageNewEvent::class,
                MarkAllReadEvent::class,
                NotificationMarkReadEvent::class
            ) { event ->
                when (event) {
                    is NewMessageEvent -> {
                        val unreadChannels = event.unreadChannels
                        val totalUnreadCount = event.totalUnreadCount
                    }
                    is NotificationMessageNewEvent -> {
                        val unreadChannels = event.unreadChannels
                        val totalUnreadCount = event.totalUnreadCount
                    }
                    is MarkAllReadEvent -> {
                        val unreadChannels = event.unreadChannels
                        val totalUnreadCount = event.totalUnreadCount
                    }
                    is NotificationMarkReadEvent -> {
                        val unreadChannels = event.unreadChannels
                        val totalUnreadCount = event.totalUnreadCount
                    }
                }
            }
        }
    }
}
