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

package io.getstream.chat.ui.sample.feature.channel.list

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.event.ChatEventHandlerFactory
import io.getstream.chat.android.client.api.event.DefaultChatEventHandler
import io.getstream.chat.android.client.api.event.EventHandlingResult
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.ui.sample.common.isDraft
import kotlinx.coroutines.flow.StateFlow

class CustomChatEventHandlerFactory : ChatEventHandlerFactory() {
    override fun chatEventHandler(channels: StateFlow<Map<String, Channel>?>) = CustomChatEventHandler(channels)
}

class CustomChatEventHandler(channels: StateFlow<Map<String, Channel>?>) :
    DefaultChatEventHandler(channels, ChatClient.instance().clientState) {

    override fun handleCidEvent(event: CidEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult {
        return when (event) {
            is MemberAddedEvent -> addIfCurrentUserJoinedAndChannelIsNotDraft(cachedChannel, event.member)
            else -> super.handleCidEvent(event, filter, cachedChannel)
        }
    }

    override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is NotificationAddedToChannelEvent -> watchIfChannelIsNotDraft(event.channel)
            is ChannelUpdatedEvent -> handleChannelUpdate(event.channel)
            is ChannelUpdatedByUserEvent -> handleChannelUpdate(event.channel)
            else -> super.handleChannelEvent(event, filter)
        }
    }

    private fun addIfCurrentUserJoinedAndChannelIsNotDraft(channel: Channel?, member: Member): EventHandlingResult {
        return if (isChannelNullOrDraft(channel)) {
            EventHandlingResult.Skip
        } else {
            addIfCurrentUserJoinedChannel(channel, member)
        }
    }

    private fun watchIfChannelIsNotDraft(channel: Channel): EventHandlingResult {
        return if (isChannelNullOrDraft(channel)) {
            EventHandlingResult.Skip
        } else {
            EventHandlingResult.WatchAndAdd(channel.cid)
        }
    }

    private fun addIfChannelIsNotDraft(channel: Channel?): EventHandlingResult {
        return if (isChannelNullOrDraft(channel)) {
            EventHandlingResult.Skip
        } else {
            addIfChannelIsAbsent(channel)
        }
    }

    private fun isChannelNullOrDraft(channel: Channel?) = channel == null || channel.isDraft

    private fun handleChannelUpdate(channel: Channel): EventHandlingResult {
        val hasMember = channel.members.any { member ->
            ChatClient.instance().getCurrentUser()?.id == member.getUserId()
        }

        return if (hasMember) {
            addIfChannelIsNotDraft(channel)
        } else {
            removeIfChannelExists(channel.cid)
        }
    }
}
