package io.getstream.chat.docs.kotlin.client.cms

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.android.client.utils.Result

class UserPresence(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/presence_format/?language=kotlin#invisible">Invisible</a>
     */
    fun invisible() {
        val user = User(
            id = "user-id",
            invisible = true,
        )
        client.connectUser(user, "{{ chat_user_token }}").enqueue { result ->
            when (result) {
                is Result.Success -> {
                    val connectionData = result.value
                }
                is Result.Failure -> {
                    // Handler error
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/presence_format/?language=kotlin#listening-to-presence-changes">Listening to Presence Changes</a>
     */
    fun listeningPresenceChanges() {
        // You need to be watching some channels/queries to be able to get presence events.
        // Here are three different ways of doing that:

        // 1. Watch a single channel with presence = true set
        val watchRequest = WatchChannelRequest().apply {
            data["members"] = listOf("john", "jack")
            presence = true
        }
        channelClient.watch(watchRequest).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    val channel = result.value
                }
                is Result.Failure -> {
                    // Handler error
                }
            }
        }

        // 2. Query some channels with presence = true set
        val channelsRequest = QueryChannelsRequest(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf("john", "jack")),
            ),
            offset = 0,
            limit = 10,
        ).apply {
            presence = true
        }
        client.queryChannels(channelsRequest).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    val channels = result.value
                }
                is Result.Failure -> {
                    // Handler error
                }
            }
        }

        // 3. Query some users with presence = true set
        val usersQuery = QueryUsersRequest(
            filter = Filters.`in`("id", listOf("john", "jack")),
            offset = 0,
            limit = 2,
            presence = true,
        )
        client.queryUsers(usersQuery).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    val users = result.value
                }
                is Result.Failure -> {
                    // Handler error
                }
            }
        }

        // Finally, subscribe to presence to events
        client.subscribeFor<UserPresenceChangedEvent> { event ->
            // Handle change
        }
    }
}
