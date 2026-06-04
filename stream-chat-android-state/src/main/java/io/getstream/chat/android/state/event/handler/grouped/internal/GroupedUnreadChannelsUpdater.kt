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

package io.getstream.chat.android.state.event.handler.grouped.internal

import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasGroupedUnreadChannels
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.state.plugin.state.StateRegistry

/**
 * Single contract for evolving the per-group unread-channel counts map exposed via
 * `GlobalState.groupedUnreadChannels`.
 *
 * `channel.updated` / `channel.updated_by_user` deltas are computed against the pre-batch cached
 * channel, which is not refreshed until `updateChannelsState` runs after the global-state pass.
 * To stay correct against same-cid events earlier in the same batch (mark-read, deletion, removal,
 * etc.) the updater keeps batch-scoped overrides keyed on `BatchEvent.id` and auto-cleared when a
 * new batch id arrives.
 */
internal class GroupedUnreadChannelsUpdater(
    private val stateRegistry: StateRegistry,
    private val currentUserId: UserId,
) {

    private var memoBatchId: Int? = null
    private val processedCids = mutableSetOf<String>()
    private val removedCids = mutableSetOf<String>()
    private val hadUnreadOverride = mutableMapOf<String, Boolean>()
    private var markAllReadApplied: Boolean = false

    /**
     * Result of a `queryGroupedChannels` call. Per-group counts are MERGED into the current map
     * so groups not present in the response retain their existing counts.
     */
    fun calculateUpdatedCounts(
        current: Map<String, Int>,
        result: GroupedChannels,
    ): Map<String, Int> = current + result.groups.mapValues { (_, g) -> g.unreadChannels }

    /**
     * Backend-pushed authoritative map (from any [HasGroupedUnreadChannels] event). The event's
     * map REPLACES the current one (or returns it unchanged if the event carries no map). The
     * event subtype is then inspected to flip the per-batch overrides for its cid so subsequent
     * `channel.updated` deltas in the same batch see the post-event state.
     */
    fun calculateUpdatedCounts(
        current: Map<String, Int>,
        batchId: Int,
        event: HasGroupedUnreadChannels,
    ): Map<String, Int> {
        rotateBatchIfNeeded(batchId)
        val next = event.groupedUnreadChannels ?: current
        if (next !== current) {
            // The map was replaced, so the per-batch dedup no longer applies to later events.
            processedCids.clear()
        }
        recordOverridesFrom(event)
        return next
    }

    /**
     * `channel.updated` delta. If the channel changed group and the current user still has
     * unread on it (per the in-batch overrides + cached state), decrement the old group's count
     * and increment the new group's count.
     */
    fun calculateUpdatedCounts(
        current: Map<String, Int>,
        batchId: Int,
        event: ChannelUpdatedEvent,
    ): Map<String, Int> = applyDelta(current, batchId, event.cid, event.channel)

    /**
     * `channel.updated_by_user` delta. Same semantics as the [ChannelUpdatedEvent] overload.
     */
    fun calculateUpdatedCounts(
        current: Map<String, Int>,
        batchId: Int,
        event: ChannelUpdatedByUserEvent,
    ): Map<String, Int> = applyDelta(current, batchId, event.cid, event.channel)

    /**
     * Records that channel [cid] has been removed within [batchId] — either deleted, or the
     * current user is no longer a member. Later in-batch deltas for that cid are skipped.
     */
    fun notifyChannelRemoved(batchId: Int, cid: String) {
        rotateBatchIfNeeded(batchId)
        removedCids += cid
    }

    private fun recordOverridesFrom(event: HasGroupedUnreadChannels) {
        when (event) {
            is NotificationMarkReadEvent -> hadUnreadOverride[event.cid] = false
            is NotificationMarkUnreadEvent -> hadUnreadOverride[event.cid] = true
            is NewMessageEvent ->
                if (event.user.id != currentUserId) hadUnreadOverride[event.cid] = true
            is NotificationMessageNewEvent -> hadUnreadOverride[event.cid] = true
            is NotificationChannelDeletedEvent -> removedCids += event.cid
            is NotificationChannelTruncatedEvent -> hadUnreadOverride[event.cid] = false
            is MarkAllReadEvent -> {
                markAllReadApplied = true
                hadUnreadOverride.clear()
            }
        }
    }

    private fun applyDelta(
        current: Map<String, Int>,
        batchId: Int,
        cid: String,
        newChannel: Channel,
    ): Map<String, Int> {
        rotateBatchIfNeeded(batchId)
        if (cid in removedCids || cid in processedCids) return current
        val oldChannel = cachedChannel(cid) ?: return current
        val oldGroup = oldChannel.group
        val newGroup = newChannel.group
        if (oldGroup == newGroup) return current
        if (!hadUnreadFor(cid, oldChannel)) return current
        val next = current.toMutableMap().apply {
            oldGroup?.let { this[it] = ((this[it] ?: 0) - 1).coerceAtLeast(0) }
            newGroup?.let { this[it] = (this[it] ?: 0) + 1 }
        }
        if (next == current) return current
        processedCids += cid
        return next
    }

    private fun hadUnreadFor(cid: String, oldChannel: Channel): Boolean = when {
        cid in hadUnreadOverride -> hadUnreadOverride.getValue(cid)
        markAllReadApplied -> false
        else -> oldChannel.currentUserUnreadCount(currentUserId) > 0
    }

    private fun rotateBatchIfNeeded(batchId: Int) {
        if (memoBatchId == batchId) return
        memoBatchId = batchId
        processedCids.clear()
        removedCids.clear()
        hadUnreadOverride.clear()
        markAllReadApplied = false
    }

    private fun cachedChannel(cid: String): Channel? {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return if (stateRegistry.isActiveChannel(channelType, channelId)) {
            stateRegistry.mutableChannel(channelType, channelId).toChannel()
        } else {
            null
        }
    }
}
