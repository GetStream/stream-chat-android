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
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelPartialUpdatedEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.event.handler.chat.DefaultChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.utils.internal.calculateNewLastMessageAt
import io.getstream.chat.android.state.utils.internal.mergePartialUpdate
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

/**
 * [DefaultChatEventHandler] that routes channels in and out of a grouped channel list based on the
 * channel's resolved group(s) and the group's filter. Paired with `QueryChannelsIdentifier.Grouped(groupKey)` — one
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
    private val now: () -> Date = ::Date,
) : DefaultChatEventHandler(channels, clientState) {

    override fun handleChatEvent(
        event: ChatEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult {
        return when (event) {
            is ChannelUpdatedEvent,
            is ChannelUpdatedByUserEvent,
            -> routeByGroup((event as HasChannel).channel, cachedChannel, filter)

            is NotificationChannelPartialUpdatedEvent -> routePartialUpdate(event, cachedChannel, filter)

            is NotificationAddedToChannelEvent,
            is NotificationMessageNewEvent,
            is ChannelVisibleEvent,
            -> if (channelBelongsHere(event.channel, filter)) {
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
        // The cached channel can predate the new message, which may bring it back within a cutoff.
        val newMessage = (event as? NewMessageEvent)?.message
        return filterResultByGroup(defaultResult, cachedChannel, filter, newMessage)
    }

    /**
     * The membership guard prevents a `ChannelUpdatedEvent` from (re-)adding a channel the user
     * has already left — relies on [cachedChannel] (in-memory state after `member.removed` is
     * applied) rather than `event.channel.membership`, which is not guaranteed on `channel.updated`.
     *
     * The hidden guard does the same for a channel the user has hidden: `hidden` is per-user and
     * absent from `channel.updated` payloads, so it is read from [cachedChannel], which has already
     * absorbed a preceding `channel.hidden` in the same batch. A hidden channel stays out of the
     * list until a new message or `channel.visible` clears the flag.
     */
    private fun routeByGroup(channel: Channel, cachedChannel: Channel?, filter: FilterObject): EventHandlingResult {
        val belongsHere = channelBelongsHere(channel, filter) &&
            isCurrentUserMember(cachedChannel) &&
            cachedChannel?.hidden != true
        val isInList = channels.value?.containsKey(channel.cid) == true
        return when {
            belongsHere && !isInList -> EventHandlingResult.Add(channel)
            !belongsHere && isInList -> EventHandlingResult.Remove(channel.cid)
            else -> EventHandlingResult.Skip
        }
    }

    /**
     * Same checks as [routeByGroup], against [cachedChannel] with the partial update applied. The event's channel
     * lacks data such as members and the last message time, so a channel that is not in the list yet is watched to
     * get its full data instead of being added from the event.
     *
     * The cutoff is checked only for a channel in the list, whose last activity is up to date. For any other
     * channel only the group is checked: without [cachedChannel] the last activity is unknown, and a stored copy can
     * be stale, so the cutoff is left to the list, which checks the watched channel before showing it. The event is
     * only sent to members, and a hidden channel is dropped after the watch. The query skips the watch when the
     * channel sorts below its loaded pages.
     */
    private fun routePartialUpdate(
        event: NotificationChannelPartialUpdatedEvent,
        cachedChannel: Channel?,
        filter: FilterObject,
    ): EventHandlingResult {
        val channel = cachedChannel?.mergePartialUpdate(event.channel) ?: event.channel
        val isInList = channels.value?.containsKey(event.cid) == true
        val matches = if (isInList) channelBelongsHere(channel, filter) else groupMatches(channel, filter)
        val belongsHere = matches &&
            (cachedChannel == null || (isCurrentUserMember(cachedChannel) && cachedChannel.hidden != true))
        return when {
            belongsHere && !isInList -> EventHandlingResult.WatchAndAdd(event.cid)
            !belongsHere && isInList -> EventHandlingResult.Remove(event.cid)
            else -> EventHandlingResult.Skip
        }
    }

    /** Whether [channel] belongs in this group. A disabled channel never does, since the backend leaves it out. */
    private fun channelBelongsHere(channel: Channel, filter: FilterObject): Boolean =
        !channel.isDisabled &&
            resolver.resolve(channel).contains(groupKey) &&
            filter.matchesGroupedChannel(group = channel.group, queryTimestamp = channel.queryTimestamp, now = now())

    /** [channelBelongsHere] with the cutoff passed, for a channel whose last activity is unknown or may be stale. */
    private fun groupMatches(channel: Channel, filter: FilterObject): Boolean {
        val checkedAt = now()
        return !channel.isDisabled &&
            resolver.resolve(channel).contains(groupKey) &&
            filter.matchesGroupedChannel(group = channel.group, queryTimestamp = checkedAt, now = checkedAt)
    }

    /** The channel with [newMessage] counted in its last message time, as the channel state counts it. */
    private fun Channel.withNewMessage(newMessage: Message?): Channel = when (newMessage) {
        null -> this
        else -> copy(
            lastMessageAt = calculateNewLastMessageAt(
                message = newMessage,
                currentLastMessageAt = lastMessageAt,
                skipLastMsgUpdateForSystemMsgs = false,
            ),
        )
    }

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
     * Downgrades an `Add`/`WatchAndAdd` from the default handler to `Skip` if the channel does not
     * belong in this group or does not match its filter. `Remove`/`Skip` pass through unchanged.
     */
    private fun filterResultByGroup(
        result: EventHandlingResult,
        cachedChannel: Channel?,
        filter: FilterObject,
        newMessage: Message?,
    ): EventHandlingResult = when (result) {
        // The stored copy carries the new activity, so it still matches when the list is checked again.
        is EventHandlingResult.Add -> result.channel.withNewMessage(newMessage).let { channel ->
            if (channelBelongsHere(channel, filter)) EventHandlingResult.Add(channel) else EventHandlingResult.Skip
        }

        is EventHandlingResult.WatchAndAdd ->
            // No channel data on the event; use cachedChannel if available. If we have nothing
            // to resolve against, trust the default and rely on the subsequent channel.updated
            // (which carries full channel data) to clean up.
            if (cachedChannel != null && !channelBelongsHere(cachedChannel.withNewMessage(newMessage), filter)) {
                EventHandlingResult.Skip
            } else {
                result
            }

        else -> result
    }
}
