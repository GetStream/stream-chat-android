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
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.mergeChannelFromEvent
import io.getstream.log.taggedLogger
import java.util.Date

/**
 * Stores what the server returns for the channel changes the current user makes, so they show at once and stay when
 * the matching event is late or missed. The event still arrives and is applied as usual; every change here sets
 * values from the server rather than counting, so applying both gives the same result.
 *
 * A response older than the stored channel (an earlier `updatedAt`) is ignored, so it cannot undo a newer change.
 */
internal class ChannelUpdateResponseHandler(
    private val logic: LogicRegistry,
    private val repos: RepositoryFacade,
    private val mutableGlobalState: MutableGlobalState,
    private val getCurrentUserId: () -> String?,
) {

    private val logger by taggedLogger("Chat:ChannelUpdateResponse")

    /**
     * Applies the channel returned by an update or a partial update.
     */
    suspend fun onChannelUpdated(channel: Channel) {
        applyChannel(channel)
    }

    /**
     * Applies the channel returned by a member addition or invite. A response that does not list every added member
     * (it lists a limited number of members) is left to the events: `member.added` counts a member only when it is
     * not listed yet, so a count taken from such a response would be raised twice.
     */
    suspend fun onMembersAdded(channel: Channel, addedMemberIds: List<String>) {
        val listedIds = channel.members.mapTo(mutableSetOf()) { it.getUserId() }
        if (!listedIds.containsAll(addedMemberIds)) {
            logger.d { "[onMembersAdded] skipped; ${channel.cid} response does not list every added member" }
            return
        }
        applyChannel(channel)
    }

    /**
     * Stores the channel returned by a member removal. The channel state waits for `member.removed`: it lowers the
     * member count on every removal, as its member list can be partial, so a count taken from the response first
     * would be lowered twice. When the current user left, the channel is left to the events: they also remove the
     * membership and the channel from the lists.
     */
    suspend fun onMembersRemoved(channel: Channel, removedMemberIds: List<String>) {
        if (getCurrentUserId() in removedMemberIds) {
            logger.d { "[onMembersRemoved] skipped; the current user left ${channel.cid}" }
            return
        }
        saveChannel(channel, removedMemberIds.toSet())
    }

    /**
     * Applies the channel returned by a truncation and removes the messages it removed.
     */
    suspend fun onChannelTruncated(channel: Channel) {
        val truncatedAt = channel.truncatedAt ?: return applyChannel(channel)
        repos.deleteChannelMessagesBefore(channel.cid, truncatedAt)
        activeChannel(channel)?.truncate(truncatedAt)
        applyChannel(channel)
    }

    /**
     * Marks the channel returned by a deletion as deleted and removes its messages. The channel stays in the lists:
     * removing it is up to the [io.getstream.chat.android.client.api.event.ChatEventHandler] when the event arrives.
     */
    suspend fun onChannelDeleted(channel: Channel) {
        val deletedAt = channel.deletedAt ?: return
        repos.deleteChannelMessagesBefore(channel.cid, deletedAt)
        repos.setChannelDeletedAt(channel.cid, deletedAt)
        activeChannel(channel)?.deleteChannel(deletedAt)
        refreshChannelLists(channel.cid)
    }

    /**
     * Adds the [mutes] returned by a channel mute to the current user's channel mutes.
     */
    fun onChannelMuted(channelType: String, channelId: String, mutes: List<ChannelMute>) {
        val cid = "$channelType:$channelId"
        val mutedCids = mutes.mapNotNullTo(mutableSetOf(cid)) { it.channel?.cid }
        if (mutes.isNotEmpty()) {
            val kept = mutableGlobalState.channelMutes.value.filterNot { it.channel?.cid in mutedCids }
            mutableGlobalState.setChannelMutes(kept + mutes)
        }
        setMuted(channelType, channelId, muted = true)
    }

    /**
     * Removes the channel from the current user's channel mutes after a channel unmute.
     */
    fun onChannelUnmuted(channelType: String, channelId: String) {
        val cid = "$channelType:$channelId"
        val current = mutableGlobalState.channelMutes.value
        if (current.any { it.channel?.cid == cid }) {
            mutableGlobalState.setChannelMutes(current.filterNot { it.channel?.cid == cid })
        }
        setMuted(channelType, channelId, muted = false)
    }

    private suspend fun applyChannel(channel: Channel) {
        saveChannel(channel)
        val channelLogic = activeChannel(channel) ?: return refreshChannelLists(channel.cid)
        val stateUpdatedAt = channelLogic.messagesUpdateLogic.channelState().channelData.value.updatedAt
        if (stateUpdatedAt.isAfter(channel.updatedAt)) {
            logger.d { "[applyChannel] state of ${channel.cid} is newer than the response" }
        } else {
            channelLogic.applyChannelUpdate(channel)
        }
        refreshChannelLists(channel.cid)
    }

    private suspend fun saveChannel(channel: Channel, removedMemberIds: Set<String> = emptySet()) {
        // A channel not stored yet is left to the next query: the response has no messages or reads.
        val stored = repos.selectChannel(channel.cid) ?: return
        if (stored.updatedAt.isAfter(channel.updatedAt)) {
            logger.d { "[saveChannel] stored ${channel.cid} is newer than the response" }
            return
        }
        val merged = stored.mergeChannelFromEvent(channel).copy(hidden = channel.hidden ?: stored.hidden)
        repos.insertChannel(merged.copy(members = merged.members.filterNot { it.getUserId() in removedMemberIds }))
    }

    private fun setMuted(channelType: String, channelId: String, muted: Boolean) {
        if (logic.isActiveChannel(channelType, channelId)) {
            logic.channel(channelType, channelId).setMuted(muted)
            refreshChannelLists("$channelType:$channelId")
        }
    }

    // Only a channel with state is updated: creating one here would add a channel with no messages to the lists.
    private fun activeChannel(channel: Channel): ChannelLogic? =
        if (logic.isActiveChannel(channel.type, channel.id)) logic.channel(channel.type, channel.id) else null

    private fun refreshChannelLists(cid: String) {
        logic.getActiveQueryChannelsLogic().forEach { it.refreshChannelState(cid) }
    }

    private fun Date?.isAfter(other: Date?): Boolean = this != null && other != null && this.after(other)
}
