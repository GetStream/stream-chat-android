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
    fun `serverMessageIds leaves out the messages the server does not count`() {
        val sent = randomMessage(id = "sent", syncStatus = SyncStatus.COMPLETED, type = MessageType.REGULAR)
        val pending = randomMessage(id = "pending", syncStatus = SyncStatus.SYNC_NEEDED, type = MessageType.REGULAR)
        val ephemeral = randomMessage(id = "ephemeral", syncStatus = SyncStatus.COMPLETED, type = MessageType.EPHEMERAL)

        val result = serverMessageIds(listOf(sent, pending), listOf(ephemeral, sent))

        assertEquals(setOf("sent"), result)
    }

    @Test
    fun `withReadsAfterTruncationAt counts only the channel messages the truncation kept`() {
        val channel = randomChannel(
            messages = listOf(
                randomMessage(id = "removed", createdAt = Date(1_000)),
                randomMessage(id = "kept", createdAt = Date(3_000), syncStatus = SyncStatus.COMPLETED),
            ),
            read = listOf(randomChannelUserRead(unreadMessages = 5, lastRead = Date(500), lastReadMessageId = "removed")),
        )

        val result = channel.withReadsAfterTruncationAt(truncatedAt).read.single()

        assertEquals(1, result.unreadMessages)
        assertNull(result.lastReadMessageId)
    }
}
