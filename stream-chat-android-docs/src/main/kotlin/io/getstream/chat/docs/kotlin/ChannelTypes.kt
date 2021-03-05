package io.getstream.chat.docs.kotlin

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User

class ChannelTypes(val client: ChatClient) {

    inner class MultiTenantAndTeams {

        /**
         * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=kotlin#channel-team">Multi Tenants & Teams</a>
         */
        fun createTeamChannel() {
            // Creates the red-general channel for the red team
            client.createChannel(
                channelType = "messaging",
                channelId = "red-general",
                extraData = mapOf("team" to "red")
            ).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=kotlin#user-search">User Search</a>
         */
        fun searchUserWithSpecificTeam() {
            // Search for users with the name Jordan that are part of the red team
            val filter = Filters.and(
                Filters.eq("name", "Jordan"),
                Filters.contains("teams", "red")
            )

            client.queryUsers(QueryUsersRequest(filter, offset = 0, limit = 1)).enqueue { result ->
                if (result.isSuccess) {
                    val users: List<User> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }
}
