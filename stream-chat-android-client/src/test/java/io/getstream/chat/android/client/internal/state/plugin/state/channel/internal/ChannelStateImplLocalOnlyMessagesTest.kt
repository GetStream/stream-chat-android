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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplLocalOnlyMessagesTest : ChannelStateImplTestBase() {

    // region setLocalOnlyMessages — visibility

    @Nested
    inner class SetLocalOnlyMessages {

        @Test
        fun `local-only message appears in messages flow after setLocalOnlyMessages`() = runTest {
            val localOnly = createLocalOnlyMessage(1, timestamp = 1000L)
            channelState.setLocalOnlyMessages(listOf(localOnly))
            assertTrue(channelState.messages.value.any { it.id == localOnly.id })
        }

        @Test
        fun `replacing with empty list removes previously visible local-only messages`() = runTest {
            val localOnly = createLocalOnlyMessage(1, timestamp = 1000L)
            channelState.setLocalOnlyMessages(listOf(localOnly))
            assertTrue(channelState.messages.value.any { it.id == localOnly.id })
            channelState.setLocalOnlyMessages(emptyList())
            assertFalse(channelState.messages.value.any { it.id == localOnly.id })
        }

        @Test
        fun `local-only message is merged before a later regular message`() = runTest {
            val regular = createMessage(2, timestamp = 2000L)
            val localOnly = createLocalOnlyMessage(1, timestamp = 1000L)
            channelState.setMessages(listOf(regular))
            channelState.setLocalOnlyMessages(listOf(localOnly))
            val ids = channelState.messages.value.map { it.id }
            assertTrue(ids.indexOf(localOnly.id) < ids.indexOf(regular.id))
        }

        @Test
        fun `local-only message is merged after an earlier regular message`() = runTest {
            val regular = createMessage(1, timestamp = 1000L)
            val localOnly = createLocalOnlyMessage(2, timestamp = 2000L)
            channelState.setMessages(listOf(regular))
            channelState.setLocalOnlyMessages(listOf(localOnly))
            val ids = channelState.messages.value.map { it.id }
            assertTrue(ids.indexOf(regular.id) < ids.indexOf(localOnly.id))
        }
    }

    // endregion

    // region floor / ceiling filtering

    @Nested
    inner class WindowFiltering {

        @Test
        fun `local-only message below oldest loaded date floor is not shown`() = runTest {
            val localOnly = createLocalOnlyMessage(1, timestamp = 500L)
            val floor = randomMessage(id = "floor", createdAt = Date(1000L), createdLocallyAt = null)
            channelState.setLocalOnlyMessages(listOf(localOnly))
            channelState.paginationManager.setOldestMessage(floor)
            assertFalse(channelState.messages.value.any { it.id == localOnly.id })
        }

        @Test
        fun `local-only message above newest loaded date ceiling is not shown`() = runTest {
            val localOnly = createLocalOnlyMessage(1, timestamp = 3000L)
            val ceiling = randomMessage(id = "ceiling", createdAt = Date(2000L), createdLocallyAt = null)
            channelState.setLocalOnlyMessages(listOf(localOnly))
            channelState.paginationManager.setNewestMessage(ceiling)
            assertFalse(channelState.messages.value.any { it.id == localOnly.id })
        }

        @Test
        fun `local-only message within both floor and ceiling is shown`() = runTest {
            val localOnly = createLocalOnlyMessage(1, timestamp = 2000L)
            val floor = randomMessage(id = "f", createdAt = Date(1000L), createdLocallyAt = null)
            val ceiling = randomMessage(id = "c", createdAt = Date(3000L), createdLocallyAt = null)
            channelState.setLocalOnlyMessages(listOf(localOnly))
            channelState.paginationManager.setOldestMessage(floor)
            channelState.paginationManager.setNewestMessage(ceiling)
            assertTrue(channelState.messages.value.any { it.id == localOnly.id })
        }

        @Test
        fun `local-only message at exact floor boundary is shown`() = runTest {
            val localOnly = createLocalOnlyMessage(1, timestamp = 1000L)
            val floor = randomMessage(id = "f", createdAt = Date(1000L), createdLocallyAt = null)
            channelState.setLocalOnlyMessages(listOf(localOnly))
            channelState.paginationManager.setOldestMessage(floor)
            assertTrue(channelState.messages.value.any { it.id == localOnly.id })
        }

        @Test
        fun `removing ceiling reveals previously hidden local-only message`() = runTest {
            val localOnly = createLocalOnlyMessage(1, timestamp = 5000L)
            val ceiling = randomMessage(id = "c", createdAt = Date(1000L), createdLocallyAt = null)
            channelState.setLocalOnlyMessages(listOf(localOnly))
            channelState.paginationManager.setNewestMessage(ceiling)
            assertFalse(channelState.messages.value.any { it.id == localOnly.id })
            channelState.paginationManager.setNewestMessage(null)
            assertTrue(channelState.messages.value.any { it.id == localOnly.id })
        }

        @Test
        fun `null floor means no floor restriction, all local-only messages are visible`() = runTest {
            val localOnly = createLocalOnlyMessage(1, timestamp = 1L)
            channelState.setLocalOnlyMessages(listOf(localOnly))
            assertTrue(channelState.messages.value.any { it.id == localOnly.id })
        }
    }

    // endregion

    // region upsertMessage — ephemeral path

    @Nested
    inner class UpsertMessageEphemeral {

        @Test
        fun `upsertMessage with ephemeral message adds it to local-only messages`() = runTest {
            val ephemeral = createLocalOnlyMessage(1, timestamp = 1000L)
            channelState.upsertMessage(ephemeral)
            assertTrue(channelState.messages.value.any { it.id == ephemeral.id })
        }

        @Test
        fun `upsertMessage with ephemeral message updates existing entry without duplicating it`() = runTest {
            val ephemeral = createLocalOnlyMessage(1, timestamp = 1000L)
            // Seed local-only state first, then update via upsertMessage
            channelState.setLocalOnlyMessages(listOf(ephemeral))
            channelState.upsertMessage(ephemeral.copy(text = "Updated"))
            val found = channelState.messages.value.find { it.id == ephemeral.id }
            assertEquals("Updated", found?.text)
            assertEquals(1, channelState.messages.value.count { it.id == ephemeral.id })
        }
    }

    // endregion

    // region deleteMessage — local-only cleanup

    @Nested
    inner class DeleteMessage {

        @Test
        fun `deleteMessage removes ephemeral message from local-only messages`() = runTest {
            val ephemeral = createLocalOnlyMessage(1, timestamp = 1000L)
            channelState.setLocalOnlyMessages(listOf(ephemeral))
            assertTrue(channelState.messages.value.any { it.id == ephemeral.id })
            channelState.deleteMessage(ephemeral.id)
            assertFalse(channelState.messages.value.any { it.id == ephemeral.id })
        }
    }

    // endregion
}
