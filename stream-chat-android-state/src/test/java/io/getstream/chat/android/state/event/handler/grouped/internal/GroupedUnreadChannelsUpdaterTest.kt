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

import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasGroupedUnreadChannels
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

private const val CURRENT_USER_ID = "user-1"
private const val CHANNEL_TYPE = "messaging"
private const val CHANNEL_ID = "channel-id"
private const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"

internal class GroupedUnreadChannelsUpdaterTest {

    private val stateRegistry: StateRegistry = mock()
    private val updater = GroupedUnreadChannelsUpdater(stateRegistry, CURRENT_USER_ID)

    // region HasGroupedUnreadChannels overload

    @Test
    fun `HasGroupedUnreadChannels event with non-null map replaces current map`() {
        val current = mapOf("a" to 1, "b" to 2)
        val event = hasGroupedUnreadChannels(mapOf("a" to 5, "c" to 3))

        val result = updater.calculateUpdatedCounts(current, event)

        assertEquals(mapOf("a" to 5, "c" to 3), result)
    }

    @Test
    fun `HasGroupedUnreadChannels event with null map returns current map unchanged`() {
        val current = mapOf("a" to 1, "b" to 2)
        val event = hasGroupedUnreadChannels(null)

        val result = updater.calculateUpdatedCounts(current, event)

        assertSame(current, result)
    }

    // endregion

    // region GroupedChannels (query result) overload

    @Test
    fun `GroupedChannels result merges per-group counts into current map`() {
        val current = mapOf("a" to 1, "b" to 2, "c" to 3)
        val result = GroupedChannels(
            groups = mapOf(
                "b" to groupedChannelsGroup("b", unreadChannels = 7),
                "d" to groupedChannelsGroup("d", unreadChannels = 4),
            ),
        )

        val next = updater.calculateUpdatedCounts(current, result)

        // a/c preserved (not in result), b/d overwritten/added
        assertEquals(mapOf("a" to 1, "b" to 7, "c" to 3, "d" to 4), next)
    }

    @Test
    fun `GroupedChannels result with empty groups returns current map unchanged`() {
        val current = mapOf("a" to 1)
        val result = GroupedChannels(groups = emptyMap())

        val next = updater.calculateUpdatedCounts(current, result)

        assertEquals(current, next)
    }

    // endregion

    // region ChannelUpdatedEvent / ChannelUpdatedByUserEvent overloads

    @Test
    fun `ChannelUpdatedEvent migrates count from old group to new group when channel had unread`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 3)
        val current = mapOf("a" to 5, "b" to 2)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, channelUpdatedEvent(newChannel))

        assertEquals(mapOf("a" to 4, "b" to 3), next)
    }

    @Test
    fun `ChannelUpdatedEvent with group set for first time only increments new group`() {
        seedActiveChannel(oldGroup = null, unreadMessages = 1)
        val current = mapOf("b" to 0)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, channelUpdatedEvent(newChannel))

        assertEquals(mapOf("b" to 1), next)
    }

    @Test
    fun `ChannelUpdatedEvent with group removed only decrements old group`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 2)
        val current = mapOf("a" to 2)
        val newChannel = channel(group = null)

        val next = updater.calculateUpdatedCounts(current, channelUpdatedEvent(newChannel))

        assertEquals(mapOf("a" to 1), next)
    }

    @Test
    fun `ChannelUpdatedEvent with unchanged group returns current map unchanged`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val current = mapOf("a" to 5)
        val newChannel = channel(group = "a")

        val next = updater.calculateUpdatedCounts(current, channelUpdatedEvent(newChannel))

        assertSame(current, next)
    }

    @Test
    fun `ChannelUpdatedEvent with no unread on cached channel returns current map unchanged`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 0)
        val current = mapOf("a" to 5, "b" to 1)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, channelUpdatedEvent(newChannel))

        assertSame(current, next)
    }

    @Test
    fun `ChannelUpdatedEvent decrement is clamped at zero`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        // Edge: backend-pushed map has 0 for "a" yet cached channel still claims unread.
        val current = mapOf("a" to 0)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, channelUpdatedEvent(newChannel))

        assertEquals(mapOf("a" to 0, "b" to 1), next)
    }

    @Test
    fun `ChannelUpdatedEvent for inactive channel returns current map unchanged`() {
        whenever(stateRegistry.isActiveChannel(CHANNEL_TYPE, CHANNEL_ID)) doReturn false
        val current = mapOf("a" to 5)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, channelUpdatedEvent(newChannel))

        assertSame(current, next)
    }

    @Test
    fun `ChannelUpdatedByUserEvent applies the same migration semantics`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 2)
        val current = mapOf("a" to 3, "b" to 1)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, channelUpdatedByUserEvent(newChannel))

        assertEquals(mapOf("a" to 2, "b" to 2), next)
    }

    // endregion

    // region helpers

    private fun seedActiveChannel(oldGroup: String?, unreadMessages: Int) {
        val cached = channel(group = oldGroup).copy(
            read = listOf(
                randomChannelUserRead(
                    user = User(id = CURRENT_USER_ID),
                    unreadMessages = unreadMessages,
                ),
            ),
        )
        val channelMutableState: ChannelMutableState = mock {
            on { toChannel() } doReturn cached
        }
        whenever(stateRegistry.isActiveChannel(CHANNEL_TYPE, CHANNEL_ID)) doReturn true
        whenever(stateRegistry.mutableChannel(CHANNEL_TYPE, CHANNEL_ID)) doReturn channelMutableState
    }

    private fun channel(group: String?): Channel = randomChannel(
        id = CHANNEL_ID,
        type = CHANNEL_TYPE,
        extraData = group?.let { mapOf("group" to it) } ?: emptyMap(),
        read = emptyList(),
    )

    private fun channelUpdatedEvent(newChannel: Channel): ChannelUpdatedEvent = ChannelUpdatedEvent(
        type = "channel.updated",
        createdAt = Date(),
        rawCreatedAt = "",
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = newChannel,
        message = null,
    )

    private fun channelUpdatedByUserEvent(newChannel: Channel): ChannelUpdatedByUserEvent =
        ChannelUpdatedByUserEvent(
            type = "channel.updated_by_user",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = CID,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            user = randomUser(),
            channel = newChannel,
            message = null,
        )

    private fun hasGroupedUnreadChannels(map: Map<String, Int>?): HasGroupedUnreadChannels =
        NotificationMarkReadEvent(
            type = "notification.mark_read",
            createdAt = Date(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = CID,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            lastReadMessageId = null,
            groupedUnreadChannels = map,
        )

    private fun groupedChannelsGroup(key: String, unreadChannels: Int): GroupedChannelsGroup =
        GroupedChannelsGroup(
            groupKey = key,
            channels = emptyList(),
            unreadChannels = unreadChannels,
            next = null,
            prev = null,
        )

    // endregion
}
