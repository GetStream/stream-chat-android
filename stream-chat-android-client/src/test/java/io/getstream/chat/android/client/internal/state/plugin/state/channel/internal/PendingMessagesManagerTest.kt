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

import io.getstream.chat.android.randomMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

internal class PendingMessagesManagerTest {

    private lateinit var sut: PendingMessagesManager

    @BeforeEach
    fun setUp() {
        sut = PendingMessagesManager()
    }

    // region initial state

    @Test
    fun `pendingMessagesInRange is empty when disabled (initial state)`() {
        assertTrue(sut.pendingMessagesInRange.value.isEmpty())
    }

    // endregion

    // region setEnabled

    @Nested
    inner class SetEnabled {

        @Test
        fun `enabling makes pending messages visible`() {
            // Given
            val message = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(message))
            // When
            sut.setEnabled(true)
            // Then
            assertEquals(listOf(message), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `disabling returns empty list`() {
            // Given
            val message = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setEnabled(true)
            sut.setPendingMessages(listOf(message))
            // When
            sut.setEnabled(false)
            // Then
            assertTrue(sut.pendingMessagesInRange.value.isEmpty())
        }

        @Test
        fun `disabling clears buffered messages so re-enabling starts empty`() {
            // Given
            val message = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setEnabled(true)
            sut.setPendingMessages(listOf(message))
            // When
            sut.setEnabled(false)
            sut.setEnabled(true)
            // Then
            assertTrue(sut.pendingMessagesInRange.value.isEmpty())
        }
    }

    // endregion

    // region setPendingMessages

    @Nested
    inner class SetPendingMessages {

        @Test
        fun `messages are sorted by createdAt ascending`() {
            // Given
            sut.setEnabled(true)
            val newer = randomMessage(id = "m1", createdAt = Date(2000L), createdLocallyAt = null)
            val older = randomMessage(id = "m2", createdAt = Date(1000L), createdLocallyAt = null)
            // When
            sut.setPendingMessages(listOf(newer, older))
            // Then
            assertEquals(listOf(older, newer), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `replaces previously set messages`() {
            // Given
            sut.setEnabled(true)
            val first = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(first))
            val second = randomMessage(id = "m2", createdAt = Date(2000L), createdLocallyAt = null)
            // When
            sut.setPendingMessages(listOf(second))
            // Then
            assertEquals(listOf(second), sut.pendingMessagesInRange.value)
        }
    }

    // endregion

    // region removePendingMessage

    @Nested
    inner class RemovePendingMessage {

        @Test
        fun `removes existing message by id`() {
            // Given
            sut.setEnabled(true)
            val m1 = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            val m2 = randomMessage(id = "m2", createdAt = Date(2000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(m1, m2))
            // When
            sut.removePendingMessage("m1")
            // Then
            assertEquals(listOf(m2), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `no-op when id is not found — list content is unchanged`() {
            // Given
            val m1 = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setEnabled(true)
            sut.setPendingMessages(listOf(m1))
            // When
            sut.removePendingMessage("does-not-exist")
            // Then — message is still present, nothing was removed
            assertEquals(listOf(m1), sut.pendingMessagesInRange.value)
        }
    }

    // endregion

    // region advanceOldestLoadedDate

    @Nested
    inner class AdvanceOldestLoadedDate {

        @Test
        fun `initializes floor on first call and shows messages at or after it`() {
            // Given
            sut.setEnabled(true)
            val floor = Date(1000L)
            val atFloor = randomMessage(id = "m1", createdAt = floor, createdLocallyAt = null)
            val belowFloor = randomMessage(id = "m2", createdAt = Date(500L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(atFloor, belowFloor))
            // When — first call with a message whose createdAt = floor
            sut.advanceOldestLoadedDate(listOf(atFloor))
            // Then
            assertEquals(listOf(atFloor), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `advances floor backward when new date is older`() {
            // Given
            sut.setEnabled(true)
            val initial = randomMessage(id = "anchor", createdAt = Date(1000L), createdLocallyAt = null)
            sut.advanceOldestLoadedDate(listOf(initial)) // floor = 1000
            val older = randomMessage(id = "m2", createdAt = Date(500L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(initial, older))
            // When — provide a message older than the current floor
            sut.advanceOldestLoadedDate(listOf(older))
            // Then — older message is now in range
            assertEquals(listOf(older, initial), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `does NOT advance floor when new date is newer than current floor`() {
            // Given
            sut.setEnabled(true)
            val floorMsg = randomMessage(id = "m1", createdAt = Date(500L), createdLocallyAt = null)
            val outside = randomMessage(id = "m2", createdAt = Date(200L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(floorMsg, outside))
            sut.advanceOldestLoadedDate(listOf(floorMsg)) // floor = 500
            // When — try to advance with a newer date (1000 > 500)
            val newer = randomMessage(id = "anchor2", createdAt = Date(1000L), createdLocallyAt = null)
            sut.advanceOldestLoadedDate(listOf(newer))
            // Then — message at 200 still outside the floor
            assertEquals(listOf(floorMsg), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `no-op when message list is empty`() {
            // Given
            sut.setEnabled(true)
            val msg = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(msg))
            // When — floor remains null
            sut.advanceOldestLoadedDate(emptyList())
            // Then — floor is still null, so no filter applied
            assertEquals(listOf(msg), sut.pendingMessagesInRange.value)
        }
    }

    // endregion

    // region setNewestLoadedDate

    @Nested
    inner class SetNewestLoadedDate {

        @Test
        fun `sets ceiling and excludes messages above it`() {
            // Given
            sut.setEnabled(true)
            val ceiling = Date(2000L)
            val atCeiling = randomMessage(id = "m1", createdAt = ceiling, createdLocallyAt = null)
            val aboveCeiling = randomMessage(id = "m2", createdAt = Date(3000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(atCeiling, aboveCeiling))
            // When
            sut.setNewestLoadedDate(ceiling)
            // Then
            assertEquals(listOf(atCeiling), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `null removes ceiling so all pending messages pass`() {
            // Given
            sut.setEnabled(true)
            val msg = randomMessage(id = "m1", createdAt = Date(5000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(msg))
            sut.setNewestLoadedDate(Date(1000L)) // ceiling blocks msg
            assertTrue(sut.pendingMessagesInRange.value.isEmpty())
            // When
            sut.setNewestLoadedDate(null)
            // Then
            assertEquals(listOf(msg), sut.pendingMessagesInRange.value)
        }
    }

    // endregion

    // region advanceNewestLoadedDate

    @Nested
    inner class AdvanceNewestLoadedDate {

        @Test
        fun `first non-null call sets ceiling`() {
            // Given
            sut.setEnabled(true)
            val msg = randomMessage(id = "m1", createdAt = Date(3000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(msg))
            // When — ceiling = 2000, msg at 3000 is above
            sut.advanceNewestLoadedDate(Date(2000L))
            // Then
            assertTrue(sut.pendingMessagesInRange.value.isEmpty())
        }

        @Test
        fun `advances ceiling forward when date is newer`() {
            // Given
            sut.setEnabled(true)
            val msg = randomMessage(id = "m1", createdAt = Date(3000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(msg))
            sut.advanceNewestLoadedDate(Date(2000L)) // ceiling = 2000, msg hidden
            // When — advance to 4000
            sut.advanceNewestLoadedDate(Date(4000L))
            // Then — msg at 3000 is now within range
            assertEquals(listOf(msg), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `does NOT advance ceiling backward`() {
            // Given
            sut.setEnabled(true)
            val msg = randomMessage(id = "m1", createdAt = Date(3000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(msg))
            sut.advanceNewestLoadedDate(Date(4000L)) // ceiling = 4000, msg visible
            // When — try to retreat ceiling to 2000
            sut.advanceNewestLoadedDate(Date(2000L))
            // Then — msg still visible
            assertEquals(listOf(msg), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `null argument is a no-op`() {
            // Given
            sut.setEnabled(true)
            val msg = randomMessage(id = "m1", createdAt = Date(3000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(msg))
            sut.advanceNewestLoadedDate(Date(2000L)) // ceiling = 2000
            // When
            sut.advanceNewestLoadedDate(null)
            // Then — ceiling unchanged, msg still hidden
            assertTrue(sut.pendingMessagesInRange.value.isEmpty())
        }
    }

    // endregion

    // region reset

    @Nested
    inner class Reset {

        @Test
        fun `clears pending messages and date range`() {
            // Given
            sut.setEnabled(true)
            val msg = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(msg))
            sut.advanceOldestLoadedDate(listOf(msg))
            sut.setNewestLoadedDate(Date(5000L))
            // When
            sut.reset()
            // Then — messages cleared; null floor and ceiling means no messages to show anyway
            assertTrue(sut.pendingMessagesInRange.value.isEmpty())
        }

        @Test
        fun `state can be repopulated after reset`() {
            // Given
            sut.setEnabled(true)
            val msg = randomMessage(id = "m1", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(msg))
            sut.reset()
            // When
            sut.setPendingMessages(listOf(msg))
            // Then
            assertEquals(listOf(msg), sut.pendingMessagesInRange.value)
        }
    }

    // endregion

    // region date filtering

    @Nested
    inner class DateFiltering {

        @Test
        fun `message at floor boundary is included`() {
            // Given
            sut.setEnabled(true)
            val floor = Date(1000L)
            val atFloor = randomMessage(id = "m1", createdAt = floor, createdLocallyAt = null)
            sut.setPendingMessages(listOf(atFloor))
            sut.advanceOldestLoadedDate(listOf(randomMessage(id = "anchor", createdAt = floor, createdLocallyAt = null)))
            // Then
            assertEquals(listOf(atFloor), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `message just below floor is excluded`() {
            // Given
            sut.setEnabled(true)
            val justBelowFloor = randomMessage(id = "m1", createdAt = Date(999L), createdLocallyAt = null)
            val floorMsg = randomMessage(id = "anchor", createdAt = Date(1000L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(justBelowFloor))
            sut.advanceOldestLoadedDate(listOf(floorMsg))
            // Then
            assertTrue(sut.pendingMessagesInRange.value.isEmpty())
        }

        @Test
        fun `message at ceiling boundary is included`() {
            // Given
            sut.setEnabled(true)
            val ceiling = Date(2000L)
            val atCeiling = randomMessage(id = "m1", createdAt = ceiling, createdLocallyAt = null)
            sut.setPendingMessages(listOf(atCeiling))
            sut.setNewestLoadedDate(ceiling)
            // Then
            assertEquals(listOf(atCeiling), sut.pendingMessagesInRange.value)
        }

        @Test
        fun `message just above ceiling is excluded`() {
            // Given
            sut.setEnabled(true)
            val aboveCeiling = randomMessage(id = "m1", createdAt = Date(2001L), createdLocallyAt = null)
            sut.setPendingMessages(listOf(aboveCeiling))
            sut.setNewestLoadedDate(Date(2000L))
            // Then
            assertTrue(sut.pendingMessagesInRange.value.isEmpty())
        }
    }

    // endregion
}
