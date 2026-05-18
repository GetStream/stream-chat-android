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

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannelDeletedEvent
import io.getstream.chat.android.client.test.randomChannelUpdatedEvent
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMemberRemovedEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class GroupAwareChatEventHandlerTest {

    private val defaultResolver = DefaultChannelGroupResolver()

    @Test
    fun `Given channel belongs to this group and is not cached When ChannelUpdatedEvent arrives Should add`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomChannelUpdatedEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "vip"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given channel belongs to this group and is already cached When ChannelUpdatedEvent arrives Should skip`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "vip", cachedChannels = mapOf(channel.cid to channel))
        val event = randomChannelUpdatedEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "vip"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given channel moved to another group and is currently cached When ChannelUpdatedEvent arrives Should remove`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "vip", cachedChannels = mapOf(channel.cid to channel))
        val event = randomChannelUpdatedEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "other"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given channel belongs to another group and is not cached When ChannelUpdatedEvent arrives Should skip`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomChannelUpdatedEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "other"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given handler is for the all group When ChannelUpdatedEvent arrives Should always add`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "all", cachedChannels = emptyMap())
        val event = randomChannelUpdatedEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "vip"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given channel does not belong here When NotificationAddedToChannelEvent arrives Should skip`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNotificationAddedToChannelEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "other"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given channel belongs here When NotificationAddedToChannelEvent arrives Should watch and add`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNotificationAddedToChannelEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "vip"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.WatchAndAdd(channel.cid), result)
    }

    @Test
    fun `Given channel does not belong here When NotificationMessageNewEvent arrives Should skip`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNotificationMessageNewEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "other"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given NewMessageEvent for another group Should skip without consulting super`() {
        val channel = randomChannel()
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
        )
        val event = randomNewMessageEvent(
            cid = channel.cid,
            channelCustom = mapOf("group" to "other"),
        )

        // cachedChannel is provided; if super were consulted it could return Add. We assert Skip.
        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = channel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given NewMessageEvent for this group with cached channel absent from list Should add`() {
        val channel = randomChannel()
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
        )
        val event = randomNewMessageEvent(
            cid = channel.cid,
            message = randomMessage(type = "regular"),
            channelCustom = mapOf("group" to "vip"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = channel)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given NewMessageEvent for this group but no cached channel Should skip`() {
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNewMessageEvent(
            channelCustom = mapOf("group" to "vip"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given system NewMessageEvent for this group Should skip via super`() {
        val channel = randomChannel()
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNewMessageEvent(
            cid = channel.cid,
            message = randomMessage(type = "system"),
            channelCustom = mapOf("group" to "vip"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = channel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given current user joined with a cached channel When MemberAddedEvent arrives Should add via super`() {
        // Member events do not carry channel_custom and the handler trusts super (no group filter).
        val currentUser = randomUser()
        val channel = randomChannel()
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
            currentUser = currentUser,
        )
        val event = randomMemberAddedEvent(
            cid = channel.cid,
            member = randomMember(user = currentUser),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = channel)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given current user left a cached channel When MemberRemovedEvent arrives Should remove regardless of group`() {
        val currentUser = randomUser()
        val channel = randomChannel()
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = randomMemberRemovedEvent(
            cid = channel.cid,
            member = randomMember(user = currentUser),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a cached channel When ChannelDeletedEvent arrives Should remove regardless of group`() {
        val channel = randomChannel()
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
        )
        val event = randomChannelDeletedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a custom resolver that reads a different field When ChannelUpdatedEvent arrives Should use custom field`() {
        val channel = randomChannel()
        val customResolver = ChannelGroupResolver { channelCustom, _, _ ->
            setOfNotNull(channelCustom?.get("tier") as? String)
        }
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
            resolver = customResolver,
        )
        val event = randomChannelUpdatedEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("tier" to "vip"),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given custom resolver Should receive the handler's groupKey as currentGroup`() {
        val channel = randomChannel()
        var capturedGroup: String? = null
        val capturingResolver = ChannelGroupResolver { _, _, currentGroup ->
            capturedGroup = currentGroup
            setOf("vip")
        }
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
            resolver = capturingResolver,
        )
        val event = randomChannelUpdatedEvent(
            cid = channel.cid,
            channel = channel,
            channelCustom = mapOf("group" to "vip"),
        )

        handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals("vip", capturedGroup)
    }

    private fun handlerFor(
        groupKey: String,
        cachedChannels: Map<String, Channel>,
        resolver: ChannelGroupResolver = defaultResolver,
        currentUser: User? = null,
    ): GroupAwareChatEventHandler {
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(currentUser)
        }
        return GroupAwareChatEventHandler(
            groupKey = groupKey,
            resolver = resolver,
            channels = MutableStateFlow(cachedChannels),
            clientState = clientState,
        )
    }
}
