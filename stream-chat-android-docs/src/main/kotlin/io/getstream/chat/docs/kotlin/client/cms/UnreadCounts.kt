package io.getstream.chat.docs.kotlin.client.cms

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.extensions.countUnreadMentionsForUser
import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.User
import io.getstream.result.Result

class UnreadCounts(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/unread/?language=kotlin">Unread</a>
     */
    inner class Unread {
        fun userConnect() {
            client.connectUser(User("user-id"), "{{ chat_user_token }}").enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val user = result.value.user
                        val unreadChannels = user.unreadChannels
                        val totalUnreadCount = user.totalUnreadCount
                        val unreadThreads = user.unreadThreads
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        fun markRead() {
            channelClient.markRead().enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        // Messages in the channel marked as read
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        fun listenToReadEvents() {
            channelClient.subscribeFor(
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
                    else -> Unit
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/unread_channel/?language=kotlin">Unread Channels</a>
     */
    inner class UnreadChannels {

        fun unreadChannels() {
            // Get channel
            val queryChannelRequest = QueryChannelRequest().withState()

            client.queryChannel(
                channelType = "channel-type",
                channelId = "channel-id",
                request = queryChannelRequest,
            ).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        // readState is the list of read states for each user on the channel
                        val readState: List<ChannelUserRead> = result.value.read
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        @OptIn(InternalStreamChatApi::class)
        fun unreadMessagesPerChannel() {
            // Get channel
            val queryChannelRequest = QueryChannelRequest().withState()

            client.queryChannel(
                channelType = "channel-type",
                channelId = "channel-id",
                request = queryChannelRequest,
            ).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        // Unread count for current user
                        val unreadCount = result.value.currentUserUnreadCount()
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        fun unreadMentions() {
            // Get channel
            val queryChannelRequest = QueryChannelRequest().withState()
            val currentUser = client.clientState.user.value
            if (currentUser == null) {
                // Handle user not connected state
                return
            }

            client.queryChannel(
                channelType = "channel-type",
                channelId = "channel-id",
                request = queryChannelRequest,
            ).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        // Unread mentions
                        val channel = result.value
                        val unreadCount = channel.countUnreadMentionsForUser(currentUser)
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        fun markAllAsRead() {
            client.markAllRead().enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        // Handle success
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }
    }
}
