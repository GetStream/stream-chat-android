package io.getstream.chat.docs.kotlin.client.cms

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.models.Filters
import io.getstream.result.Result

class UserPermissions(val client: ChatClient) {

    inner class MultiTenantAndTeams {

        /**
         * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=kotlin#channel-team">Channel Team</a>
         */
        fun createTeamChannel() {
            // Creates the red-general channel for the red team
            client.createChannel(
                channelType = "messaging",
                channelId = "red-general",
                memberIds = emptyList(),
                extraData = mapOf("team" to "red")
            ).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val channel = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
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
                when (result) {
                    is Result.Success -> {
                        val users = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }
    }
}
