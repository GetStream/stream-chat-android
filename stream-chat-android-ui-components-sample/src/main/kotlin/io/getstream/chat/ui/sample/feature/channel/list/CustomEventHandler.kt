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

package io.getstream.chat.ui.sample.feature.channel.list

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.offline.event.handler.chat.BaseChatEventHandler
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.ui.sample.common.isDraft
import kotlinx.coroutines.flow.StateFlow

class CustomChatEventHandlerFactory : ChatEventHandlerFactory() {
    override fun chatEventHandler(channels: StateFlow<List<Channel>>) = CustomChatEventHandler(channels)
}

class CustomChatEventHandler(private val channels: StateFlow<List<Channel>>) : BaseChatEventHandler() {

    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsentAndNotDraft(channels, event.channel)

    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = addIfChannelIsAbsentAndNotDraft(channels, cachedChannel)

    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleChannelUpdate(event.channel)

    override fun handleChannelUpdatedEvent(
        event: ChannelUpdatedEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleChannelUpdate(event.channel)

    override fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsentAndNotDraft(channels, event.channel)

    override fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = handleMemberRemoval(channels, cachedChannel, event.member)

    override fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleMemberRemoval(channels, event.channel, event.member)

    private fun addIfChannelIsAbsentAndNotDraft(
        channels: StateFlow<List<Channel>>,
        channel: Channel?,
    ): EventHandlingResult {
        return if (channel == null || channel.isDraft || channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Skip
        } else {
            EventHandlingResult.Add(channel)
        }
    }

    private fun handleChannelUpdate(channel: Channel): EventHandlingResult {
        val hasMember = channel.members.any { member ->
            ChatClient.instance().getCurrentUser()?.id == member.getUserId()
        }

        return if (hasMember) {
            addIfChannelIsAbsentAndNotDraft(channels, channel)
        } else {
            removeIfChannelIsPresent(channels, channel)
        }
    }

    private fun handleMemberRemoval(
        channels: StateFlow<List<Channel>>,
        cachedChannel: Channel?,
        member: Member,
    ): EventHandlingResult {
        val currentUserId = ChatClient.instance().getCurrentUser()?.id
        val removedMemberId = member.getUserId()

        return if (currentUserId == removedMemberId) {
            removeIfChannelIsPresent(channels, cachedChannel)
        } else {
            EventHandlingResult.Skip
        }
    }

    private fun removeIfChannelIsPresent(channels: StateFlow<List<Channel>>, channel: Channel?): EventHandlingResult {
        return if (channel != null && channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Remove(channel.cid)
        } else {
            EventHandlingResult.Skip
        }
    }
}
