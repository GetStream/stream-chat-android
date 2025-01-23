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

package io.getstream.chat.android.state.querychannels

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMemberRemovedEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.client.test.randomNotificationRemovedFromChannelEvent
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.event.handler.chat.DefaultChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
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
}
