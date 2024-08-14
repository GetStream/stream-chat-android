package io.getstream.chat.docs.kotlin.client.cms

import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.Pagination.LESS_THAN
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.extensions.isMutedFor
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.client.subscribeFor
import io.getstream.result.Result

class Channels(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/creating_channels/?language=kotlin">Creating Channels</a>
     */
    inner class CreatingChannels {

        /**
         * @see <a href="https://getstream.io/chat/docs/react/creating_channels/?language=kotlin#1.-creating-a-channel-using-a-channel-id">Creating a Channel Using a Channel Id</a>
         */
        fun createAChannel() {
            val channelClient = client.channel(channelType = "messaging", channelId = "general")

            channelClient.create(memberIds = emptyList(), extraData = emptyMap()).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val newChannel: Channel = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/react/creating_channels/?language=kotlin#2.-creating-a-channel-for-a-list-of-members">Creating a Channel for a List of Members</a>
         */
        fun createChannelWithListOfMembers() {
            client.createChannel(
                channelType = "messaging",
                channelId = "",
                memberIds = listOf("thierry", "tomasso"),
                extraData = emptyMap()
            ).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val channel: Channel = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
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
                when (result) {
                    is Result.Success -> {
                        val channel: Channel = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
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
                querySort = QuerySortByField.descByName("lastMessageAt")
            ).apply {
                // Watches the channels automatically
                watch = true
                state = true
            }

            // Run query on ChatClient
            client.queryChannels(request).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val channels: List<Channel> = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=kotlin#unwatching">Unwacthing</a>
         */
        fun unwatchAChannel() {
            channelClient.stopWatching().enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        // Channel unwatched
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        fun watcherCount() {
            val request = QueryChannelRequest().withState()
            channelClient.query(request).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val channel: Channel = result.value
                        channel.watcherCount
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }

        fun paginatingChannelWatchers() {
            val request = QueryChannelRequest()
                .withWatchers(limit = 5, offset = 0)
            channelClient.query(request).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val channel: Channel = result.value
                        val watchers: List<User> = channel.watchers
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
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
                    else -> Unit
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_update/?language=kotlin">Updating a Channel</a>
     */
    inner class UpdatingAChannel {

        /**
         * @see <a href="https://getstream.io/chat/docs/android/channel_update/?language=kotlin#partial-update">Partial Update</a>
         */
        fun partialUpdate() {
            // Here's a channel with some custom field data that might be useful
            val channelClient = client.channel(channelType = "messaging", channelId = "general")

            channelClient.create(
                memberIds = listOf("thierry", "tomasso"),
                extraData = mapOf(
                    "source" to "user",
                    "source_detail" to mapOf("user_id" to 123),
                    "channel_detail" to mapOf(
                        "topic" to "Plants and Animals",
                        "rating" to "pg"
                    )
                )
            ).execute()

            // let's change the source of this channel
            channelClient.updatePartial(set = mapOf("source" to "system")).execute()

            // since it's system generated we no longer need source_detail
            channelClient.updatePartial(unset = listOf("source_detail")).execute()

            // and finally update one of the nested fields in the channel_detail
            channelClient.updatePartial(set = mapOf("channel_detail.topic" to "Nature")).execute()

            // and maybe we decide we no longer need a rating
            channelClient.updatePartial(unset = listOf("channel_detail.rating")).execute()
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/channel_update/?language=kotlin#full-update-(overwrite)">Full Update (overwrite)</a>
         */
        fun fullUpdate() {
            val channelClient = client.channel("messaging", "general")

            channelClient.update(
                message = Message(
                    text = "Thierry changed the channel color to green"
                ),
                extraData = mapOf(
                    "name" to "myspecialchannel",
                    "color" to "green",
                ),
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
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_members/?language=kotlin">Updating Channel Members</a>
     */
    inner class UpdatingChannelMembers {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_members/?language=kotlin#adding-removing-channel-members">Adding & Removing Channel Members</a>
         */
        fun addingAndRemovingChannelMembers() {
            val channelClient = client.channel("messaging", "general")

            // Add members with ids "thierry" and "josh"
            channelClient.addMembers(listOf("thierry", "josh")).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val channel = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }

            // Remove member with id "tommaso"
            channelClient.removeMembers(listOf("tommaso")).enqueue { result ->
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
         * @see <a href="https://getstream.io/chat/docs/android/channel_members/?language=kotlin#message-parameter">Message Parameter</a>
         */
        fun messageParameter() {
            val channelClient = client.channel("messaging", "general")

            // Add members with ids "thierry" and "josh"
            channelClient.addMembers(
                listOf("thierry", "josh"),
                Message(text = "Thierry and Josh joined this channel.")
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

            // Remove member with id "tommaso"
            channelClient.removeMembers(
                listOf("tommaso"),
                Message(text = "Tommaso was removed from this channel.")
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
                    querySort = QuerySortByField.descByName("lastMessageAt")
                ).apply {
                    watch = true
                    state = true
                }

                client.queryChannels(request).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val channels = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
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
                    when (result) {
                        is Result.Success -> {
                            val channels = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }

                // Get the second 10 channels
                val nextRequest = QueryChannelsRequest(
                    filter = filter,
                    offset = 10, // Skips first 10
                    limit = limit
                )
                client.queryChannels(nextRequest).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val channels = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_members/?language=kotlin">Querying Members</a>
         */
        inner class QueryingMembers {

            fun paginationAndOrdering() {
                val channelClient = client.channel("messaging", "general")
                val filter = Filters.neutral()
                val offset = 0
                val limit = 10

                // paginate by user_id in descending order
                val sort = QuerySortByField<Member>().descByName("userId")

                channelClient.queryMembers(offset, limit, filter, sort).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val members = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }

                // paginate by created at in ascending order
                val createdAtSort = QuerySortByField<Member>().ascByName("createdAt")

                channelClient.queryMembers(offset, limit, filter, createdAtSort).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val members = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }

            fun queryingMembers() {
                val channelClient = client.channel("messaging", "general")

                val offset = 0 // Use this value for pagination
                val limit = 10
                val sort = QuerySortByField<Member>()

                // Channel members can be queried with various filters
                // 1. Create the filter, e.g query members by user name
                val filterByName = Filters.eq("name", "tommaso")
                // 2. Call queryMembers with that filter
                channelClient.queryMembers(offset, limit, filterByName, sort).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val members = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
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
                val filterByNone = NeutralFilterObject

                // Results can also be ordered with the QuerySortByField param
                // For example, this will order results by member creation time, descending
                val createdAtDescendingSort = QuerySortByField<Member>().desc("created_at")
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
                    when (result) {
                        is Result.Success -> {
                            val messages: List<Message> = result.value.messages
                            if (messages.size < pageSize) {
                                // All messages loaded
                            } else {
                                // Load next page
                                val nextRequest = QueryChannelRequest()
                                    .withMessages(LESS_THAN, messages.last().id, pageSize)
                                // ...
                            }
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/channel_capabilities/?language=kotlin">Capabilities</a>
         */
        inner class Capabilities {

            fun frontendCapabilities() {
                val channelClient = client.channel("messaging", "general")

                channelClient.query(QueryChannelRequest()).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val channel = result.value

                            val capabilities = channel.ownCapabilities
                            val userCanDeleteOwnMessage = capabilities.contains(ChannelCapabilities.DELETE_OWN_MESSAGE)
                            val userCanUpdateAnyMessage = capabilities.contains(ChannelCapabilities.UPDATE_ANY_MESSAGE)
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
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
                val data = mapOf("invites" to listOf("nick"))

                channelClient.create(memberIds = listOf("thierry", "tommaso"), extraData = data).enqueue { result ->
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
             * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#accepting-an-invite">Accept an Invite</a>
             */
            fun acceptingAnInvite() {
                channelClient.acceptInvite(
                    message = "Nick joined this channel!"
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
             * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#rejecting-an-invite">Rejecting an Invite</a>
             */
            fun rejectingAnInvite() {
                channelClient.rejectInvite().enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // Invite rejected
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
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
                    when (result) {
                        is Result.Success -> {
                            val channels = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
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
                    when (result) {
                        is Result.Success -> {
                            val channels = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }

            /**
             * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#query-for-pending-invites">Query For Pending Invites</a>
             */
            fun queryForPendingInvites() {
                val request = QueryChannelsRequest(
                    filter = Filters.eq("invite", "pending"),
                    offset = 0,
                    limit = 10
                )
                client.queryChannels(request).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            val channels = result.value
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }

        inner class MutingOrHidingChannels {
            /**
             * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=kotlin">Muting Channels</a>
             */
            inner class MutingChannels {

                /**
                 * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=kotlin#channel-mute">Channel Mute</a>
                 */
                fun channelMute() {
                    // Mute a channel
                    val channelClient = client.channel("messaging", "general")
                    channelClient.mute().enqueue { result ->
                        when (result) {
                            is Result.Success -> {
                                // Channel is muted
                            }
                            is Result.Failure -> {
                                // Handler error
                            }
                        }
                    }

                    // Mute a channel for 60 minutes
                    channelClient.mute(expiration = 60 * 60 * 1000).enqueue { result ->
                        when (result) {
                            is Result.Success -> {
                                // Channel is muted
                            }
                            is Result.Failure -> {
                                // Handler error
                            }
                        }
                    }

                    // Get list of muted channels when user is connected
                    client.connectUser(User("user-id"), "token")
                        .enqueue { result ->
                            when (result) {
                                is Result.Success -> {
                                    val user = result.value.user
                                    // Result contains the list of channel mutes
                                    val mutes: List<ChannelMute> = user.channelMutes
                                }
                                is Result.Failure -> {
                                    // Handler error
                                }
                            }
                        }

                    // Get updates about muted channels
                    client.subscribeFor<NotificationChannelMutesUpdatedEvent> { event ->
                        val mutes: List<ChannelMute> = event.me.channelMutes
                    }
                }

                /**
                 * @see <a href="https://getstream.io/chat/docs/android/muting_channels/?language=kotlin#check-if-user-is-muted">Check if User is Muted</a>
                 */
                fun checkIfUserIsMuted(channel: Channel, user: User) {
                    val isMuted = channel.isMutedFor(user)
                    if (isMuted) {
                        // Handle UI for muted channel
                    } else {
                        // Handle UI for not muted channel
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
                        when (result) {
                            is Result.Success -> {
                                val channels = result.value
                            }
                            is Result.Failure -> {
                                // Handler error
                            }
                        }
                    }
                }

                /**
                 * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=kotlin#remove-a-channel-mute">Remove a Channel Mute</a>
                 */
                fun removeAChannelMute() {
                    // Unmute channel for current user
                    channelClient.unmute().enqueue { result ->
                        when (result) {
                            is Result.Success -> {
                                // Channel is unmuted
                            }
                            is Result.Failure -> {
                                // Handler error
                            }
                        }
                    }
                }
            }

            inner class HidingChannel {

                /**
                 * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin#hiding-a-channel">Hiding a Channel</a>
                 */
                fun hidingAChannel() {
                    // Hides the channel until a new message is added there
                    channelClient.hide().enqueue { result ->
                        when (result) {
                            is Result.Success -> {
                                // Channel is hidden
                            }
                            is Result.Failure -> {
                                // Handler error
                            }
                        }
                    }

                    // Shows a previously hidden channel
                    channelClient.show().enqueue { result ->
                        when (result) {
                            is Result.Success -> {
                                // Channel is shown
                            }
                            is Result.Failure -> {
                                // Handler error
                            }
                        }
                    }

                    // Hide the channel and clear the message history
                    channelClient.hide(clearHistory = true).enqueue { result ->
                        when (result) {
                            is Result.Success -> {
                                // Channel is hidden
                            }
                            is Result.Failure -> {
                                // Handler error
                            }
                        }
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/disabling_channels/?language=kotlin">Disabling Channels</a>
         */
        inner class DisablingChannels {

            fun freeze() {
                val channelClient = client.channel("messaging", "general")

                channelClient.updatePartial(set = mapOf("frozen" to true)).enqueue { result ->
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

            fun unfreeze() {
                val channelClient = client.channel("messaging", "general")

                channelClient.updatePartial(unset = listOf("frozen")).enqueue { result ->
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
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Deleting Channels</a>
         */
        inner class DeletingChannels {

            fun deletingAChannel() {
                val channelClient = client.channel("messaging", "general")

                channelClient.delete().enqueue { result ->
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
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/channel_delete/?language=kotlin#truncate-a-channel">Truncate a Channel</a>
         */
        inner class TruncateAChannel {

            fun truncateAChannel() {
                // Removes all of the messages of the channel but doesn't affect the channel data or members
                channelClient.truncate().enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // Channel is truncated
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }

                // Or with message parameter.
                val message = Message(text = "Dear Everyone. The channel has been truncated.")
                channelClient.truncate(systemMessage = message).enqueue { result ->
                    when (result) {
                        is Result.Success -> {
                            // Channel is truncated
                        }
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
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
                    when (result) {
                        is Result.Success -> {
                            val channel = result.value
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
                        is Result.Failure -> {
                            // Handler error
                        }
                    }
                }
            }
        }
    }
}
