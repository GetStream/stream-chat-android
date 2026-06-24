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

package io.getstream.chat.android.client.internal.state.event.handler.grouped.internal

import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasGroupedUnreadChannels
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

private const val CURRENT_USER_ID = "user-1"
private const val OTHER_USER_ID = "user-2"
private const val CHANNEL_TYPE = "messaging"
private const val CHANNEL_ID = "channel-id"
private const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
private const val BATCH_ID = 1
private const val NEXT_BATCH_ID = 2

internal class GroupedUnreadChannelsUpdaterTest {

    private val stateRegistry: StateRegistry = mock()
    private val updater = GroupedUnreadChannelsUpdater(stateRegistry, CURRENT_USER_ID)

    // region HasGroupedUnreadChannels overload

    @Test
    fun `HasGroupedUnreadChannels event with non-null map replaces current map`() {
        val current = mapOf("a" to 1, "b" to 2)
        val event = hasGroupedUnreadChannels(mapOf("a" to 5, "c" to 3))

        val result = updater.calculateUpdatedCounts(current, BATCH_ID, event)

        assertEquals(mapOf("a" to 5, "c" to 3), result)
    }

    @Test
    fun `HasGroupedUnreadChannels event with null map returns current map unchanged`() {
        val current = mapOf("a" to 1, "b" to 2)
        val event = hasGroupedUnreadChannels(null)

        val result = updater.calculateUpdatedCounts(current, BATCH_ID, event)

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

        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(newChannel))

