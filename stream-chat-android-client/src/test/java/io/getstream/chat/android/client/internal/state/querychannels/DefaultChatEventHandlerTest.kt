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

package io.getstream.chat.android.client.internal.state.querychannels

import io.getstream.chat.android.client.api.event.DefaultChatEventHandler
import io.getstream.chat.android.client.api.event.EventHandlingResult
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannelHiddenEvent
import io.getstream.chat.android.client.test.randomChannelVisibleEvent
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMemberRemovedEvent
import io.getstream.chat.android.client.test.randomMemberUpdatedEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.client.test.randomNotificationRemovedFromChannelEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class DefaultChatEventHandlerTest {

    @Test
    fun `Given the channel is present When received MemberRemovedEvent concerning current user Should remove the channel`() {
        val channel = randomChannel()
        val currentUser = randomUser()
        val member = randomMember(user = currentUser)
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(currentUser)
        }
        val eventHandler =
            DefaultChatEventHandler(MutableStateFlow(mapOf(channel.cid to channel)), clientState)
        val event = randomMemberRemovedEvent(
            cid = channel.cid,
            member = member,
        )

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Remove(channel.cid)
    }

    @Test
    fun `Given the channel is present When received MemberRemovedEvent concerning different member Should skip the update`() {
        val channel = randomChannel()
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(randomUser())
        }
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(mapOf(channel.cid to channel)), clientState)
        val event = randomMemberRemovedEvent(cid = channel.cid)

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is not present When received MemberRemovedEvent Should skip the update`() {
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(null)
        }
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), clientState)
        val event = randomMemberRemovedEvent()

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is not present When received MemberAddedEvent Should skip the update`() {
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(null)
        }
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), clientState)
        val event = randomMemberAddedEvent()

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is not present When received MemberAddedEvent concerning different member Should skip the update`() {
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(randomUser())
        }
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), clientState)
        val event = randomMemberAddedEvent()

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is not present When received MemberAddedEvent concerning current user Should add the channel`() {
        val channel = randomChannel()
        val currentUser = randomUser()
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(currentUser)
        }
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), clientState)
        val event = randomMemberAddedEvent(cid = channel.cid, member = randomMember(user = currentUser))

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @Test
    fun `When received NotificationMessageNewEvent Should watch and add the channel`() {
        val cid = randomCID()
        val clientState = mock<ClientState>()
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), clientState)
        val event = randomNotificationMessageNewEvent(cid = cid)

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.WatchAndAdd(cid)
    }

    @Test
    fun `When received NotificationAddedToChannelEvent Should watch and add the channel`() {
        val cid = randomCID()
        val clientState = mock<ClientState>()
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), clientState)
        val event = randomNotificationAddedToChannelEvent(cid = cid)

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.WatchAndAdd(cid)
    }

    @Test
    fun `Given the channel is present When received NotificationRemovedFromChannelEvent concerning current user Should remove the channel`() {
        val channel = randomChannel()
        val currentUser = randomUser()
        val member = randomMember(user = currentUser)
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(currentUser)
        }
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(mapOf(channel.cid to channel)), clientState)
        val event = randomNotificationRemovedFromChannelEvent(channel.cid, member = member)

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Remove(channel.cid)
    }

    @Test
    fun `Given the channel is present When received NotificationRemovedFromChannelEvent concerning different member Should skip the update`() {
        val channel = randomChannel()
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(randomUser())
        }
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(mapOf(channel.cid to channel)), clientState)
        val event = randomNotificationRemovedFromChannelEvent(channel.cid)

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is not present When received NotificationRemovedFromChannelEvent Should skip the update`() {
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(null)
        }
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), clientState)
        val event = randomNotificationRemovedFromChannelEvent()

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given membership updated for current user When received MemberUpdatedEvent Should add the channel`() {
        val currentUser = randomUser()
        val member = randomMember(user = currentUser)
        val channel = randomChannel(membership = member, hidden = false)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), mock())
        val event = randomMemberUpdatedEvent(cid = channel.cid, member = member)

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @Test
    fun `Given membership updated for current user and the channel is hidden When received MemberUpdatedEvent Should skip`() {
        val currentUser = randomUser()
        val member = randomMember(user = currentUser)
        val channel = randomChannel(membership = member, hidden = true)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), mock())
        val event = randomMemberUpdatedEvent(cid = channel.cid, member = member)

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given no cached channel When received MemberUpdatedEvent Should skip the update`() {
        val currentUser = randomUser()
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), mock())
        val event = randomMemberUpdatedEvent(member = randomMember(user = currentUser))

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given membership updated for another user When received MemberUpdatedEvent Should skip the update`() {
        val channel = randomChannel(membership = randomMember(user = randomUser()), hidden = false)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), mock())
        val event = randomMemberUpdatedEvent(cid = channel.cid, member = randomMember(user = randomUser()))

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given membership updated and hidden is unknown When received MemberUpdatedEvent Should add the channel`() {
        val currentUser = randomUser()
        val member = randomMember(user = currentUser)
        val channel = randomChannel(membership = member, hidden = null)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), mock())
        val event = randomMemberUpdatedEvent(cid = channel.cid, member = member)

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @Test
    fun `Given the channel is not present When received NewMessageEvent with not system message Should add the channel`() {
        val channel = randomChannel()
        val clientState = mock<ClientState>()
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), mock())
        val event = randomNewMessageEvent(cid = channel.cid, message = randomMessage(type = "regular"))

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @Test
    fun `Given the channel is not present When received NewMessageEvent with system message Should skip the update`() {
        val channel = randomChannel()
        val clientState = mock<ClientState>()
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyMap()), mock())
        val event = randomNewMessageEvent(cid = channel.cid, message = randomMessage(type = "system"))

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is present When received NewMessageEvent with not system message Should skip the update`() {
        val channel = randomChannel()
        val clientState = mock<ClientState>()
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(mapOf(channel.cid to channel)), mock())
        val event = randomNewMessageEvent(cid = channel.cid, message = randomMessage(type = "regular"))

        val result = eventHandler.handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given a list of visible channels When received ChannelHiddenEvent Should remove the channel`() {
        val event = randomChannelHiddenEvent()

        val result = eventHandler().handleChatEvent(event = event, filter = Filters.neutral(), cachedChannel = null)

        result `should be equal to` EventHandlingResult.Remove(event.cid)
    }

    @Test
    fun `Given a list of visible channels When received ChannelVisibleEvent Should watch and add the channel`() {
        val event = randomChannelVisibleEvent()

        val result = eventHandler().handleChatEvent(
            event = event,
            filter = Filters.eq("hidden", false),
            cachedChannel = null,
        )

        result `should be equal to` EventHandlingResult.WatchAndAdd(event.cid)
    }

    @Test
    fun `Given a list of hidden channels When received ChannelHiddenEvent Should watch and add the channel`() {
        val event = randomChannelHiddenEvent()

        val result = eventHandler().handleChatEvent(event = event, filter = hiddenChannelsFilter, cachedChannel = null)

        result `should be equal to` EventHandlingResult.WatchAndAdd(event.cid)
    }

    @Test
    fun `Given a list of hidden channels When received ChannelVisibleEvent Should remove the channel`() {
        val event = randomChannelVisibleEvent()

        val result = eventHandler().handleChatEvent(event = event, filter = hiddenChannelsFilter, cachedChannel = null)

        result `should be equal to` EventHandlingResult.Remove(event.cid)
    }

    @Test
    fun `Given a listed hidden channel When received NewMessageEvent that shows it again Should remove the channel`() {
        val channel = randomChannel(hidden = true, blocked = false)
        val event = randomNewMessageEvent(cid = channel.cid, message = randomMessage(type = "regular"))

        val result = eventHandler(channel).handleChatEvent(event, hiddenChannelsFilter, cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Remove(channel.cid)
    }

    @Test
    fun `Given a listed hidden channel When received NewMessageEvent that keeps it hidden Should skip the update`() {
        val channel = randomChannel(hidden = true, blocked = false)
        val event = randomNewMessageEvent(cid = channel.cid, message = randomMessage(type = "regular", shadowed = true))

        val result = eventHandler(channel).handleChatEvent(event, hiddenChannelsFilter, cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given a listed hidden channel When received NewMessageEvent from a blocked user Should skip the update`() {
        val channel = randomChannel(hidden = true, blocked = false)
        val author = randomUser()
        val event = randomNewMessageEvent(cid = channel.cid, message = randomMessage(type = "regular", user = author))

        val result = eventHandler(channel, blockedUserIds = listOf(author.id))
            .handleChatEvent(event, hiddenChannelsFilter, cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given a list of hidden channels When received NewMessageEvent for an absent channel Should skip the update`() {
        val channel = randomChannel(hidden = false)
        val event = randomNewMessageEvent(cid = channel.cid, message = randomMessage(type = "regular"))

        val result = eventHandler().handleChatEvent(event, hiddenChannelsFilter, cachedChannel = channel)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given a listed hidden channel When received NotificationMessageNewEvent that shows it again Should remove the channel`() {
        val channel = randomChannel(hidden = true, blocked = false)
        val event = randomNotificationMessageNewEvent(
            cid = channel.cid,
            channel = channel,
            message = randomMessage(type = "regular"),
        )

        val result = eventHandler(channel).handleChatEvent(event, hiddenChannelsFilter, cachedChannel = null)

        result `should be equal to` EventHandlingResult.Remove(channel.cid)
    }

    @Test
    fun `Given a listed hidden blocked channel When received NotificationMessageNewEvent Should skip the update`() {
        val channel = randomChannel(hidden = true, blocked = true)
        val event = randomNotificationMessageNewEvent(
            cid = channel.cid,
            channel = channel.copy(blocked = null),
            message = randomMessage(type = "regular"),
        )

        val result = eventHandler(channel).handleChatEvent(event, hiddenChannelsFilter, cachedChannel = null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given a list of hidden channels When the current user is added to a channel Should skip the update`() {
        val currentUser = randomUser()
        val channel = randomChannel()
        val member = randomMember(user = currentUser)
        val handler = eventHandler(currentUser = currentUser)

        handler.handleChatEvent(
            event = randomMemberAddedEvent(cid = channel.cid, member = member),
            filter = hiddenChannelsFilter,
            cachedChannel = channel,
        ) `should be equal to` EventHandlingResult.Skip
        handler.handleChatEvent(
            event = randomNotificationAddedToChannelEvent(cid = channel.cid, channel = channel, member = member),
            filter = hiddenChannelsFilter,
            cachedChannel = null,
        ) `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given a list of hidden channels When membership updated Should add only a hidden channel`() {
        val member = randomMember(user = randomUser())
        val hiddenChannel = randomChannel(membership = member, hidden = true)
        val visibleChannel = randomChannel(membership = member, hidden = false)

        eventHandler().handleChatEvent(
            event = randomMemberUpdatedEvent(cid = hiddenChannel.cid, member = member),
            filter = hiddenChannelsFilter,
            cachedChannel = hiddenChannel,
        ) `should be equal to` EventHandlingResult.Add(hiddenChannel)
        eventHandler().handleChatEvent(
            event = randomMemberUpdatedEvent(cid = visibleChannel.cid, member = member),
            filter = hiddenChannelsFilter,
            cachedChannel = visibleChannel,
        ) `should be equal to` EventHandlingResult.Skip
    }

    private val hiddenChannelsFilter = Filters.and(Filters.eq("type", "messaging"), Filters.eq("hidden", true))

    private fun eventHandler(
        vararg listed: Channel,
        currentUser: User = randomUser(),
        blockedUserIds: List<String> = emptyList(),
    ): DefaultChatEventHandler {
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(currentUser)
        }
        return DefaultChatEventHandler(
            MutableStateFlow(listed.associateBy(Channel::cid)),
            clientState,
        ) { blockedUserIds }
    }
}
