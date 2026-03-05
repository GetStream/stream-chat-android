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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.models.Config
import io.getstream.chat.android.randomMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplPendingMessagesTest : ChannelStateImplTestBase() {

    private fun enablePendingMessages() {
        channelState.setChannelConfig(Config(markMessagesPending = true))
    }

    // region setChannelConfig — enable/disable gate

    @Nested
    inner class SetChannelConfig {

        @Test
        fun `pending messages are not visible when markMessagesPending is false (default)`() {
            // Given — feature is disabled by default
            val pending = createMessage(1, timestamp = 1000L)
            // When
            channelState.setPendingMessages(listOf(pending))
            // Then — message is silently ignored
            assertFalse(channelState.messages.value.any { it.id == pending.id })
        }

        @Test
        fun `pending messages become visible after enabling via setChannelConfig`() {
            // Given
            val pending = createMessage(1, timestamp = 1000L)
            // When
            enablePendingMessages()
            channelState.setPendingMessages(listOf(pending))
            // Then
            assertTrue(channelState.messages.value.any { it.id == pending.id })
        }

        @Test
        fun `disabling via setChannelConfig clears previously visible pending messages`() {
            // Given — start enabled with a pending message
            enablePendingMessages()
            val pending = createMessage(1, timestamp = 1000L)
            channelState.setPendingMessages(listOf(pending))
            assertTrue(channelState.messages.value.any { it.id == pending.id })
            // When — disable the feature
            channelState.setChannelConfig(Config(markMessagesPending = false))
            // Then — pending message disappears
            assertFalse(channelState.messages.value.any { it.id == pending.id })
        }
    }

    // endregion

    // region messages merge

    @Nested
    inner class MessagesMerge {

        @Test
        fun `pending message is merged before a later regular message`() {
            // Given
            enablePendingMessages()
            val regular = createMessage(2, timestamp = 2000L)
            val pending = createMessage(1, timestamp = 1000L)
            channelState.setMessages(listOf(regular))
            // When
            channelState.setPendingMessages(listOf(pending))
            // Then — pending (t=1000) comes before regular (t=2000)
            val ids = channelState.messages.value.map { it.id }
            assertEquals(listOf(pending.id, regular.id), ids)
        }

        @Test
        fun `pending message is merged after an earlier regular message`() {
            // Given
            enablePendingMessages()
            val regular = createMessage(1, timestamp = 1000L)
            val pending = createMessage(2, timestamp = 2000L)
            channelState.setMessages(listOf(regular))
            // When
            channelState.setPendingMessages(listOf(pending))
            // Then — regular (t=1000) comes before pending (t=2000)
            val ids = channelState.messages.value.map { it.id }
            assertEquals(listOf(regular.id, pending.id), ids)
        }

        @Test
        fun `multiple pending messages are merged in sorted order`() {
            // Given
            enablePendingMessages()
            val r1 = createMessage(1, timestamp = 1000L)
            val r2 = createMessage(4, timestamp = 4000L)
            val p1 = createMessage(2, timestamp = 2000L)
            val p2 = createMessage(3, timestamp = 3000L)
            channelState.setMessages(listOf(r1, r2))
            // When
            channelState.setPendingMessages(listOf(p2, p1)) // intentionally reversed
            // Then
            val ids = channelState.messages.value.map { it.id }
            assertEquals(listOf(r1.id, p1.id, p2.id, r2.id), ids)
        }

        @Test
        fun `messages flow contains only regular messages when pending list is empty`() {
            // Given
            enablePendingMessages()
            val regular = createMessage(1, timestamp = 1000L)
            channelState.setMessages(listOf(regular))
            // When — no pending messages set
            channelState.setPendingMessages(emptyList())
            // Then
            assertEquals(listOf(regular.id), channelState.messages.value.map { it.id })
        }
    }

    // endregion

    // region date range filtering via ChannelStateImpl delegation

    @Nested
    inner class DateRangeFiltering {

        @Test
        fun `pending message below oldest loaded date is not shown`() {
            // Given
            enablePendingMessages()
            val pending = createMessage(1, timestamp = 500L)
            val floor = randomMessage(id = "floor", createdAt = Date(1000L), createdLocallyAt = null)
            channelState.setPendingMessages(listOf(pending))
            // When — floor = 1000, pending at 500 is below it
            channelState.advanceOldestLoadedDate(listOf(floor))
            // Then
            assertFalse(channelState.messages.value.any { it.id == pending.id })
        }

        @Test
        fun `pending message above newest loaded date ceiling is not shown`() {
            // Given
            enablePendingMessages()
            val pending = createMessage(1, timestamp = 3000L)
            channelState.setPendingMessages(listOf(pending))
            // When — ceiling = 2000, pending at 3000 is above it
            channelState.setNewestLoadedDate(Date(2000L))
            // Then
            assertFalse(channelState.messages.value.any { it.id == pending.id })
        }

        @Test
        fun `pending message within both floor and ceiling is shown`() {
            // Given
            enablePendingMessages()
            val pending = createMessage(1, timestamp = 2000L)
            val floorMsg = randomMessage(id = "f", createdAt = Date(1000L), createdLocallyAt = null)
            channelState.setPendingMessages(listOf(pending))
            channelState.advanceOldestLoadedDate(listOf(floorMsg)) // floor = 1000
            channelState.setNewestLoadedDate(Date(3000L)) // ceiling = 3000
            // Then
            assertTrue(channelState.messages.value.any { it.id == pending.id })
        }

        @Test
        fun `setNewestLoadedDate null removes ceiling and reveals previously hidden pending messages`() {
            // Given
            enablePendingMessages()
            val pending = createMessage(1, timestamp = 5000L)
            channelState.setPendingMessages(listOf(pending))
            channelState.setNewestLoadedDate(Date(1000L))
            assertFalse(channelState.messages.value.any { it.id == pending.id })
            // When
            channelState.setNewestLoadedDate(null)
            // Then
            assertTrue(channelState.messages.value.any { it.id == pending.id })
        }

        @Test
        fun `advanceNewestLoadedDate advances ceiling to reveal newer pending messages`() {
            // Given
            enablePendingMessages()
            val pending = createMessage(1, timestamp = 3000L)
            channelState.setPendingMessages(listOf(pending))
            channelState.advanceNewestLoadedDate(Date(2000L)) // hidden
            assertFalse(channelState.messages.value.any { it.id == pending.id })
            // When — advance to 4000
            channelState.advanceNewestLoadedDate(Date(4000L))
            // Then
            assertTrue(channelState.messages.value.any { it.id == pending.id })
        }
    }

    // endregion

    // region removePendingMessage

    @Nested
    inner class RemovePendingMessage {

        @Test
        fun `removes message from the combined messages flow`() {
            // Given
            enablePendingMessages()
            val p1 = createMessage(1, timestamp = 1000L)
            val p2 = createMessage(2, timestamp = 2000L)
            channelState.setPendingMessages(listOf(p1, p2))
            // When
            channelState.removePendingMessage(p1.id)
            // Then
            assertFalse(channelState.messages.value.any { it.id == p1.id })
            assertTrue(channelState.messages.value.any { it.id == p2.id })
        }
    }

    // endregion

    // region destroy

    @Nested
    inner class Destroy {

        @Test
        fun `destroy clears pending messages from the messages flow`() {
            // Given
            enablePendingMessages()
            val pending = createMessage(1, timestamp = 1000L)
            channelState.setPendingMessages(listOf(pending))
            assertTrue(channelState.messages.value.any { it.id == pending.id })
            // When
            channelState.destroy()
            // Then
            assertFalse(channelState.messages.value.any { it.id == pending.id })
        }
    }

    // endregion
}
