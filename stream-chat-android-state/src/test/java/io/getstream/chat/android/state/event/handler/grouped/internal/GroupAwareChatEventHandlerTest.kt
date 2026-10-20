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

package io.getstream.chat.android.state.event.handler.grouped.internal

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannelDeletedEvent
import io.getstream.chat.android.client.test.randomChannelUpdatedEvent
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMemberRemovedEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.client.test.randomNotificationChannelPartialUpdatedEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.concurrent.TimeUnit

@Suppress("LargeClass")
internal class GroupAwareChatEventHandlerTest {

    private val defaultResolver = DefaultChannelGroupResolver()
    private val now = Date(1_800_000_000_000)
    private val cutoff14Days = Filters.greaterThanEquals("query_timestamp", "now-14d")

    @Test
    fun `Given channel belongs to this group and is not cached When ChannelUpdatedEvent arrives Should add`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given channel belongs to this group and is already cached When ChannelUpdatedEvent arrives Should skip`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given channel moved to another group and is currently cached When ChannelUpdatedEvent arrives Should remove`() {
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = mapOf(channel.cid to channel))
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given channel belongs to another group and is not cached When ChannelUpdatedEvent arrives Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given handler is for the all group When ChannelUpdatedEvent arrives Should always add`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "all",
            cachedChannels = emptyMap(),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given channel does not belong here When NotificationAddedToChannelEvent arrives Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNotificationAddedToChannelEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given channel belongs here When NotificationAddedToChannelEvent arrives Should watch and add`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNotificationAddedToChannelEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.WatchAndAdd(channel.cid), result)
    }

    @Test
    fun `Given channel does not belong here When NotificationMessageNewEvent arrives Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNotificationMessageNewEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given current user joined with matching cached channel When MemberAddedEvent arrives Should add`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
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
    fun `Given current user joined with non-matching cached channel When MemberAddedEvent arrives Should skip`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "other"))
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

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given current user left a cached channel When MemberRemovedEvent arrives Should remove regardless of group`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
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
    fun `Given cachedChannel has no membership for current user When ChannelUpdatedEvent moves channel into this group Should skip`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "old"))
        val cachedChannel = channel.copy(membership = null)
        val handler = handlerFor(
            groupKey = "old",
            cachedChannels = emptyMap(),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given current user lost membership while channel still resolves here When ChannelUpdatedEvent arrives Should remove`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "old"))
        val cachedChannel = channel.copy(membership = null)
        val handler = handlerFor(
            groupKey = "old",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given cached channel is hidden When ChannelUpdatedEvent moves channel into this group Should skip`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = true)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given cached channel is hidden and is in the list When ChannelUpdatedEvent arrives Should remove`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = true)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a channel in the list When ChannelUpdatedEvent disables it Should remove`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip", "disabled" to true))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a disabled channel not in the list When ChannelUpdatedEvent enables it Should add`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip", "disabled" to false))
        val cachedChannel = channel.copy(
            membership = randomMember(user = currentUser),
            hidden = false,
            extraData = mapOf("group" to "vip", "disabled" to true),
        )
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given a disabled channel When NotificationAddedToChannelEvent arrives Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip", "disabled" to true))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNotificationAddedToChannelEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given no connected user When ChannelUpdatedEvent arrives Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
            currentUser = null,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given current user has a connection but no cachedChannel When ChannelUpdatedEvent arrives Should skip`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "old"))
        val handler = handlerFor(
            groupKey = "old",
            cachedChannels = emptyMap(),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a cached channel When ChannelDeletedEvent arrives Should remove regardless of group`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
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
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("tier" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val customResolver = ChannelGroupResolver { ch ->
            setOfNotNull(ch.extraData["tier"] as? String)
        }
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = emptyMap(),
            resolver = customResolver,
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given a filtered group and a channel past the cutoff in the list When ChannelUpdatedEvent arrives Should remove`() {
        val currentUser = randomUser()
        val channel = randomChannel(
            extraData = mapOf("group" to "expired"),
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(15),
        )
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "expired",
            cachedChannels = mapOf(channel.cid to cachedChannel),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a channel kept within the cutoff by touched_at When ChannelUpdatedEvent arrives Should keep it`() {
        val currentUser = randomUser()
        val channel = randomChannel(
            extraData = mapOf("group" to "expired", "touched_at" to daysAgo(1)),
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(20),
        )
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "expired",
            cachedChannels = mapOf(channel.cid to cachedChannel),
            currentUser = currentUser,
        )
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a filtered group and a channel within the cutoff When ChannelUpdatedEvent arrives Should add`() {
        val currentUser = randomUser()
        val channel = randomChannel(
            extraData = mapOf("group" to "expired"),
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(3),
        )
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(groupKey = "expired", cachedChannels = emptyMap(), currentUser = currentUser)
        val event = randomChannelUpdatedEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Add(channel), result)
    }

    @Test
    fun `Given the all group filter excludes a group When NotificationMessageNewEvent arrives for it Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "bee"), lastMessageAt = daysAgo(0))
        val handler = handlerFor(groupKey = "all", cachedChannels = emptyMap())
        val event = randomNotificationMessageNewEvent(cid = channel.cid, channel = channel)

        val result = handler.handleChatEvent(event, Filters.ne("group", "bee"), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a cached channel past the cutoff When a new message arrives Should add it back`() {
        val cachedChannel = randomChannel(
            extraData = mapOf("group" to "expired"),
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(20),
        )
        val handler = handlerFor(groupKey = "expired", cachedChannels = emptyMap())
        val event = randomNewMessageEvent(
            cid = cachedChannel.cid,
            createdAt = now,
            message = channelMessage(createdAt = now),
        )

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = cachedChannel)

        // The stored copy carries the new message time, so the next cutoff check keeps it.
        assertEquals(EventHandlingResult.Add(cachedChannel.copy(lastMessageAt = now)), result)
    }

    @Test
    fun `Given a hidden cached channel When a campaign message arrives Should skip`() {
        val cachedChannel = randomChannel(extraData = mapOf("group" to "vip"), hidden = true)
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val message = channelMessage(createdAt = now).copy(extraData = mapOf("created_by_campaign_id" to "campaign"))
        val event = randomNewMessageEvent(cid = cachedChannel.cid, createdAt = now, message = message)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a hidden cached channel When a regular message arrives Should add it back`() {
        val cachedChannel = randomChannel(extraData = mapOf("group" to "vip"), hidden = true, lastMessageAt = now)
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNewMessageEvent(
            cid = cachedChannel.cid,
            createdAt = now,
            message = channelMessage(createdAt = now),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Add(cachedChannel), result)
    }

    @Test
    fun `Given a hidden cached channel When NotificationMessageNewEvent from a blocked user arrives Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val sender = randomUser()
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), blockedUserIds = listOf(sender.id))
        val event = randomNotificationMessageNewEvent(
            cid = channel.cid,
            channel = channel,
            message = channelMessage(createdAt = now).copy(user = sender),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = channel.copy(hidden = true))

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a hidden cached channel When NotificationMessageNewEvent with a regular message arrives Should watch and add`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap())
        val event = randomNotificationMessageNewEvent(
            cid = channel.cid,
            channel = channel,
            message = channelMessage(createdAt = now),
        )

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = channel.copy(hidden = true))

        assertEquals(EventHandlingResult.WatchAndAdd(channel.cid), result)
    }

    @Test
    fun `Given a cached channel past the cutoff When a thread reply not shown in the channel arrives Should skip`() {
        val cachedChannel = randomChannel(
            extraData = mapOf("group" to "expired"),
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(20),
        )
        val handler = handlerFor(groupKey = "expired", cachedChannels = emptyMap())
        val event = randomNewMessageEvent(
            cid = cachedChannel.cid,
            createdAt = now,
            message = channelMessage(createdAt = now).copy(parentId = randomString(), showInChannel = false),
        )

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a channel in the list When a partial update moves it to another group Should remove`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = partialUpdateOf(channel, custom = mapOf("group" to "other"))

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a channel in the list When a partial update removes its group Should remove`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = partialUpdateOf(channel, custom = emptyMap())

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a channel in the list When a partial update keeps it in the group Should skip`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"))

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a channel not in local state When a partial update moves it into the group Should watch and add`() {
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = randomUser())
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"))

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.WatchAndAdd(channel.cid), result)
    }

    @Test
    fun `Given a channel in the list When a partial update disables it Should remove`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"), disabled = true)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a channel not in local state When a partial update moves it into the group disabled Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = randomUser())
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"), disabled = true)

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a stale copy past the cutoff and not in the list When a partial update moves it into the group Should watch and add`() {
        // The copy's last activity can be stale, so the list checks the cutoff after the watch.
        val currentUser = randomUser()
        val channel = randomChannel(
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(20),
            extraData = mapOf("group" to "other"),
        )
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = currentUser)
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"))

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.WatchAndAdd(channel.cid), result)
    }

    @Test
    fun `Given a cached channel not in the list When a partial update moves it into the group Should watch and add`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = currentUser)
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"))

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.WatchAndAdd(channel.cid), result)
    }

    @Test
    fun `Given a channel not in local state When a partial update keeps it in another group Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = randomUser())
        val event = partialUpdateOf(channel, custom = mapOf("group" to "other"))

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given the user hid the channel When a partial update moves it into the group Should skip`() {
        val currentUser = randomUser()
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = true)
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = currentUser)
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"))

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a cached channel the user left When a partial update moves it into the group Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "other"))
        val cachedChannel = channel.copy(membership = null, hidden = false)
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = randomUser())
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"))

        val result = handler.handleChatEvent(event, Filters.neutral(), cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a recent last message in local state When a partial update sets an old touched_at Should keep it`() {
        val currentUser = randomUser()
        val channel = randomChannel(
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(1),
            extraData = mapOf("group" to "vip"),
        )
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip", "touched_at" to daysAgo(20)))

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Skip, result)
    }

    @Test
    fun `Given a channel in a filtered group When a partial update leaves it past the cutoff Should remove`() {
        val currentUser = randomUser()
        val channel = randomChannel(
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(20),
            extraData = mapOf("group" to "vip", "touched_at" to daysAgo(1)),
        )
        val cachedChannel = channel.copy(membership = randomMember(user = currentUser), hidden = false)
        val handler = handlerFor(
            groupKey = "vip",
            cachedChannels = mapOf(channel.cid to channel),
            currentUser = currentUser,
        )
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip", "touched_at" to daysAgo(20)))

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = cachedChannel)

        assertEquals(EventHandlingResult.Remove(channel.cid), result)
    }

    @Test
    fun `Given a channel not in local state When a partial update touches it within the cutoff Should watch and add`() {
        val channel = randomChannel(createdAt = daysAgo(30), extraData = mapOf("group" to "vip"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = randomUser())
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip", "touched_at" to daysAgo(1)))

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = null)

        assertEquals(EventHandlingResult.WatchAndAdd(channel.cid), result)
    }

    @Test
    fun `Given a channel not in local state When a partial update moves it into a filtered group Should watch and add`() {
        // The last message time is unknown without local data, so the list checks the cutoff after the watch.
        val channel = randomChannel(createdAt = daysAgo(30), extraData = mapOf("group" to "other"))
        val handler = handlerFor(groupKey = "vip", cachedChannels = emptyMap(), currentUser = randomUser())
        val event = partialUpdateOf(channel, custom = mapOf("group" to "vip"))

        val result = handler.handleChatEvent(event, cutoff14Days, cachedChannel = null)

        assertEquals(EventHandlingResult.WatchAndAdd(channel.cid), result)
    }

    @Test
    fun `Given a channel not in local state When a partial update moves it into a group the filter excludes Should skip`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val handler = handlerFor(groupKey = "all", cachedChannels = emptyMap(), currentUser = randomUser())
        val event = partialUpdateOf(channel, custom = mapOf("group" to "archived"))
        val filter = Filters.and(Filters.ne("group", "archived"), cutoff14Days)

        val result = handler.handleChatEvent(event, filter, cachedChannel = null)

        assertEquals(EventHandlingResult.Skip, result)
    }

    /** The event as the backend sends it: only the channel's basic fields and its custom data. */
    private fun partialUpdateOf(channel: Channel, custom: Map<String, Any>, disabled: Boolean = false) =
        randomNotificationChannelPartialUpdatedEvent(
            cid = channel.cid,
            channelType = channel.type,
            channelId = channel.id,
            channel = Channel(
                id = channel.id,
                type = channel.type,
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt,
                extraData = custom + ("disabled" to disabled),
            ),
        )

    private fun channelMessage(createdAt: Date) = randomMessage(
        type = "regular",
        createdAt = createdAt,
        createdLocallyAt = null,
        parentId = null,
        showInChannel = false,
        shadowed = false,
    )

    private fun daysAgo(days: Long): Date = Date(now.time - TimeUnit.DAYS.toMillis(days))

    private fun handlerFor(
        groupKey: String,
        cachedChannels: Map<String, Channel>,
        resolver: ChannelGroupResolver = defaultResolver,
        currentUser: User? = null,
        blockedUserIds: List<String> = emptyList(),
    ): GroupAwareChatEventHandler {
        val clientState = mock<ClientState> {
            whenever(it.user) doReturn MutableStateFlow(currentUser)
        }
        return GroupAwareChatEventHandler(
            groupKey = groupKey,
            resolver = resolver,
            channels = MutableStateFlow(cachedChannels),
            clientState = clientState,
            blockedUserIds = { blockedUserIds },
            now = { now },
        )
    }
}
