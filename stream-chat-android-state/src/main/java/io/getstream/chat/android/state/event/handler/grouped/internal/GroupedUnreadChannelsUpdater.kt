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
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.state.plugin.state.StateRegistry

/**
 * Single contract for evolving the per-group unread-channel counts map exposed via
 * `GlobalState.groupedUnreadChannels`. Each method is a pure calculator: it takes the current
 * map plus an input (event or API result) and returns the next map. Callers are responsible
 * for writing the result back via `MutableGlobalState.setGroupedUnreadChannels(...)`.
 */
internal class GroupedUnreadChannelsUpdater(
    private val stateRegistry: StateRegistry,
    private val currentUserId: UserId,
) {

    /**
     * Backend-pushed authoritative map (from any [HasGroupedUnreadChannels] event).
     * The backend's value REPLACES the current map. If the event carries no map
     * (`groupedUnreadChannels == null`), the current map is preserved.
     */
    fun calculateUpdatedCounts(
        current: Map<String, Int>,
        event: HasGroupedUnreadChannels,
    ): Map<String, Int> = event.groupedUnreadChannels ?: current

    /**
     * Result of a `queryGroupedChannels` call. Per-group counts are MERGED into the current map
     * so groups not present in the response retain their existing counts.
     */
    fun calculateUpdatedCounts(
        current: Map<String, Int>,
        result: GroupedChannels,
    ): Map<String, Int> = current + result.groups.mapValues { (_, g) -> g.unreadChannels }

    /**
     * `channel.updated` delta. If the channel's `group` field changed and the cached channel
     * had unread for the current user, decrement the old group's count and increment the new
     * group's count. See [applyGroupChange].
     */
    fun calculateUpdatedCounts(
        current: Map<String, Int>,
        event: ChannelUpdatedEvent,
    ): Map<String, Int> = applyGroupChange(current, event.cid, event.channel)

    /**
     * `channel.updated_by_user` delta. Same semantics as the [ChannelUpdatedEvent] overload.
     */
    fun calculateUpdatedCounts(
        current: Map<String, Int>,
        event: ChannelUpdatedByUserEvent,
    ): Map<String, Int> = applyGroupChange(current, event.cid, event.channel)

    private fun applyGroupChange(
        current: Map<String, Int>,
        cid: String,
        newChannel: Channel,
    ): Map<String, Int> {
        val oldChannel = cachedChannel(cid) ?: return current
        val oldGroup = oldChannel.group
        val newGroup = newChannel.group
        if (oldGroup == newGroup) return current
        if (oldChannel.currentUserUnreadCount(currentUserId) == 0) return current
        return current.toMutableMap().apply {
            oldGroup?.let { this[it] = ((this[it] ?: 0) - 1).coerceAtLeast(0) }
            newGroup?.let { this[it] = (this[it] ?: 0) + 1 }
        }
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