        assertEquals(mapOf("a" to 4, "b" to 3), next)
    }

    @Test
    fun `ChannelUpdatedEvent with group set for first time only increments new group`() {
        seedActiveChannel(oldGroup = null, unreadMessages = 1)
        val current = mapOf("b" to 0)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(newChannel))

        assertEquals(mapOf("b" to 1), next)
    }

    @Test
    fun `ChannelUpdatedEvent with group removed only decrements old group`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 2)
        val current = mapOf("a" to 2)
        val newChannel = channel(group = null)

        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(newChannel))

        assertEquals(mapOf("a" to 1), next)
    }

    @Test
    fun `ChannelUpdatedEvent with unchanged group returns current map unchanged`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val current = mapOf("a" to 5)
        val newChannel = channel(group = "a")

        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(newChannel))

        assertSame(current, next)
    }

    @Test
    fun `ChannelUpdatedEvent with no unread on cached channel returns current map unchanged`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 0)
        val current = mapOf("a" to 5, "b" to 1)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(newChannel))

        assertSame(current, next)
    }

    @Test
    fun `ChannelUpdatedEvent decrement is clamped at zero`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        // Edge: backend-pushed map has 0 for "a" yet cached channel still claims unread.
        val current = mapOf("a" to 0)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(newChannel))

        assertEquals(mapOf("a" to 0, "b" to 1), next)
    }

    @Test
    fun `ChannelUpdatedEvent for inactive channel returns current map unchanged`() {
        whenever(stateRegistry.isActiveChannel(ChannelId.fromTypeAndId(CHANNEL_TYPE, CHANNEL_ID)!!)) doReturn false
        val current = mapOf("a" to 5)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(newChannel))

        assertSame(current, next)
    }

    @Test
    fun `ChannelUpdatedByUserEvent applies the same migration semantics`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 2)
        val current = mapOf("a" to 3, "b" to 1)
        val newChannel = channel(group = "b")

        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedByUserEvent(newChannel))

        assertEquals(mapOf("a" to 2, "b" to 2), next)
    }

    // endregion

    // region Same-batch dedup

    @Test
    fun `Two ChannelUpdatedEvent for same cid in one batch apply migration only once`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val newChannel = channel(group = "b")
        val event = channelUpdatedEvent(newChannel)
        val initial = mapOf("a" to 5, "b" to 0)

        val afterFirst = updater.calculateUpdatedCounts(initial, BATCH_ID, event)
        val afterSecond = updater.calculateUpdatedCounts(afterFirst, BATCH_ID, event)

        // First call applies the delta; second call is suppressed by processedCids.
        assertEquals(mapOf("a" to 4, "b" to 1), afterFirst)
        assertSame(afterFirst, afterSecond)
    }

    @Test
    fun `Same channel updated in two different batches applies delta in each`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val newChannel = channel(group = "b")
        val event = channelUpdatedEvent(newChannel)
        val initial = mapOf("a" to 5, "b" to 0)

        val afterFirst = updater.calculateUpdatedCounts(initial, BATCH_ID, event)
        val afterSecond = updater.calculateUpdatedCounts(afterFirst, NEXT_BATCH_ID, event)

        assertEquals(mapOf("a" to 4, "b" to 1), afterFirst)
        assertEquals(mapOf("a" to 3, "b" to 2), afterSecond)
    }

    @Test
    fun `No-op ChannelUpdatedEvent does not consume the per-batch dedup slot`() {
        // First event is a no-op (same group as cached); a later event with a real group change
        // for the same cid should still apply, because we only mark processed on real mutation.
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val noop = channelUpdatedEvent(channel(group = "a"))
        val real = channelUpdatedEvent(channel(group = "b"))
        val initial = mapOf("a" to 5, "b" to 0)

        val afterNoop = updater.calculateUpdatedCounts(initial, BATCH_ID, noop)
        val afterReal = updater.calculateUpdatedCounts(afterNoop, BATCH_ID, real)

        assertSame(initial, afterNoop)
        assertEquals(mapOf("a" to 4, "b" to 1), afterReal)
    }

    // endregion

    // region HGUC subtype side effects on overrides

    @Test
    fun `mark_read before channel updated in same batch suppresses delta`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val initial = mapOf("a" to 5, "b" to 0)
        val markRead = notificationMarkReadEvent(map = mapOf("a" to 4, "b" to 0))
        val update = channelUpdatedEvent(channel(group = "b"))

        val afterMarkRead = updater.calculateUpdatedCounts(initial, BATCH_ID, markRead)
        val afterUpdate = updater.calculateUpdatedCounts(afterMarkRead, BATCH_ID, update)

        assertEquals(mapOf("a" to 4, "b" to 0), afterMarkRead)
        assertSame(afterMarkRead, afterUpdate)
    }

    @Test
    fun `mark_unread before channel updated in same batch allows delta`() {
        // Cached channel has unread=0; without override the delta would skip.
        // mark_unread flips override to true so delta fires.
        seedActiveChannel(oldGroup = "a", unreadMessages = 0)
        val initial = mapOf("a" to 5, "b" to 0)
        val markUnread = notificationMarkUnreadEvent(map = mapOf("a" to 6, "b" to 0))
        val update = channelUpdatedEvent(channel(group = "b"))

        val afterMarkUnread = updater.calculateUpdatedCounts(initial, BATCH_ID, markUnread)
        val afterUpdate = updater.calculateUpdatedCounts(afterMarkUnread, BATCH_ID, update)

        assertEquals(mapOf("a" to 6, "b" to 0), afterMarkUnread)
        assertEquals(mapOf("a" to 5, "b" to 1), afterUpdate)
    }

    @Test
    fun `new message from other user before channel updated in same batch allows delta`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 0)
        val initial = mapOf("a" to 5, "b" to 0)
        val newMessage = newMessageEvent(senderUserId = OTHER_USER_ID, map = mapOf("a" to 6, "b" to 0))
        val update = channelUpdatedEvent(channel(group = "b"))

        val afterNewMessage = updater.calculateUpdatedCounts(initial, BATCH_ID, newMessage)
        val afterUpdate = updater.calculateUpdatedCounts(afterNewMessage, BATCH_ID, update)

        assertEquals(mapOf("a" to 6, "b" to 0), afterNewMessage)
        assertEquals(mapOf("a" to 5, "b" to 1), afterUpdate)
    }

    @Test
    fun `new message from current user does not flip unread override`() {
        // Cached channel says unread=0 and sender is the current user, so no override.
        // Delta then reads cached hadUnread=false and is a no-op.
        seedActiveChannel(oldGroup = "a", unreadMessages = 0)
        val initial = mapOf("a" to 5, "b" to 0)
        val newMessage = newMessageEvent(senderUserId = CURRENT_USER_ID, map = mapOf("a" to 5, "b" to 0))
        val update = channelUpdatedEvent(channel(group = "b"))

        val afterNewMessage = updater.calculateUpdatedCounts(initial, BATCH_ID, newMessage)
        val afterUpdate = updater.calculateUpdatedCounts(afterNewMessage, BATCH_ID, update)

        assertEquals(mapOf("a" to 5, "b" to 0), afterNewMessage)
        assertSame(afterNewMessage, afterUpdate)
    }

    @Test
    fun `notification channel_deleted HGUC before channel updated suppresses delta`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val initial = mapOf("a" to 5, "b" to 0)
        val deleted = notificationChannelDeletedEvent(map = mapOf("a" to 4, "b" to 0))
        val update = channelUpdatedEvent(channel(group = "b"))

        val afterDeleted = updater.calculateUpdatedCounts(initial, BATCH_ID, deleted)
        val afterUpdate = updater.calculateUpdatedCounts(afterDeleted, BATCH_ID, update)

        assertEquals(mapOf("a" to 4, "b" to 0), afterDeleted)
        assertSame(afterDeleted, afterUpdate)
    }

    @Test
    fun `notification channel_truncated before channel updated suppresses delta via override`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val initial = mapOf("a" to 5, "b" to 0)
        val truncated = notificationChannelTruncatedEvent(map = mapOf("a" to 4, "b" to 0))
        val update = channelUpdatedEvent(channel(group = "b"))

        val afterTruncated = updater.calculateUpdatedCounts(initial, BATCH_ID, truncated)
        val afterUpdate = updater.calculateUpdatedCounts(afterTruncated, BATCH_ID, update)

        assertEquals(mapOf("a" to 4, "b" to 0), afterTruncated)
        assertSame(afterTruncated, afterUpdate)
    }

    // endregion

    // region notifyChannelRemoved

    @Test
    fun `notifyChannelRemoved before channel updated suppresses delta`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val current = mapOf("a" to 5, "b" to 0)

        updater.notifyChannelRemoved(BATCH_ID, CID)
        val next = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(channel(group = "b")))

        assertSame(current, next)
    }

    // endregion

    // region MarkAllRead

    @Test
    fun `MarkAllReadEvent before channel updated suppresses delta`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val initial = mapOf("a" to 5, "b" to 0)

        val afterMarkAllRead = updater.calculateUpdatedCounts(initial, BATCH_ID, markAllReadEvent(map = null))
        val next = updater.calculateUpdatedCounts(
            afterMarkAllRead,
            BATCH_ID,
            channelUpdatedEvent(channel(group = "b")),
        )

        assertSame(afterMarkAllRead, next)
    }

    @Test
    fun `MarkAllReadEvent with authoritative map replaces map and suppresses subsequent delta`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val initial = mapOf("a" to 5, "b" to 0)

        val afterMarkAllRead = updater.calculateUpdatedCounts(
            initial,
            BATCH_ID,
            markAllReadEvent(map = mapOf("a" to 0, "b" to 0)),
        )
        val next = updater.calculateUpdatedCounts(
            afterMarkAllRead,
            BATCH_ID,
            channelUpdatedEvent(channel(group = "b")),
        )

        assertEquals(mapOf("a" to 0, "b" to 0), afterMarkAllRead)
        assertSame(afterMarkAllRead, next)
    }

    @Test
    fun `per-cid unread override after MarkAllReadEvent wins for that cid`() {
        // MarkAllRead would normally suppress; a later new_message from another user re-sets
        // the per-cid override to true, so the delta fires.
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val initial = mapOf("a" to 5, "b" to 0)

        val afterMarkAllRead = updater.calculateUpdatedCounts(initial, BATCH_ID, markAllReadEvent(map = null))
        val afterNewMessage = updater.calculateUpdatedCounts(
            afterMarkAllRead,
            BATCH_ID,
            newMessageEvent(senderUserId = OTHER_USER_ID, map = mapOf("a" to 5, "b" to 0)),
        )
        val afterUpdate = updater.calculateUpdatedCounts(
            afterNewMessage,
            BATCH_ID,
            channelUpdatedEvent(channel(group = "b")),
        )

        assertEquals(mapOf("a" to 4, "b" to 1), afterUpdate)
    }

    @Test
    fun `MarkAllReadEvent clears prior new_message override for same cid`() {
        // new_message first sets hadUnreadOverride[X]=true; MarkAllRead then clears it.
        // Subsequent channel.updated should no-op because the global flag now fires.
        seedActiveChannel(oldGroup = "a", unreadMessages = 0)
        val initial = mapOf("a" to 5, "b" to 0)

        val afterNewMessage = updater.calculateUpdatedCounts(
            initial,
            BATCH_ID,
            newMessageEvent(senderUserId = OTHER_USER_ID, map = mapOf("a" to 6, "b" to 0)),
        )
        val afterMarkAllRead = updater.calculateUpdatedCounts(afterNewMessage, BATCH_ID, markAllReadEvent(map = null))
        val afterUpdate = updater.calculateUpdatedCounts(
            afterMarkAllRead,
            BATCH_ID,
            channelUpdatedEvent(channel(group = "b")),
        )

        assertSame(afterMarkAllRead, afterUpdate)
    }

    // endregion

    // region Batch rotation

    @Test
    fun `dedup state is cleared when a new batch id arrives`() {
        seedActiveChannel(oldGroup = "a", unreadMessages = 1)
        val current = mapOf("a" to 5, "b" to 0)

        updater.notifyChannelRemoved(BATCH_ID, CID)
        val sameBatch = updater.calculateUpdatedCounts(current, BATCH_ID, channelUpdatedEvent(channel(group = "b")))
        val nextBatch = updater.calculateUpdatedCounts(current, NEXT_BATCH_ID, channelUpdatedEvent(channel(group = "b")))

        assertSame(current, sameBatch)
        assertEquals(mapOf("a" to 4, "b" to 1), nextBatch)
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
        val channelMutableState: ChannelState = mock {
            on { toChannel() } doReturn cached
        }
        whenever(stateRegistry.isActiveChannel(ChannelId.fromTypeAndId(CHANNEL_TYPE, CHANNEL_ID)!!)) doReturn true
        whenever(stateRegistry.channel(CHANNEL_TYPE, CHANNEL_ID)) doReturn channelMutableState
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
        notificationMarkReadEvent(map = map)

    private fun markAllReadEvent(map: Map<String, Int>?): MarkAllReadEvent =
        MarkAllReadEvent(
            type = "notification.mark_read",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = CURRENT_USER_ID),
            totalUnreadCount = 0,
            unreadChannels = 0,
            groupedUnreadChannels = map,
        )

    private fun notificationMarkReadEvent(map: Map<String, Int>?): NotificationMarkReadEvent =
        NotificationMarkReadEvent(
            type = "notification.mark_read",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = CURRENT_USER_ID),
            cid = CID,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            lastReadMessageId = null,
            groupedUnreadChannels = map,
        )

    private fun notificationMarkUnreadEvent(map: Map<String, Int>?): NotificationMarkUnreadEvent =
        NotificationMarkUnreadEvent(
            type = "notification.mark_unread",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = CURRENT_USER_ID),
            cid = CID,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            firstUnreadMessageId = "msg-1",
            lastReadMessageAt = Date(),
            lastReadMessageId = null,
            unreadMessages = 1,
            totalUnreadCount = 1,
            unreadChannels = 1,
            threadId = null,
            unreadThreads = 0,
            unreadThreadMessages = 0,
            groupedUnreadChannels = map,
        )

    private fun newMessageEvent(senderUserId: String, map: Map<String, Int>?): NewMessageEvent =
        NewMessageEvent(
            type = "message.new",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = senderUserId),
            cid = CID,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            message = randomMessage(),
            watcherCount = 0,
            totalUnreadCount = 1,
            unreadChannels = 1,
            channelMessageCount = 1,
            groupedUnreadChannels = map,
        )

    private fun notificationChannelDeletedEvent(map: Map<String, Int>?): NotificationChannelDeletedEvent =
        NotificationChannelDeletedEvent(
            type = "notification.channel_deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = CID,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            channel = channel(group = "a"),
            totalUnreadCount = 0,
            unreadChannels = 0,
            groupedUnreadChannels = map,
        )

    private fun notificationChannelTruncatedEvent(map: Map<String, Int>?): NotificationChannelTruncatedEvent =
        NotificationChannelTruncatedEvent(
            type = "notification.channel_truncated",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = CID,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            channel = channel(group = "a"),
            totalUnreadCount = 0,
            unreadChannels = 0,
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
