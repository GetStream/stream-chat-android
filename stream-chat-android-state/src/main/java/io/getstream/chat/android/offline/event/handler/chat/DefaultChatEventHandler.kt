/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.event.handler.chat

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.setup.state.ClientState
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of [ChatEventHandler] which is based on the current user membership.
 *
 * @param channels The list of visible channels.
 * @param clientState The client state used to obtain current user.
 */
public open class DefaultChatEventHandler(
    private val channels: StateFlow<List<Channel>?>,
    private val clientState: ClientState = ChatClient.instance().clientState,
) : BaseChatEventHandler() {

    /**
     * Handles additional events:
     * - [MemberRemovedEvent] - removes the channel from the set if a current user left
     * - [MemberAddedEvent] - adds the channel to the set if a current user was added.
     *
     * @see [BaseChatEventHandler.handleCidEvent]
     *
     * @param event [ChatEvent] that may contain updates for the set of channels.
     * @param filter [FilterObject] associated with the set of channels. Can be used to define the result of handling.
     * @param cachedChannel optional cached [Channel] object if exists.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    override fun handleCidEvent(event: CidEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult {
        return when (event) {
            is MemberRemovedEvent -> removeIfCurrentUserLeftChannel(channels, event.cid, event.member)
            is MemberAddedEvent -> addIfCurrentUserJoinedChannel(channels, cachedChannel, event.member)
            else -> super.handleCidEvent(event, filter, cachedChannel)
        }
    }

    /**
     * Handles additional events:
     * - [NotificationMessageNewEvent] - calls watch and adds the channel to the set.
     * - [NotificationRemovedFromChannelEvent] - removes the channel from the set if a current user left.
     * - [NotificationAddedToChannelEvent] - calls watch and adds the channel to the set if a current user was added.
     *
     * @param event [ChatEvent] that may contain updates for the set of channels.
     * @param filter [FilterObject] associated with the set of channels. Can be used to define the result of handling.

     * @return [EventHandlingResult] Result of handling.
     */
    override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is NotificationMessageNewEvent -> EventHandlingResult.WatchAndAdd(event.cid)
            is NotificationAddedToChannelEvent -> EventHandlingResult.WatchAndAdd(event.cid)
            is NotificationRemovedFromChannelEvent -> removeIfCurrentUserLeftChannel(
                channels,
                event.cid,
                event.member,
            )
            else -> super.handleChannelEvent(event, filter)
        }
    }

    /**
     * Checks if the current user has left the channel and the channel is in the collection.
     * If yes then it removes it. Otherwise, it simply skips the event.
     */
    private fun removeIfCurrentUserLeftChannel(
        channels: StateFlow<List<Channel>?>,
        channelCid: String,
        member: Member,
    ): EventHandlingResult {
        val channelsList = channels.value
        val isCurrentUserRelated = member.getUserId() == clientState.user.value?.id

        return when {
            channelsList == null || !isCurrentUserRelated -> EventHandlingResult.Skip
            channelsList.any { it.cid == channelCid } -> EventHandlingResult.Remove(channelCid)
            else -> EventHandlingResult.Skip
        }
    }

    /**
     * Checks if the current user joined the channel and the channel wasn't added yet.
     * If yes then it adds it. Otherwise, it simply skips the event.
     */
    private fun addIfCurrentUserJoinedChannel(
        channels: StateFlow<List<Channel>?>,
        channel: Channel?,
        member: Member,
    ): EventHandlingResult {
        return if (clientState.user.value?.id == member.getUserId()) {
            addIfChannelIsAbsent(channels, channel)
        } else {
            EventHandlingResult.Skip
        }
    }

    /**
     * Checks if the channel is not present in the collection yet.
     * If yes then it adds it. Otherwise, it simply skips the event.
     */
    private fun addIfChannelIsAbsent(channels: StateFlow<List<Channel>?>, channel: Channel?): EventHandlingResult {
        val channelsList = channels.value ?: return EventHandlingResult.Skip

        return if (channel == null || channelsList.any { it.cid == channel.cid }) {
            EventHandlingResult.Skip
        } else {
            EventHandlingResult.Add(channel)
        }
    }

    /**
     * Handles [MemberRemovedEvent]. It removes the channel if it's present in the list.
     *
     * @param event Instance of [MemberRemovedEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     * @param cachedChannel optional [Channel] object cached in database
     */
    override fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = removeIfCurrentUserLeftChannel(channels, event.cid, event.member)

    /**
     *  Handles [MemberAddedEvent] event. It adds the channel if it is absent.
     *
     * @param event Instance of [NotificationAddedToChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     * @param cachedChannel optional [Channel] object cached in database.
     */
    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = addIfCurrentUserJoinedChannel(channels, cachedChannel, event.member)

    /**
     * Handles [NotificationRemovedFromChannelEvent]. It removes the channel if it's present in the list.
     *
     * @param event Instance of [NotificationRemovedFromChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = removeIfCurrentUserLeftChannel(channels, event.cid, event.member)

    /**
     *  Handles [NotificationAddedToChannelEvent] event. It adds the channel if it is absent.
     *
     * @param event Instance of [NotificationAddedToChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.WatchAndAdd(event.cid)

    /** Handles [ChannelUpdatedByUserEvent] event. The event is skipped. */
    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /** Handles [ChannelUpdatedEvent] event. The event is skipped. */
    override fun handleChannelUpdatedEvent(
        event: ChannelUpdatedEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip
}
