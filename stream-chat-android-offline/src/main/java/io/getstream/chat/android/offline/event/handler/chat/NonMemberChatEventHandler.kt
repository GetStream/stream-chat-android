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

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementation of [ChatEventHandler] that adds the channel if the current user is not a member and it will removed it otherwise.
 *
 * @param channels The list of visible channels.
 */
public class NonMemberChatEventHandler(
    private val channels: StateFlow<List<Channel>>,
) : BaseChatEventHandler() {

    /**
     *  Handles [NotificationAddedToChannelEvent] event. It removes the channel if it's present in the list.
     *
     * @param event Instance of [NotificationAddedToChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = removeIfChannelIsPresent(channels, event.channel)

    /**
     *  Handles [MemberAddedEvent] event. It removes the channel if it's present in the list
     *
     * @param event Instance of [NotificationAddedToChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     * @param cachedChannel optional [Channel] object cached in database.
     */
    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = removeIfChannelIsPresent(channels, cachedChannel)

    /** Handles [ChannelUpdatedByUserEvent] event. The event is skipped. */
    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /** Handles [ChannelUpdatedEvent] event. The event is skipped. */
    override fun handleChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult =
        EventHandlingResult.Skip

    /** Handles [NotificationMessageNewEvent] event. The event is skipped. */
    override fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /**
     * Handles [MemberRemovedEvent]. It adds the channel if it is absent.
     *
     * @param event Instance of [MemberRemovedEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     * @param cachedChannel optional [Channel] object cached in database.
     */
    override fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = addIfChannelIsAbsent(channels, cachedChannel)

    /**
     * Handles [NotificationRemovedFromChannelEvent]. It adds the channel if it is absent.
     *
     * @param event Instance of [NotificationRemovedFromChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsent(channels, event.channel)
}
