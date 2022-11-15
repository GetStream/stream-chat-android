package io.getstream.chat.docs.kotlin.client.cms

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.client.utils.Result
import java.util.Date

class Moderation(val client: ChatClient, val channelClient: ChannelClient) {

    inner class ModerationTools {

        /**
         * @see <a href="hhttps://getstream.io/chat/docs/android/moderation/?language=kotlin#flag">Flag</a>
         */
        inner class Flags {

            fun flag() {
                client.flagMessage("message-id").enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // Message was flagged
                            val flag = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }

                client.flagUser("user-id").enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // User was flagged
                            val flag = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=kotlin#mutes">Mutes</a>
         */
        inner class Mutes {

            fun mutes() {
                client.muteUser("user-id").enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // User was muted
                            val mute = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }

                client.unmuteUser("user-id").enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // User was unmuted
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=kotlin#ban">Bans</a>
         */
        inner class Bans {

            fun ban() {
                // Ban user for 60 minutes from a channel
                channelClient.banUser(targetId = "user-id", reason = "Bad words", timeout = 60).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // User was banned
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }

                channelClient.unbanUser(targetId = "user-id").enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // User was unbanned
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=kotlin#query-bans-endpoint">Query bans endpoint</a>
         */
        inner class QueryBannedUsers {

            fun queryBans() {
                // Retrieve the list of banned users
                client.queryUsers(
                    QueryUsersRequest(
                        filter = Filters.eq("banned", true),
                        offset = 0,
                        limit = 10,
                    )
                ).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val users = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }

                // Query for banned members from one channel
                client.queryBannedUsers(filter = Filters.eq("channel_cid", "ChannelType:ChannelId")).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val bannedUsers = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }

            fun queryBansEndpoint() {
                // Get the bans for channel livestream:123 in descending order
                client.queryBannedUsers(
                    filter = Filters.eq("channel_cid", "livestream:123"),
                    sort = QuerySortByField.descByName("createdAt"),
                ).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val bannedUsers = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }

                // Get the page of bans which where created before or equal date for the same channel
                client.queryBannedUsers(
                    filter = Filters.eq("channel_cid", "livestream:123"),
                    sort = QuerySortByField.descByName("createdAt"),
                    createdAtBeforeOrEqual = Date(),
                ).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val bannedUsers = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=kotlin#shadow-ban">Shadow ban</a>
         */
        inner class ShadowBan {

            fun shadowBanUser() {
                // Shadow ban user for 60 minutes from a channel
                channelClient.shadowBanUser(targetId = "user-id", reason = "Bad words", timeout = 60)
                    .enqueue { result ->
                        when (result) {
                            is Result.Success -> {
                                // User was shadow banned
                            }
                            is Result.Failure -> {
                                // Handler error
                            }
                        }
                    }

                channelClient.removeShadowBan("user-id").enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // Shadow ban was removed
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }
    }
}
