package io.getstream.chat.docs.kotlin

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination.LESS_THAN
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.ChannelsMuteEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.docs.StaticInstances.TAG

class Channels(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/initialize_channel/?language=kotlin">Channel Initialization</a>
     */
    inner class ChannelInitialization {
        fun initialization() {
            val channelClient = client.channel(channelType = "messaging", channelId = "general")
            channelClient
                .create(
                    members = listOf("thierry", "tommaso"),
                    extraData = mapOf(
                        "name" to "Founder Chat",
                        "image" to "https://bit.ly/2O35mws",
                    )
                )
                .enqueue { result ->
                    if (result.isSuccess) {
                        val channel: Channel = result.data()
                    } else {
                        // Handle result.error()
                    }
                }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/creating_channels/?language=kotlin">Creating Channels</a>
     */
    inner class CreatingChannels {
        fun createAChannel() {
            val channelClient = client.channel(channelType = "messaging", channelId = "general")

            channelClient.create().enqueue { result ->
                if (result.isSuccess) {
                    val newChannel: Channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=kotlin">Watching A Channel</a>
     */
    inner class WatchingAChannel {
        fun watchingASingleChannel() {
            val channelClient = client.channel(channelType = "messaging", channelId = "general")

            channelClient.watch().enqueue { result ->
                if (result.isSuccess) {
                    val channel: Channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=kotlin#unwatching">Unwacthing</a>
         */
        fun unwatchAChannel() {
            channelClient.stopWatching().enqueue { result ->
                if (result.isSuccess) {
                    // Channel unwatched
                } else {
                    // Handle result.error()
                }
            }
        }

        fun watchingMultipleChannels(currentUserId: String) {
            val request = QueryChannelsRequest(
                filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf(currentUserId)),
                ),
                offset = 0,
                limit = 10,
                querySort = QuerySort.desc("last_message_at")
            ).apply {
                // Watches the channels automatically
                watch = true
                state = true
            }

            // Run query on ChatClient
            client.queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    val channels: List<Channel> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        fun watcherCount() {
            val request = QueryChannelRequest().withState()
            channelClient.query(request).enqueue { result ->
                if (result.isSuccess) {
                    val channel: Channel = result.data()
                    channel.watcherCount
                } else {
                    // Handle result.error()
                }
            }
        }

        fun paginatingChannelWatchers() {
            val request = QueryChannelRequest()
                .withWatchers(limit = 5, offset = 0)
            channelClient.query(request).enqueue { result ->
                if (result.isSuccess) {
                    val channel: Channel = result.data()
                    val watchers: List<User> = channel.watchers
                } else {
                    // Handle result.error()
                }
            }
        }

        fun listeningToChangesInWatchers() {
            // Start watching channel
            channelClient.watch().enqueue {
                /* Handle result */
            }

            // Subscribe for watching events
            channelClient.subscribeFor(
                UserStartWatchingEvent::class,
                UserStopWatchingEvent::class,
            ) { event ->
                when (event) {
                    is UserStartWatchingEvent -> {
                        // User who started watching the channel
                        val user = event.user
                    }
                    is UserStopWatchingEvent -> {
                        // User who stopped watching the channel
                        val user = event.user
                    }
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Querying Channels</a>
     */
    inner class QueryingChannels {
        fun queryChannels() {
            val request = QueryChannelsRequest(
                filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf("thierry")),
                ),
                offset = 0,
                limit = 10,
                querySort = QuerySort.desc("last_message_at")
            ).apply {
                watch = true
                state = true
            }

            client.queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    val channels: List<Channel> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin#common-filters-by-use-case">Common Filters</a>
         */
        inner class CommonFilters {
            fun channelsThatContainsSpecificUser() {
                val filter = Filters.`in`("members", listOf("thierry"))
            }

            fun channelsThatWithSpecificStatus(user: User) {
                val filter = Filters.and(
                    Filters.eq("agent_id", user.id),
                    Filters.`in`("status", listOf("pending", "open", "new")),
                )
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin#response">Response</a>
         */
        fun paginatingChannels() {
            // Get the first 10 channels
            val filter = Filters.`in`("members", "thierry")
            val offset = 0
            val limit = 10
            val request = QueryChannelsRequest(filter, offset, limit)
            client.queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    val channels = result.data()
                } else {
                    // Handle result.error()
                }
            }

            // Get the second 10 channels
            val nextRequest = QueryChannelsRequest(
                filter = filter,
                offset = 10, // Skips first 10
                limit = limit
            )
            client.queryChannels(nextRequest).enqueue { result ->
                if (result.isSuccess) {
                    val channels = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_pagination/?language=kotlin">Channel Pagination</a>
     */
    inner class ChannelPagination {

        fun channelPagination() {
            val channelClient = client.channel("messaging", "general")
            val pageSize = 10

            // Request for the first page
            val request = QueryChannelRequest()
                .withMessages(pageSize)

            channelClient.query(request).enqueue { result ->
                if (result.isSuccess) {
                    val messages: List<Message> = result.data().messages
                    if (messages.size < pageSize) {
                        // All messages loaded
                    } else {
                        // Load next page
                        val nextRequest = QueryChannelRequest()
                            .withMessages(LESS_THAN, messages.last().id, pageSize)
                        // ...
                    }
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_update/?language=kotlin">Updating a Channel</a>
     */
    inner class UpdatingAChannel {
        fun fullUpdate() {
            val channelClient = client.channel("messaging", "general")

            channelClient.update(
                message = Message(
                    text = "Thierry changed the channel color to green"
                ),
                extraData = mapOf(
                    "color" to "green",
                    "color" to "green",
                ),
            ).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_members/?language=kotlin">Updating a Channel</a>
     */
    inner class ChangingChannelMembers {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_members/?language=kotlin#adding-removing-channel-members">Adding & Removing Channel Members</a>
         */
        fun addingAndRemovingChannelMembers() {
            val channelClient = client.channel("messaging", "general")

            // Add members with ids "thierry" and "josh"
            channelClient.addMembers("thierry", "josh").enqueue { result ->
                if (result.isSuccess) {
                    val channel: Channel = result.data()
                } else {
                    // Handle result.error()
                }
            }

            // Remove member with id "tommaso"
            channelClient.removeMembers("tommaso").enqueue { result ->
                if (result.isSuccess) {
                    val channel: Channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_conversations/?language=kotlin">One to One Conversations</a>
     */
    inner class OneToOneConversations {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_conversations/?language=kotlin#creating-conversations">Creating Conversations</a>
         */
        fun creatingConversation() {
            client.createChannel(
                channelType = "messaging",
                members = listOf("thierry", "tomasso")
            ).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_conversations/?language=kotlin">Invites</a>
     */
    inner class Invites {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#inviting-users">Inviting Users</a>
         */
        fun invitingUsers() {
            val channelClient = client.channel("messaging", "general")
            val data = mapOf(
                "members" to listOf("thierry", "tommaso"),
                "invites" to listOf("nick")
            )

            channelClient.create(data).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#accepting-an-invite">Accept an Invite</a>
         */
        fun acceptingAnInvite() {
            channelClient.acceptInvite(
                message = "Nick joined this channel!"
            ).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#rejecting-an-invite">Rejecting an Invite</a>
         */
        fun rejectingAnInvite() {
            channelClient.rejectInvite().enqueue { result ->
                if (result.isSuccess) {
                    // Invite rejected
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#query-for-accepted-invites">Query For Accepted Invites</a>
         */
        fun queryForAcceptedInvites() {
            val request = QueryChannelsRequest(
                filter = Filters.eq("invite", "accepted"),
                offset = 0,
                limit = 10
            )
            client.queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    val channels: List<Channel> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#query-for-rejected-invites">Query For Rejected Invites</a>
         */
        fun queryForRejectedInvites() {
            val request = QueryChannelsRequest(
                filter = Filters.eq("invite", "rejected"),
                offset = 0,
                limit = 10
            )
            client.queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    val channels: List<Channel> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Deleting & Hiding a Channel</a>
     */
    inner class DeletingAndHidingAChannel {

        fun deletingAChannel() {
            val channelClient = client.channel("messaging", "general")

            channelClient.delete().enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin#hiding-a-channel">Hiding a Channel</a>
         */
        fun hidingAChannel() {
            // Hides the channel until a new message is added there
            channelClient.hide().enqueue { result ->
                if (result.isSuccess) {
                    // Channel is hidden
                } else {
                    // Handle result.error()
                }
            }

            // Shows a previously hidden channel
            channelClient.show().enqueue { result ->
                if (result.isSuccess) {
                    // Channel is shown
                } else {
                    // Handle result.error()
                }
            }

            // Hide the channel and clear the message history
            channelClient.hide(clearHistory = true).enqueue { result ->
                if (result.isSuccess) {
                    // Channel is hidden
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=kotlin">Muting Channels</a>
     */
    inner class MutingChannels {

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=kotlin#channel-mute">Channel Mute</a>
         */
        // TODO code in this method doesn't match the CMS, review it
        fun channelMute() {
            client.muteChannel("channel-type", "channel-id").enqueue { result ->
                if (result.isSuccess) {
                    // Channel is muted
                } else {
                    // Handle result.error()
                }
            }

            // Get list of muted channels when user is connected
            client.connectUser(User("user-id"), "token")
                .enqueue { result ->
                    if (result.isSuccess) {
                        val user = result.data().user
                        // Mutes contains the list of channel mutes
                        val mutes: List<ChannelMute> = user.channelMutes
                    }
                }

            // Get updates about muted channels
            client.subscribeFor<ChannelsMuteEvent> { event ->
                val mutes: List<ChannelMute> = event.channelsMute
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=kotlin#query-muted-channels">Query Muted Channels</a>
         */
        fun queryMutedChannels(currentUserId: String, filter: FilterObject) {
            // Filter for all channels excluding muted ones
            val notMutedFilter = Filters.and(
                Filters.eq("muted", false),
                Filters.`in`("members", listOf(currentUserId)),
            )

            // Filter for muted channels
            val mutedFilter = Filters.eq("muted", true)

            // Executing a channels query with either of the filters
            client.queryChannels(
                QueryChannelsRequest(
                    filter = filter, // Set the correct filter here
                    offset = 0,
                    limit = 10,
                )
            ).enqueue { result ->
                if (result.isSuccess) {
                    val channels: List<Channel> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=kotlin#remove-a-channel-mute">Remove a Channel Mute</a>
         */
        fun removeAChannelMute() {
            // Unmute channel for current user
            channelClient.unmute().enqueue { result ->
                if (result.isSuccess) {
                    // Channel is unmuted
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_members/?language=kotlin">Query Members</a>
     */
    inner class QueryMembers {
        fun queryingMembers() {
            val channelClient = client.channel("messaging", "general")

            val offset = 0 // Use this value for pagination
            val limit = 10
            val sort = QuerySort<Member>()

            // Channel members can be queried with various filters
            // 1. Create the filter, e.g query members by user name
            val filterByName = Filters.eq("name", "tommaso")
            // 2. Call queryMembers with that filter
            channelClient.queryMembers(offset, limit, filterByName, sort).enqueue { result ->
                if (result.isSuccess) {
                    val members: List<Member> = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().cause)
                }
            }

            // Here are some other commons filters you can use:

            // Autocomplete members by user name (names containing "tom")
            val filterByAutoCompleteName = Filters.autocomplete("name", "tom")

            // Query member by id
            val filterById = Filters.eq("id", "tommaso")

            // Query multiple members by id
            val filterByIds = Filters.`in`("id", listOf("tommaso", "thierry"))

            // Query channel moderators
            val filterByModerator = Filters.eq("is_moderator", true)

            // Query for banned members in channel
            val filterByBannedMembers = Filters.eq("banned", true)

            // Query members with pending invites
            val filterByPendingInvite = Filters.eq("invite", "pending")

            // Query all the members
            val filterByNone = FilterObject()

            // Results can also be orderd with the QuerySort param
            // For example, this will order results by member creation time, descending
            val createdAtDescendingSort = QuerySort<Member>().desc("created_at")
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/slow_mode/?language=kotlin">Throttling & Slow mode</a>
     */
    inner class ThrottlingAndSlowMode {
        private fun disableMessageSendingUi() {}
        private fun enableMessageSendingUi() {}

        fun enableAndDisable() {
            val channelClient = client.channel("messaging", "general")

            // Enable slow mode and set cooldown to 1s
            channelClient.enableSlowMode(cooldownTimeInSeconds = 1).enqueue { /* Result handling */ }

            // Increase cooldown to 30s
            channelClient.enableSlowMode(cooldownTimeInSeconds = 30).enqueue { /* Result handling */ }

            // Disable slow mode
            channelClient.disableSlowMode().enqueue { /* Result handling */ }
        }

        fun blockUi() {
            val channelClient = client.channel("messaging", "general")

            // Get the cooldown value
            channelClient.query(QueryChannelRequest()).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                    val cooldown = channel.cooldown

                    val message = Message(text = "Hello")
                    channelClient.sendMessage(message).enqueue {
                        // After sending a message, block the UI temporarily
                        // The disable/enable UI methods have to be implemented by you
                        disableMessageSendingUi()

                        Handler(Looper.getMainLooper())
                            .postDelayed(::enableMessageSendingUi, cooldown.toLong())
                    }
                }
            }
        }
    }
}
