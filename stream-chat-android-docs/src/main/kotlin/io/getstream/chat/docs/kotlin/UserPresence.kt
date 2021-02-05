package io.getstream.chat.docs.kotlin

import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.docs.StaticInstances

class UserPresence(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/presence_format/?language=kotlin#invisible">Invisible</a>
     */
    fun invisible() {
        val user = User("user-id").apply {
            invisible = true
        }
        client.connectUser(user, "{{ chat_user_token }}").enqueue()
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/presence_format/?language=kotlin#listening-to-presence-changes">Listening to Presence Changes</a>
     */
    fun listeningPresenceChanges() {
        // You need to be watching some channels/queries to be able to get presence events.
        // There are multiple ways of doing so:
        // 1. Watch a channel for presence event
        val watchRequest = WatchChannelRequest().apply {
            presence = true
            data["members"] = listOf("john", "jack")
        }
        channelClient.watch(watchRequest).enqueue {
            if (it.isSuccess) {
                val channel = it.data()
            } else {
                Log.e(StaticInstances.TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }

        // 2. Query some channels with presence events
        val channelsOffset = 0
        val channelsLimit = 10
        val channelsFilter = Filters.`in`("members", "john", "jack").put("type", "messaging")
        val channelsRequest = QueryChannelsRequest(channelsFilter, channelsOffset, channelsLimit).apply {
            presence = true
        }
        client.queryChannels(channelsRequest).enqueue {
            if (it.isSuccess) {
                val channels = it.data()
            } else {
                Log.e(StaticInstances.TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }

        // 3. Query some users for presence event
        val usersOffset = 0
        val usersLimit = 2
        val usersFilter = Filters.`in`("id", listOf("john", "jack"))
        val usersQuery = QueryUsersRequest(usersFilter, usersOffset, usersLimit).apply {
            presence = true
        }
        client.queryUsers(usersQuery).enqueue {
            if (it.isSuccess) {
                val users = it.data()
            } else {
                Log.e(StaticInstances.TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }

        // Finally, Subscribe to events
        client.subscribeFor<UserPresenceChangedEvent> { event ->
            // Handle change
        }
    }
}
