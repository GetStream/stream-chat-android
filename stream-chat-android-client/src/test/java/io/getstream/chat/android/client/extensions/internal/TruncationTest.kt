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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class TruncationTest {

    private val truncatedAt = Date(2_000)

    @Test
    fun `afterTruncationAt caps the unread count at the remaining messages`() {
        val read = randomChannelUserRead(unreadMessages = 5, lastRead = Date(1_000))

        val result = read.afterTruncationAt(truncatedAt, remainingMessageIds = setOf("kept"))

        assertEquals(1, result.unreadMessages)
    }

    @Test
    fun `afterTruncationAt keeps an unread count below the remaining messages`() {
        val read = randomChannelUserRead(unreadMessages = 1, lastRead = Date(3_000))

        val result = read.afterTruncationAt(truncatedAt, remainingMessageIds = setOf("a", "b", "c"))

        assertEquals(1, result.unreadMessages)
    }

    @Test
    fun `afterTruncationAt clears a last read message read before the truncation`() {
        val read = randomChannelUserRead(lastRead = Date(1_000), lastReadMessageId = "kept")

        val result = read.afterTruncationAt(truncatedAt, remainingMessageIds = setOf("kept"))

        assertNull(result.lastReadMessageId)
    }

    @Test
    fun `afterTruncationAt clears a last read message read exactly at the truncation`() {
        val read = randomChannelUserRead(lastRead = truncatedAt, lastReadMessageId = "kept")

        val result = read.afterTruncationAt(truncatedAt, remainingMessageIds = setOf("kept"))

        assertNull(result.lastReadMessageId)
    }

    @Test
    fun `afterTruncationAt clears a last read message the truncation removed`() {
        val read = randomChannelUserRead(lastRead = Date(3_000), lastReadMessageId = "removed")

        val result = read.afterTruncationAt(truncatedAt, remainingMessageIds = setOf("kept"))

        assertNull(result.lastReadMessageId)
    }

    @Test
    fun `afterTruncationAt keeps a last read message read after the truncation that still exists`() {
        val read = randomChannelUserRead(lastRead = Date(3_000), lastReadMessageId = "kept")

        val result = read.afterTruncationAt(truncatedAt, remainingMessageIds = setOf("kept"))

        assertEquals("kept", result.lastReadMessageId)
        assertEquals(read.lastRead, result.lastRead)
    }

    @Test
    fun `afterTruncationAt keeps the unread count when the remaining messages are unknown`() {
        val read = randomChannelUserRead(unreadMessages = 5, lastRead = Date(1_000), lastReadMessageId = "old")

        val result = read.afterTruncationAt(truncatedAt, remainingMessageIds = null)

        assertEquals(5, result.unreadMessages)
        assertNull(result.lastReadMessageId)
    }

    @Test
    fun `afterTruncationAt keeps a read after the truncation when the remaining messages are unknown`() {
        val read = randomChannelUserRead(unreadMessages = 2, lastRead = Date(3_000), lastReadMessageId = "kept")

        assertEquals(read, read.afterTruncationAt(truncatedAt, remainingMessageIds = null))
    }

    @Test
    fun `remainingMessageIdsAfterTruncationAt returns the server messages after the truncation`() {
        val page = listOf(
            serverMessage("removed", Date(1_000)),
            serverMessage("kept", Date(3_000)),
            serverMessage("pending", Date(3_500)).copy(syncStatus = SyncStatus.SYNC_NEEDED),
            serverMessage("ephemeral", Date(3_600)).copy(type = MessageType.EPHEMERAL),
        )

        val result = remainingMessageIdsAfterTruncationAt(truncatedAt, page, startsAtOldest = false)

        assertEquals(setOf("kept"), result)
    }

    @Test
    fun `remainingMessageIdsAfterTruncationAt leaves out messages the server does not count as unread`() {
        val page = listOf(
            serverMessage("kept", Date(3_000)),
            serverMessage("deleted", Date(3_100)).copy(deletedAt = Date(3_200)),
            serverMessage("silent", Date(3_300)).copy(silent = true),
            serverMessage("shadowed", Date(3_400)).copy(shadowed = true),
        )

        assertEquals(setOf("kept"), remainingMessageIdsAfterTruncationAt(truncatedAt, page, startsAtOldest = true))
    }

    @Test
    fun `remainingMessageIdsAfterTruncationAt is complete when only a deleted message reaches back past the truncation`() {
        val page = listOf(
            serverMessage("removed", Date(1_000)).copy(deletedAt = Date(1_500)),
            serverMessage("kept", Date(3_000)),
        )

        assertEquals(setOf("kept"), remainingMessageIdsAfterTruncationAt(truncatedAt, page, startsAtOldest = false))
    }

    @Test
    fun `remainingMessageIdsAfterTruncationAt is unknown when the page starts after the truncation`() {
        val page = listOf(serverMessage("kept", Date(3_000)))

        assertNull(remainingMessageIdsAfterTruncationAt(truncatedAt, page, startsAtOldest = false))
    }

    @Test
    fun `remainingMessageIdsAfterTruncationAt is known when the page starts at the oldest message`() {
        val page = listOf(serverMessage("kept", Date(3_000)))

        assertEquals(setOf("kept"), remainingMessageIdsAfterTruncationAt(truncatedAt, page, startsAtOldest = true))
    }

    @Test
    fun `withReadsAfterTruncationAt counts only the channel messages the truncation kept`() {
        val channel = randomChannel(
            messages = listOf(serverMessage("removed", Date(1_000)), serverMessage("kept", Date(3_000))),
            read = listOf(randomChannelUserRead(unreadMessages = 5, lastRead = Date(500), lastReadMessageId = "removed")),
        )

        val result = channel.withReadsAfterTruncationAt(truncatedAt).read.single()

        assertEquals(1, result.unreadMessages)
        assertNull(result.lastReadMessageId)
    }

    private fun serverMessage(id: String, createdAt: Date) =
        randomMessage(
            id = id,
            createdAt = createdAt,
            deletedAt = null,
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            silent = false,
            shadowed = false,
        )
}
