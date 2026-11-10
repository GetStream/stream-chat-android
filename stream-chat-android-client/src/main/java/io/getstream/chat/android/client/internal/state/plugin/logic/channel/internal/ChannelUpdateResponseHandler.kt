/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.core.utils.date.max
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.mergeChannelFromEvent
import io.getstream.chat.android.models.mergeFromEvent
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CancellationException
import java.util.Date

/**
 * Stores what the server returns for the channel changes the current user makes, so they show at once and stay when
 * the matching event is late or missed. The event still arrives and sets the same fields again.
 *
 * A response can arrive after events that are newer than it, so it never takes back newer data:
 * - A response older than the stored channel or the open state (an earlier `updatedAt`) is ignored.
 * - The last message date and the message count keep the later value, as new messages do not move `updatedAt`.
 * - The members and the member count are taken only from a member addition or removal response.
 *
 * A failure here is logged and never changes the result of the request.
 */
internal class ChannelUpdateResponseHandler(
    private val logic: LogicRegistry,
    private val repos: RepositoryFacade,
    private val mutableGlobalState: MutableGlobalState,
    private val getCurrentUserId: () -> String?,
) {

    private val logger by taggedLogger("Chat:ChannelUpdateResponse")

    /**
     * Applies the channel returned by an update or a partial update. The members are kept as they are: these
     * requests do not change them, and a member event processed before the response is newer.
     */
    suspend fun onChannelUpdated(channel: Channel) = safely("onChannelUpdated") {
        applyChannel(channel, withMembers = false)
    }

    /**
     * Applies the channel returned by a member addition or invite. A response that does not list every added member
     * (it lists a limited number of members) is left to the events: `member.added` counts a member only when it is
     * not listed yet, so a count taken from such a response would be raised twice.
     */
    suspend fun onMembersAdded(channel: Channel, addedMemberIds: List<String>) = safely("onMembersAdded") {
        val listedIds = channel.members.mapTo(mutableSetOf()) { it.getUserId() }
        if (listedIds.containsAll(addedMemberIds)) {
            applyChannel(channel, withMembers = true)
        } else {
            logger.d { "[onMembersAdded] skipped; ${channel.cid} response does not list every added member" }
        }
    }

    /**
     * Stores the channel returned by a member removal. The open channel state waits for `member.removed`: it still
     * lists the removed member and lowers the member count when the event arrives, so a count taken from the response
     * first would be lowered twice. When the current user left, the channel is left to the events: they also remove
     * the membership and the channel from the lists.
     */
    suspend fun onMembersRemoved(channel: Channel, removedMemberIds: List<String>) = safely("onMembersRemoved") {
        if (getCurrentUserId() in removedMemberIds) {
            logger.d { "[onMembersRemoved] skipped; the current user left ${channel.cid}" }
        } else {
            saveChannel(channel, withMembers = true, removedMemberIds = removedMemberIds.toSet())
        }
    }

    /**
     * Applies the channel returned by a truncation and removes the messages it removed.
     */
    suspend fun onChannelTruncated(channel: Channel) = safely("onChannelTruncated") {
        channel.truncatedAt?.let { truncatedAt ->
            repos.deleteChannelMessagesBefore(channel.cid, truncatedAt)
            activeChannel(channel)?.truncate(truncatedAt)
        }
        applyChannel(channel, withMembers = false)
    }

    /**
     * Marks the channel returned by a deletion as deleted and removes its messages. The channel stays in the lists:
     * removing it is up to the [io.getstream.chat.android.client.api.event.ChatEventHandler] when the event arrives.
     */
    suspend fun onChannelDeleted(channel: Channel) = safely("onChannelDeleted") {
        val deletedAt = channel.deletedAt ?: return@safely
        repos.deleteChannelMessagesBefore(channel.cid, deletedAt)
        repos.setChannelDeletedAt(channel.cid, deletedAt)
        activeChannel(channel)?.deleteChannel(deletedAt)
        refreshChannelLists(channel.cid)
    }

    /**
     * Applies the current user's [channelMutes] after a channel mute or unmute of the channel.
     *
     * @param muted True if the channel was muted, false if it was unmuted.
     * @param channelMutes The current user's channel mutes after the request.
     */
    fun onChannelMuteChanged(
        channelType: String,
        channelId: String,
        muted: Boolean,
        channelMutes: List<ChannelMute>,
    ) = safely("onChannelMuteChanged") {
        mutableGlobalState.setChannelMutes(channelMutes)
        if (logic.isActiveChannel(channelType, channelId)) {
            logic.channel(channelType, channelId).setMuted(muted)
            refreshChannelLists("$channelType:$channelId")
        }
    }

    private suspend fun applyChannel(channel: Channel, withMembers: Boolean) {
        saveChannel(channel, withMembers)
        val channelLogic = activeChannel(channel) ?: return
        val stateUpdatedAt = channelLogic.messagesUpdateLogic.channelState().channelData.value.updatedAt
        if (stateUpdatedAt.isAfter(channel.updatedAt)) {
            logger.d { "[applyChannel] state of ${channel.cid} is newer than the response" }
            return
        }
        channelLogic.applyChannelUpdate(channel, withMembers)
        refreshChannelLists(channel.cid)
    }

    private suspend fun saveChannel(
        channel: Channel,
        withMembers: Boolean,
        removedMemberIds: Set<String> = emptySet(),
    ) {
        // A channel not stored yet is left to the next query: the response has no messages or reads.
        val stored = repos.selectChannel(channel.cid) ?: return
        if (stored.updatedAt.isAfter(channel.updatedAt)) {
            logger.d { "[saveChannel] stored ${channel.cid} is newer than the response" }
            return
        }
        val merged = stored.mergeChannelFromEvent(channel)
        repos.insertChannel(
            merged.copy(
                hidden = channel.hidden ?: stored.hidden,
                lastMessageAt = max(stored.lastMessageAt, channel.lastMessageAt),
                messageCount = maxOfOrNull(stored.messageCount, channel.messageCount),
                members = if (withMembers) {
                    merged.members.filterNot { it.getUserId() in removedMemberIds }
                } else {
                    stored.members
                },
                memberCount = if (withMembers) merged.memberCount else stored.memberCount,
            ),
        )
    }

    // Only a channel with state is updated: creating one here would add a channel with no messages to the lists.
    private fun activeChannel(channel: Channel): ChannelLogic? =
        if (logic.isActiveChannel(channel.type, channel.id)) logic.channel(channel.type, channel.id) else null

    private fun refreshChannelLists(cid: String) {
        logic.getActiveQueryChannelsLogic().forEach { it.refreshChannelState(cid) }
    }

    @Suppress("TooGenericExceptionCaught")
    private inline fun safely(operation: String, block: () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "[$operation] failed: ${e.message}" }
        }
    }
}

/**
 * Applies the channel data returned by a channel update of the current user to this data. Unlike
 * [ChannelData.mergeFromEvent], newer data that the response can predate is kept: the later last message date, the
 * higher message count and, unless [withMembers], the member count.
 */
internal fun ChannelData.mergeChannelUpdate(update: ChannelData, withMembers: Boolean): ChannelData =
    mergeFromEvent(update).copy(
        lastMessageAt = max(lastMessageAt, update.lastMessageAt),
        messageCount = maxOfOrNull(messageCount, update.messageCount),
        memberCount = if (withMembers) update.memberCount else memberCount,
    )

private fun maxOfOrNull(a: Int?, b: Int?): Int? = listOfNotNull(a, b).maxOrNull()

private fun Date?.isAfter(other: Date?): Boolean = this != null && other != null && this.after(other)
