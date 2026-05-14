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

package io.getstream.chat.android.state.event.handler.chat

import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import kotlinx.coroutines.flow.StateFlow

/**
 * [ChatEventHandler] that routes channels in and out of a grouped channel list based on the
 * group key carried by the inbound event's `channel_custom` map.
 *
 * Intended to be paired with `QueryChannelsIdentifier.Grouped(groupKey)` — one handler instance
 * per grouped query. Classification is performed against `event.channelCustom` rather than
 * `channel.extraData` because the cached channel can lag the server while the event itself
 * carries the authoritative custom map.
 *
 * For channel-bearing events ([ChannelUpdatedEvent], [ChannelUpdatedByUserEvent]):
 * - If [groupKey] is in the resolved set and the channel is not currently in this list,
 *   [EventHandlingResult.Add].
 * - If [groupKey] is not in the resolved set and the channel IS currently in this list,
 *   [EventHandlingResult.Remove].
 * - Otherwise [EventHandlingResult.Skip].
 *
 * For watch-and-add events ([NotificationAddedToChannelEvent], [NotificationMessageNewEvent],
 * [ChannelVisibleEvent]): emits [EventHandlingResult.WatchAndAdd] when the event's
 * `channel_custom` says the channel belongs here, otherwise [EventHandlingResult.Skip].
 *
 * For [NewMessageEvent]: filtered up-front in [handleCidEvent] using `event.channelCustom` before
 * [DefaultChatEventHandler] gets a chance to `Add(cachedChannel)`. Off-group messages produce
 * [EventHandlingResult.Skip].
 *
 * Member events (`MemberAddedEvent`/`MemberUpdatedEvent`/`MemberRemovedEvent`) and other CID-only
 * events do not carry `channel_custom`, so they delegate to [DefaultChatEventHandler] unchanged.
 * This means a user added to a channel in another group can briefly appear in this list until the
 * follow-up `channel.updated` arrives and [routeByGroup] reclassifies it.
 */
internal class GroupAwareChatEventHandler(
    private val groupKey: String,
    private val resolver: ChannelGroupResolver,
    channels: StateFlow<Map<String, Channel>?>,
    clientState: ClientState,
) : DefaultChatEventHandler(channels, clientState) {

    override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is ChannelUpdatedEvent -> routeByGroup(event.channel, event.channelCustom)
            is ChannelUpdatedByUserEvent -> routeByGroup(event.channel, event.channelCustom)
            is NotificationAddedToChannelEvent -> watchAndAddIfBelongs(event.cid, event.channelCustom)
            is NotificationMessageNewEvent -> watchAndAddIfBelongs(event.cid, event.channelCustom)
            is ChannelVisibleEvent -> watchAndAddIfBelongs(event.cid, event.channelCustom)
            else -> super.handleChannelEvent(event, filter)
        }
    }

    override fun handleCidEvent(
        event: CidEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult {
        if (event is NewMessageEvent && !belongsHere(event.channelCustom)) {
            return EventHandlingResult.Skip
        }
        return super.handleCidEvent(event, filter, cachedChannel)
    }

    /**
     * Routes a channel-bearing event to Add / Remove / Skip based on the group resolved from the
     * event's `channelCustom` and whether the channel is currently in this grouped list.
     */
    private fun routeByGroup(channel: Channel, channelCustom: Map<String, Any>?): EventHandlingResult {
        val belongsHere = belongsHere(channelCustom)
        val isInList = channels.value?.containsKey(channel.cid) == true
        return when {
            belongsHere && !isInList -> EventHandlingResult.Add(channel)
            !belongsHere && isInList -> EventHandlingResult.Remove(channel.cid)
            else -> EventHandlingResult.Skip
        }
    }

    private fun watchAndAddIfBelongs(cid: String, channelCustom: Map<String, Any>?): EventHandlingResult =
        if (belongsHere(channelCustom)) {
            EventHandlingResult.WatchAndAdd(cid)
        } else {
            EventHandlingResult.Skip
        }

    private fun belongsHere(channelCustom: Map<String, Any>?): Boolean =
        resolver.resolve(channelCustom, groupKey).contains(groupKey)
}
