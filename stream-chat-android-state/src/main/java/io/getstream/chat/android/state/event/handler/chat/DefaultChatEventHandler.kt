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

package io.getstream.chat.android.state.event.handler.chat

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.extensions.internal.updateMember
import io.getstream.chat.android.client.extensions.internal.updateMembership
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of [ChatEventHandler] which is based on the current user membership.
 *
 * @param channels The map of visible channels.
 * @param clientState The client state used to obtain current user.
 */
public open class DefaultChatEventHandler(
    protected val channels: StateFlow<Map<String, Channel>?>,
    protected val clientState: ClientState,
) : BaseChatEventHandler() {

    /**
     * Handles additional events:
     * - [NewMessageEvent] - adds the channel to the set if its not a system message.
     * - [MemberRemovedEvent] - removes the channel from the set if a current user left.
     * - [MemberAddedEvent] - adds the channel to the set if a current user was added.
     *
     * @see [BaseChatEventHandler.handleCidEvent]
     *
     * @param event [ChatEvent] that may contain updates for the set of channels.
     * @param filter [FilterObject] associated with the set of channels. Can be used to define the result of handling.
     * @param cachedChannel Optional cached [Channel] object if exists.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    override fun handleCidEvent(event: CidEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult = when (event) {
        is NewMessageEvent -> handleNewMessageEvent(event, cachedChannel)
        is MemberRemovedEvent -> removeIfCurrentUserLeftChannel(event.cid, event.member)
        is MemberAddedEvent -> addIfCurrentUserJoinedChannel(cachedChannel, event.member)
        is MemberUpdatedEvent -> addIfMembershipUpdated(cachedChannel, event.member)
        else -> super.handleCidEvent(event, filter, cachedChannel)
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
    override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult = when (event) {
        is NotificationMessageNewEvent -> EventHandlingResult.WatchAndAdd(event.cid)
        is NotificationAddedToChannelEvent -> EventHandlingResult.WatchAndAdd(event.cid)
        is NotificationRemovedFromChannelEvent -> removeIfCurrentUserLeftChannel(event.cid, event.member)
        else -> super.handleChannelEvent(event, filter)
    }

    /**
     * Checks if the message is a system message.
     * If yes it skips the event. Otherwise, it adds the channel cached channel exists and was not added yet.
     *
     * @param event [NewMessageEvent] that may contain updates for the set of channels.
     * @param cachedChannel Optional cached [Channel] object if exists.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    private fun handleNewMessageEvent(event: NewMessageEvent, cachedChannel: Channel?): EventHandlingResult = if (event.message.type == SYSTEM_MESSAGE) {
        EventHandlingResult.Skip
    } else {
        addIfChannelIsAbsent(cachedChannel)
    }

    private fun addIfMembershipUpdated(channel: Channel?, member: Member): EventHandlingResult = if (channel?.membership?.getUserId() == member.getUserId()) {
        EventHandlingResult.Add(
            channel.updateMembership(member)
                .updateMember(member),
        )
    } else {
        EventHandlingResult.Skip
    }

    /**
     * Checks if the current user has left the channel and the channel is visible.
     * If yes then it removes it. Otherwise, it simply skips the event.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param member The member who left the channel.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    private fun removeIfCurrentUserLeftChannel(cid: String, member: Member): EventHandlingResult = if (member.getUserId() != clientState.user.value?.id) {
        EventHandlingResult.Skip
    } else {
        removeIfChannelExists(cid)
    }

    /**
     * Checks if the channel with given id is visible.
     * If yes then it removes it. Otherwise, it simply skips the event.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     *
     * @return [EventHandlingResult] Result of handling.
     */
    protected fun removeIfChannelExists(cid: String): EventHandlingResult {
        val channelsMap = channels.value

        return when {
            channelsMap == null -> EventHandlingResult.Skip
            channelsMap.containsKey(cid) -> EventHandlingResult.Remove(cid)
            else -> EventHandlingResult.Skip
        }
    }

    /**
     * Checks if the current user joined the channel and the channel is not visible yet.
     * If yes then it adds it. Otherwise, it simply skips the event.
     *
     * @param channel Optional cached channel object.
     * @param member The member who joined the channel.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    protected fun addIfCurrentUserJoinedChannel(channel: Channel?, member: Member): EventHandlingResult = if (clientState.user.value?.id == member.getUserId()) {
        addIfChannelIsAbsent(channel)
    } else {
        EventHandlingResult.Skip
    }

    /**
     * Checks if the cached channel exists and is not visible yet.
     * If yes then it adds it. Otherwise, it simply skips the event.
     *
     * @param channel Optional cached channel object.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    protected fun addIfChannelIsAbsent(channel: Channel?): EventHandlingResult {
        val channelsMap = channels.value

        return when {
            channelsMap == null || channel == null -> EventHandlingResult.Skip
            channelsMap.containsKey(channel.cid) -> EventHandlingResult.Skip
            else -> EventHandlingResult.Add(channel)
        }
    }

    private companion object {
        private const val SYSTEM_MESSAGE = "system"
    }
}
