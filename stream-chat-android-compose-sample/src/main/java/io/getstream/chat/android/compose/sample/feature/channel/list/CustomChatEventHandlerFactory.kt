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

package io.getstream.chat.android.compose.sample.feature.channel.list

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.compose.sample.feature.channel.isDraft
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.state.event.handler.chat.DefaultChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import kotlinx.coroutines.flow.StateFlow

/**
 * Custom implementation of [ChatEventHandlerFactory] that provides the [CustomChatEventHandler]
 * which contains logic for handling "DRAFT" channels.
 */
class CustomChatEventHandlerFactory : ChatEventHandlerFactory() {
    override fun chatEventHandler(channels: StateFlow<Map<String, Channel>?>) = CustomChatEventHandler(channels)
}

/**
 * Custom implementation of [DefaultChatEventHandler] that contains logic for handling "DRAFT" channels.
 */
class CustomChatEventHandler(channels: StateFlow<Map<String, Channel>?>) :
    DefaultChatEventHandler(channels, ChatClient.instance().clientState) {

    override fun handleCidEvent(event: CidEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult {
        return when (event) {
            is MemberAddedEvent -> addIfCurrentUserAddedAndChannelNotDraft(cachedChannel, event.member)
            else -> super.handleCidEvent(event, filter, cachedChannel)
        }
    }

    override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is NotificationAddedToChannelEvent -> watchAndAddChannelIfNotDraft(event.channel)
            is ChannelUpdatedEvent -> handleChannelUpdate(event.channel)
            is ChannelUpdatedByUserEvent -> handleChannelUpdate(event.channel)
            else -> super.handleChannelEvent(event, filter)
        }
    }

    private fun addIfCurrentUserAddedAndChannelNotDraft(channel: Channel?, member: Member): EventHandlingResult {
        return if (channel == null || channel.isDraft) {
            EventHandlingResult.Skip
        } else {
            addIfCurrentUserJoinedChannel(channel, member)
        }
    }

    private fun watchAndAddChannelIfNotDraft(channel: Channel): EventHandlingResult {
        return if (channel.isDraft) {
            EventHandlingResult.Skip
        } else {
            EventHandlingResult.WatchAndAdd(channel.cid)
        }
    }

    private fun addChannelIfNotDraft(channel: Channel): EventHandlingResult {
        return if (channel.isDraft) {
            EventHandlingResult.Skip
        } else {
            addIfChannelIsAbsent(channel)
        }
    }

    private fun handleChannelUpdate(channel: Channel): EventHandlingResult {
        val isCurrentUserMember = channel.members.any { it.getUserId() == clientState.user.value?.id }
        return if (isCurrentUserMember) {
            addChannelIfNotDraft(channel)
        } else {
            removeIfChannelExists(channel.cid)
        }
    }
}
