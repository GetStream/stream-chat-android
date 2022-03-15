package io.getstream.chat.docs.kotlin

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.livedata.ChatDomain
import java.util.Date

class Android {

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/events/event_listening?language=kotlin">Listening for events</a>
     */
    class SyncHistory() : Fragment() {

        fun getSyncHistory(chatClient: ChatClient) {
            val cidList: List<String> = listOf("messaging:123")
            val lastSeenExample = Date()

            chatClient.getSyncHistory(cidList, lastSeenExample).enqueue { result ->
                if (result.isSuccess) {
                    val events: List<ChatEvent> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/unread_channel/?language=kotlin">Channels</a>
     */
    class UnreadCount : Fragment() {

        fun unreadCountInfo() {
            // Get channel
            val queryChannelRequest = QueryChannelRequest().withState()

            val channel = ChatClient.instance().queryChannel(
                channelType = "channel-type",
                channelId = "channel-id",
                request = queryChannelRequest
            )
                .execute()
                .data()

            // readState is the list of read states for each user on the channel
            val readState: List<ChannelUserRead> = channel.read
        }

        fun unreadCountInfoChatDomain() {
            // Get channel
            val channel = ChatDomain.instance()
                .watchChannel(cid = "messaging:123", messageLimit = 0)
                .execute()
                .data()
                .toChannel()

            // readState is the list of read states for each user on the channel
            val readState: List<ChannelUserRead> = channel.read
        }

        fun unreadCountForCurrentUser() {
            // Get channel
            val queryChannelRequest = QueryChannelRequest().withState()

            val channel = ChatClient.instance().queryChannel(
                channelType = "channel-type",
                channelId = "channel-id",
                request = queryChannelRequest
            )
                .execute()
                .data()

            // Unread count for current user
            val unreadCount: Int? = channel.unreadCount
        }

        fun unreadCountForCurrentUserChatDomain() {
            // Get channel controller
            val channelController = ChatDomain.instance()
                .watchChannel(cid = "messaging:123", messageLimit = 0)
                .execute()
                .data()

            // Unread count for current user
            val unreadCount: LiveData<Int?> = channelController.unreadCount
        }

        fun markAllRead() {
            ChatClient.instance().markAllRead().enqueue { result ->
                if (result.isSuccess) {
                    // Handle success
                } else {
                    // Handle failure
                }
            }
        }
    }
}
