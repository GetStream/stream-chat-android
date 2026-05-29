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
import io.getstream.chat.android.client.events.ChatEvent
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
 * [DefaultChatEventHandler] that routes channels in and out of a grouped channel list based on the
 * channel's resolved group(s). Paired with `QueryChannelsIdentifier.Grouped(groupKey)` — one
 * handler instance per grouped query.
 *
 * For `cid`-only events the handler delegates to [DefaultChatEventHandler] and filters any
 * resulting `Add` through the resolver. Removal events are inherited unchanged.
 *
 * Overrides [handleChatEvent] (not [handleChannelEvent]) so the query layer's `cachedChannel` — a
 * per-channel snapshot that has already absorbed preceding `member.removed` events in the same
 * batch — is available for the membership check.
 */
internal class GroupAwareChatEventHandler(
    private val groupKey: String,
    private val resolver: ChannelGroupResolver,
    channels: StateFlow<Map<String, Channel>?>,
    clientState: ClientState,
) : DefaultChatEventHandler(channels, clientState) {

    override fun handleChatEvent(
        event: ChatEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult {
        return when (event) {
            is ChannelUpdatedEvent,
            is ChannelUpdatedByUserEvent,
            -> routeByGroup((event as HasChannel).channel, cachedChannel)

            is NotificationAddedToChannelEvent,
            is NotificationMessageNewEvent,
            is ChannelVisibleEvent,
            -> if (channelBelongsHere(event.channel)) {
                EventHandlingResult.WatchAndAdd(event.cid)
            } else {
                EventHandlingResult.Skip
            }

            else -> super.handleChatEvent(event, filter, cachedChannel)
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
     * The membership guard prevents a `ChannelUpdatedEvent` from (re-)adding a channel the user
     * has already left — relies on [cachedChannel] (in-memory state after `member.removed` is
     * applied) rather than `event.channel.membership`, which is not guaranteed on `channel.updated`.
     */
    private fun routeByGroup(channel: Channel, cachedChannel: Channel?): EventHandlingResult {
        val belongsHere = channelBelongsHere(channel) && isCurrentUserMember(cachedChannel)
        val isInList = channels.value?.containsKey(channel.cid) == true
        return when {
            belongsHere && !isInList -> EventHandlingResult.Add(channel)
            !belongsHere && isInList -> EventHandlingResult.Remove(channel.cid)
            else -> EventHandlingResult.Skip
        }
    }

    private fun channelBelongsHere(channel: Channel): Boolean =
        resolver.resolve(channel).contains(groupKey)

    /**
     * Reads `cachedChannel.membership`: the SDK maintains this field via member events and does
     * not overwrite it from `channel.updated`, so `membership == null` is the authoritative
     * "not a member" signal.
     *
     * Returns `false` whenever membership can't be positively confirmed — [routeByGroup] then
     * resolves that to `Remove` (channel currently in list) or `Skip` (not in list).
     */
    private fun isCurrentUserMember(cachedChannel: Channel?): Boolean {
        val currentUserId = clientState.user.value?.id ?: return false
        val membership = cachedChannel?.membership ?: return false
        return membership.getUserId() == currentUserId
    }

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
