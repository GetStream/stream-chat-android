package io.getstream.chat.docs.kotlin

import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.docs.StaticInstances.TAG

class MultiTenantsAndTeams(val client: ChatClient, val channelController: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=kotlin">Multi Tenants & Teams</a>
     */
    fun createTeamChannel() {
        // Create channel with id red-general for red team
        val extraData = mapOf("team" to "red")
        client.createChannel("messaging", "red-general", extraData).enqueue { result ->
            if (result.isSuccess) {
                val channel = result.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=kotlin#user-search">User Search</a>
     */
    fun searchUserWithSpecificTeam() {
        // Search for users with name Jordan that are part of the red team
        val filter = Filters.and(
            Filters.eq("name", "Jordan"),
            Filters.eq("teams", Filters.contains("red"))
        )

        val offset = 0
        val limit = 1
        client.queryUsers(QueryUsersRequest(filter, offset, limit)).enqueue { result ->
            if (result.isSuccess) {
                val users = result.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
            }
        }
    }
}