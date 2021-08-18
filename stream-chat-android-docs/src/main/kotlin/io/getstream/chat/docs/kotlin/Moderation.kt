package io.getstream.chat.docs.kotlin

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.BannedUsersSort
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import java.util.Date

class Moderation(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="hhttps://getstream.io/chat/docs/android/moderation/?language=kotlin#flag">Flag</a>
     */
    inner class Flags {

        fun flagMessage() {
            client.flagMessage("message-id").enqueue { result ->
                if (result.isSuccess) {
                    // Message was flagged
                    val flag: Flag = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        fun unflagMessage() {
            client.unflagMessage("message-id").enqueue { result ->
                if (result.isSuccess) {
                    // Message flag was removed
                    val flag: Flag = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        fun flagUser() {
            client.flagUser("user-id").enqueue { result ->
                if (result.isSuccess) {
                    // User was flagged
                    val flag: Flag = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        fun unflagUser() {
            client.unflagUser("user-id").enqueue { result ->
                if (result.isSuccess) {
                    // User flag was removed
                    val flag: Flag = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=kotlin#mutes">Mutes</a>
     */
    inner class Mutes {

        fun muteUser() {
            client.muteUser("user-id").enqueue { result ->
                if (result.isSuccess) {
                    // User was muted
                    val mute: Mute = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        fun muteUserWithTimeout() {
            // Mute user for 60 minutes
            client.muteUser("user-id", timeout = 60).enqueue { result ->
                if (result.isSuccess) {
                    // User was muted
                } else {
                    // Handle result.error()
                }
            }
        }

        fun unmuteUser() {
            client.unmuteUser("user-id").enqueue { result ->
                if (result.isSuccess) {
                    // User was unmuted
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=kotlin#ban">Bans</a>
     */
    inner class Bans {

        fun banUser() {
            // Ban user for 60 minutes from a channel
            channelClient.banUser(targetId = "user-id", reason = "Bad words", timeout = 60).enqueue { result ->
                if (result.isSuccess) {
                    // User was banned
                } else {
                    // Handle result.error()
                }
            }
        }

        fun unbanUser() {
            channelClient.unbanUser(targetId = "user-id").enqueue { result ->
                if (result.isSuccess) {
                    // User was unbanned
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=kotlin#list-banned-users">List banned users</a>
     */
    inner class ListBannedUsers {

        fun queryBannedUsers() {
            // Retrieve the list of banned users
            client.queryUsers(
                QueryUsersRequest(
                    filter = Filters.eq("banned", true),
                    offset = 0,
                    limit = 10,
                )
            ).enqueue { result ->
                if (result.isSuccess) {
                    val users: List<User> = result.data()
                } else {
                    // Handle result.error()
                }
            }

            // Query for banned members from one channel
            client.queryBannedUsers(filter = Filters.eq("channel_cid", "ChannelType:ChannelId")).enqueue { result ->
                if (result.isSuccess) {
                    val bannedUsers: List<BannedUser> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        fun queryBannedMembers() {
            channelClient.queryMembers(
                offset = 0,
                limit = 10,
                filter = Filters.eq("banned", true),
                sort = QuerySort(),
                members = emptyList()
            ).enqueue { result ->
                if (result.isSuccess) {
                    val members: List<Member> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/react/moderation/?language=kotlin#query-bans-endpoint">Query bans endpoint</a>
     */
    inner class QueryBansEndpoint {

        fun queryBans() {
            // Get the bans for channel livestream:123 in descending order
            client.queryBannedUsers(
                filter = Filters.eq("channel_cid", "livestream:123"),
                sort = QuerySort.desc(BannedUsersSort::createdAt),
            ).enqueue { result ->
                if (result.isSuccess) {
                    val bannedUsers: List<BannedUser> = result.data()
                } else {
                    // Handle result.error()
                }
            }

            // Get the page of bans which where created before or equal date for the same channel
            client.queryBannedUsers(
                filter = Filters.eq("channel_cid", "livestream:123"),
                sort = QuerySort.desc(BannedUsersSort::createdAt),
                createdAtBeforeOrEqual = Date(),
            ).enqueue { result ->
                if (result.isSuccess) {
                    val bannedUsers: List<BannedUser> = result.data()
                } else {
                    // Handle result.error()
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
            channelClient.shadowBanUser(targetId = "user-id", reason = "Bad words", timeout = 60).enqueue { result ->
                if (result.isSuccess) {
                    // User was shadow banned
                } else {
                    // Handle result.error()
                }
            }
        }

        fun removeShadowBan() {
            channelClient.removeShadowBan("user-id").enqueue { result ->
                if (result.isSuccess) {
                    // Shadow ban was removed
                } else {
                    // Handle result.error()
                }
            }
        }
    }
}
