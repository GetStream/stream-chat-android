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
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.state.event.handler.chat.DefaultChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import kotlinx.coroutines.flow.StateFlow

/**
 * [ChatEventHandler] that routes channels in and out of a grouped channel list based on the
 * channel's resolved group(s).
 *
 * Intended to be paired with `QueryChannelsIdentifier.Grouped(groupKey)` — one handler instance
 * per grouped query. On every event carrying full channel data (e.g. [ChannelUpdatedEvent]),
 * the handler asks the [resolver] which groups the channel belongs to:
 * - If [groupKey] is in the set and the channel is not currently in this list, [EventHandlingResult.Add].
 * - If [groupKey] is not in the set and the channel IS currently in this list, [EventHandlingResult.Remove].
 * - Otherwise [EventHandlingResult.Skip] (no state churn for re-adding already-present channels
 *   nor for ignoring non-members).
 *
 * For events that carry only a `cid` (e.g. [io.getstream.chat.android.client.events.MemberAddedEvent]),
 * the handler delegates to [DefaultChatEventHandler] and then filters any resulting `Add` through
 * the resolver, using the supplied `cachedChannel` as the input to the group lookup.
 *
 * Removal events (`ChannelDeletedEvent`, `ChannelHiddenEvent`, `MemberRemovedEvent` for the
 * current user, etc.) are inherited from [DefaultChatEventHandler] unchanged — leaving a channel
 * removes it from any list it was in, regardless of group.
 */
internal class GroupAwareChatEventHandler(
    private val groupKey: String,
    private val resolver: ChannelGroupResolver,
    channels: StateFlow<Map<String, Channel>?>,
    clientState: ClientState,
) : DefaultChatEventHandler(channels, clientState) {

    override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is ChannelUpdatedEvent,
            is ChannelUpdatedByUserEvent,
            -> routeByGroup(event.channel)

            is NotificationAddedToChannelEvent,
            is NotificationMessageNewEvent,
            is ChannelVisibleEvent,
            -> if (channelBelongsHere(event.channel)) {
                EventHandlingResult.WatchAndAdd(event.cid)
            } else {
                EventHandlingResult.Skip
            }

            else -> super.handleChannelEvent(event, filter)
        }
    }

    override fun handleCidEvent(
        event: CidEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult {
        val defaultResult = super.handleCidEvent(event, filter, cachedChannel)
        return filterResultByGroup(defaultResult, cachedChannel)
    }

    /**
     * Routes a channel-bearing event to Add / Remove / Skip based on the channel's resolved groups
     * and whether it is currently in this grouped list. Re-adding an already-present channel is
     * skipped — channel-state updates flow through a separate pipeline, so we don't churn the list.
     */
    private fun routeByGroup(channel: Channel): EventHandlingResult {
        val belongsHere = channelBelongsHere(channel)
        val isInList = channels.value?.containsKey(channel.cid) == true
        return when {
            belongsHere && !isInList -> EventHandlingResult.Add(channel)
            !belongsHere && isInList -> EventHandlingResult.Remove(channel.cid)
            else -> EventHandlingResult.Skip
        }
    }

    private fun channelBelongsHere(channel: Channel): Boolean =
        resolver.resolve(channel, groupKey).contains(groupKey)

    /**
     * Downgrades an `Add`/`WatchAndAdd` from the default handler to `Skip` if the resolver says
     * the channel does not belong in this group. `Remove`/`Skip` pass through unchanged.
     */
    private fun filterResultByGroup(
        result: EventHandlingResult,
        cachedChannel: Channel?,
    ): EventHandlingResult = when (result) {
        is EventHandlingResult.Add ->
            if (channelBelongsHere(result.channel)) result else EventHandlingResult.Skip

        is EventHandlingResult.WatchAndAdd ->
            // No channel data on the event; use cachedChannel if available. If we have nothing
            // to resolve against, trust the default and rely on the subsequent channel.updated
            // (which carries full channel data) to clean up.
            if (cachedChannel != null && !channelBelongsHere(cachedChannel)) {
                EventHandlingResult.Skip
            } else {
                result
            }

        else -> result
    }
}
