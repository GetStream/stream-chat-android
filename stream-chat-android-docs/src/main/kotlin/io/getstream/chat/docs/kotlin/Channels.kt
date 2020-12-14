package io.getstream.chat.docs.kotlin

import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.ChannelsMuteEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.docs.StaticInstances.TAG

class Channels(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/initialize_channel/?language=kotlin">Channel Initialization</a>
     */
    inner class ChannelInitialization {
        fun initialization() {
            // Create channel client using channel type and channel id
            val channelClient = client.channel("channel-type", "channel-id")

            // Or create channel client using channel cid
            val anotherChannelClient = client.channel("cid")
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/creating_channels/?language=kotlin">Creating Channels</a>
     */
    inner class CreatingChannels {
        fun createAChannel() {
            val channelType = "messaging"
            val channelId = "id"
            val extraData = emptyMap<String, Any>()
            client.createChannel(channelType, channelId, extraData).enqueue { result ->
                if (result.isSuccess) {
                    val newChannel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=kotlin">Watching A Channel</a>
     */
    inner class WatchingAChannel {
        fun watchingChannel() {
            channelClient.watch().enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=kotlin#unwatching">Unwacthing</a>
         */
        fun stopWatchingChannel() {
            channelClient.stopWatching().enqueue { result ->
                if (result.isSuccess) {
                    // Channel unwatched
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Querying Channels</a>
     */
    inner class QueryingChannels {
        fun queryChannels() {
            val filter = Filters.`in`("members", "thierry").put("type", "messaging")
            val offset = 0
            val limit = 10
            val sort = QuerySort.desc<Channel>("last_message_at")
            val request = QueryChannelsRequest(filter, offset, limit, sort).apply {
                watch = true
                state = true
            }

            client.queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    val channels = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin#common-filters-by-use-case">Common Filters</a>
         */
        inner class CommonFilters {
            fun channelsThatContainsSpecificUser() {
                val filter = Filters
                    .`in`("members", "thierry")
                    .put("type", "messaging")
            }

            fun channelsThatWithSpecificStatus() {
                val filter = Filters
                    .`in`("status", "pending", "open", "new")
                    .put("agent_id", "user-id")
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
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }

            // Get the second 10 channels
            val nextOffset = 10
            val nextRequest = QueryChannelsRequest(filter, nextOffset, limit)
            client.queryChannels(nextRequest).enqueue { result ->
                if (result.isSuccess) {
                    val channels = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_pagination/?language=kotlin">Channel Pagination</a>
     */
    inner class ChannelPagination {

        private val pageSize = 10

        // Get the first 10 messages
        fun loadFirstPage() {
            val firstPage = QueryChannelRequest().withMessages(pageSize)
            client.queryChannel("channel-type", "channel-id", firstPage).enqueue { result ->
                if (result.isSuccess) {
                    val messages: List<Message> = result.data().messages
                    if (messages.size < pageSize) {
                        // All messages loaded
                    } else {
                        loadSecondPage(messages.last().id)
                    }
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }

        // Get the second 10 messages
        fun loadSecondPage(lastMessageId: String) {
            val secondPage = QueryChannelRequest().withMessages(Pagination.LESS_THAN, lastMessageId, pageSize)
            client.queryChannel("channel-type", "channel-id", secondPage).enqueue { result ->
                if (result.isSuccess) {
                    val messages: List<Message> = result.data().messages
                    if (messages.size < pageSize) {
                        // All messages loaded
                    } else {
                        // Load another page
                    }
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_update/?language=kotlin">Updating a Channel</a>
     */
    inner class UpdatingAChannel {
        fun updateChannel() {
            val channelData = mapOf("color" to "green")
            val updateMessage = Message().apply {
                text = "Thierry changed the channel color to green"
            }
            channelClient.update(updateMessage, channelData).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
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
            // Add member with id "thierry" and "josh"
            channelClient.addMembers("thierry", "josh").enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }

            // Remove member with id "thierry" and "josh"
            channelClient.removeMembers("thierry", "josh").enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
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
            val members = listOf("thierry", "tomasso")
            val channelType = "messaging"
            client.createChannel(channelType, members).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_conversations/?language=kotlin">Invites</a>
     */
    inner class Invites {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#inviting-users">Iniviting Users</a>
         */
        fun invitingUsers() {
            val members = listOf("thierry", "tommaso")
            val invites = listOf("nick")
            val data = mapOf(
                "members" to members,
                "invites" to invites
            )

            channelClient.create(data).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#accepting-an-invite">Accept an Invite</a>
         */
        fun acceptingAnInvite() {
            channelClient.acceptInvite("Nick joined this channel!").enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#rejecting-an-invite">Rejecting an Invite</a>
         */
        fun rejectingAnInvite() {
            channelClient.rejectInvite().enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#query-for-accepted-invites">Query For Accepted Invites</a>
         */
        fun queryForAcceptedInvites() {
            val offset = 0
            val limit = 10
            val request = QueryChannelsRequest(FilterObject("invite", "accepted"), offset, limit)
            client.queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    val channels = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=kotlin#query-for-rejected-invites">Query For Rejected Invites</a>
         */
        fun queryForRejectedInvites() {
            val offset = 0
            val limit = 10
            val request = QueryChannelsRequest(FilterObject("invite", "rejected"), offset, limit)
            client.queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    val channels = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Deleting & Hiding a Channel</a>
     */
    inner class DeletingAndHidingAChannel {

        fun deletingAChannel() {
            channelClient.delete().enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
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
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }

            // Shows a previously hidden channel
            channelClient.show().enqueue { result ->
                if (result.isSuccess) {
                    // Channel is shown
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }

            // Hide the channel and clear the message history
            channelClient.hide(clearHistory = true).enqueue { result ->
                if (result.isSuccess) {
                    // Channel is hidden
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
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
        fun channelMute() {
            client.muteChannel("channel-type", "channel-id").enqueue { result ->
                if (result.isSuccess) {
                    // Channel is muted
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }

            // Get list of muted channels when user is connected
            client.setUser(
                User("user-id"),
                "token",
                object : InitConnectionListener() {
                    override fun onSuccess(data: ConnectionData) {
                        val user = data.user
                        // Mutes contains the list of channel mutes
                        val mutes: List<ChannelMute> = user.channelMutes
                    }
                }
            )

            // Get updates about muted channels
            client.subscribeFor<ChannelsMuteEvent> { event ->
                val mutes: List<ChannelMute> = event.channelsMute
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=kotlin#query-muted-channels">Query Muted Channels</a>
         */
        fun queryMutedChannels() {
            // Retrieve channels excluding muted ones
            val offset = 0
            val limit = 10
            val notMutedFilter = Filters.eq("muted", false)
            client.queryChannels(QueryChannelsRequest(notMutedFilter, offset, limit)).enqueue { result ->
                if (result.isSuccess) {
                    val channels = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }

            // Retrieve muted channels
            val mutedFilter = Filters.eq("muted", true)
            client.queryChannels(QueryChannelsRequest(mutedFilter, offset, limit)).enqueue { result ->
                if (result.isSuccess) {
                    val channels = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
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
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_members/?language=kotlin">Query Members</a>
     */
    inner class QueryMembers {
        fun queryingMembers() {
            val offset = 0
            val limit = 10
            val sort = QuerySort<Member>()

            // We can query channel members with specific filters
            // 1. Create the filters query, e.g query members by user name
            val filterByName = Filters.eq("name", "tommaso")

            // 2. Call queryMembers with that filter
            channelClient.queryMembers(offset, limit, filterByName, sort).enqueue { result ->
                if (result.isSuccess) {
                    val members = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().cause)
                }
            }

            // Here are some commons filters you can use:
            // Autocomplete members by user name
            val filterByAutoCompleteName = Filters.autocomplete("name", "tommaso")

            // Query member by id
            val filterById = Filters.eq("id", "tommaso")

            // Query multiple members by id
            val filterByIds = Filters.`in`("id", "tommaso", "thierry")

            // Query channel moderators
            val filterByModerator = Filters.eq("is_moderator", true)

            // Query for banned members in channel
            val filterByBannedMembers = Filters.eq("banned", true)

            // Query members with pending invites
            val filterByPendingInvite = Filters.eq("invite", "pending")

            // Query all the members
            val filterByNone = FilterObject()

            // We can order the results too with QuerySort param
            // Here example to order results by member created at descending
            val createdAtDescendingSort = QuerySort<Member>().desc("created_at")
            channelClient.queryMembers(offset, limit, FilterObject(), createdAtDescendingSort)
                .enqueue { result ->
                    if (result.isSuccess) {
                        val members = result.data()
                    } else {
                        Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                    }
                }
        }
    }
}
