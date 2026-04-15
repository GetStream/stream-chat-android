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

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.result.Error
import io.getstream.result.Result
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MessagesPaginationManagerImplTest {

    private lateinit var sut: MessagesPaginationManagerImpl
    private val failure = Result.Failure(Error.ThrowableError("test", RuntimeException()))

    @BeforeEach
    fun setUp() {
        sut = MessagesPaginationManagerImpl()
    }

    // region Initial state

    @Test
    fun `initial state should have hasLoadedAllNextMessages = true`() {
        assertTrue(sut.state.value.hasLoadedAllNextMessages)
    }

    @Test
    fun `initial state should have hasLoadedAllPreviousMessages = false`() {
        assertFalse(sut.state.value.hasLoadedAllPreviousMessages)
    }

    @Test
    fun `initial state should have no loading flags set`() {
        val state = sut.state.value
        assertFalse(state.isLoadingNextMessages)
        assertFalse(state.isLoadingPreviousMessages)
        assertFalse(state.isLoadingMiddleMessages)
    }

    @Test
    fun `initial state should have null oldest and newest messages`() {
        assertNull(sut.state.value.oldestMessage)
        assertNull(sut.state.value.newestMessage)
    }

    // endregion

    // region begin()

    @Nested
    inner class Begin {

        @Test
        fun `begin with no pagination should reset to initial state`() {
            // given - dirty state
            sut.setEndOfOlderMessages(true)
            sut.setEndOfNewerMessages(false)
            // when
            sut.begin(QueryChannelRequest().withMessages(30))
            // then
            val state = sut.state.value
            assertTrue(state.hasLoadedAllNextMessages)
            assertFalse(state.hasLoadedAllPreviousMessages)
            assertFalse(state.isLoadingMessages)
        }

        @Test
        fun `begin with older pagination should set isLoadingPreviousMessages`() {
            // when
            sut.begin(QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30))
            // then
            assertTrue(sut.state.value.isLoadingPreviousMessages)
        }

        @Test
        fun `begin with older pagination should not touch other loading flags`() {
            // when
            sut.begin(QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30))
            // then
            val state = sut.state.value
            assertFalse(state.isLoadingNextMessages)
            assertFalse(state.isLoadingMiddleMessages)
        }

        @Test
        fun `begin with newer pagination should set isLoadingNextMessages`() {
            // when
            sut.begin(QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "msgId", 30))
            // then
            assertTrue(sut.state.value.isLoadingNextMessages)
        }

        @Test
        fun `begin with newer pagination should not touch other loading flags`() {
            // when
            sut.begin(QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "msgId", 30))
            // then
            val state = sut.state.value
            assertFalse(state.isLoadingPreviousMessages)
            assertFalse(state.isLoadingMiddleMessages)
        }

        @Test
        fun `begin with around pagination should set isLoadingMiddleMessages`() {
            // when
            sut.begin(QueryChannelRequest().withMessages(Pagination.AROUND_ID, "msgId", 30))
            // then
            assertTrue(sut.state.value.isLoadingMiddleMessages)
        }

        @Test
        fun `begin with around pagination should set hasLoadedAllNextMessages to false`() {
            // when
            sut.begin(QueryChannelRequest().withMessages(Pagination.AROUND_ID, "msgId", 30))
            // then
            assertFalse(sut.state.value.hasLoadedAllNextMessages)
        }

        @Test
        fun `begin with around pagination should not touch other loading flags`() {
            // when
            sut.begin(QueryChannelRequest().withMessages(Pagination.AROUND_ID, "msgId", 30))
            // then
            val state = sut.state.value
            assertFalse(state.isLoadingPreviousMessages)
            assertFalse(state.isLoadingNextMessages)
        }
    }

    // endregion

    // region end() - failure

    @Nested
    inner class EndFailure {

        @Test
        fun `end with failure should clear all loading flags`() {
            // given
            sut.begin(QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30))
            // when
            sut.end(
                query = QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30),
                result = failure,
            )
            // then
            val state = sut.state.value
            assertFalse(state.isLoadingPreviousMessages)
            assertFalse(state.isLoadingNextMessages)
            assertFalse(state.isLoadingMiddleMessages)
        }

        @Test
        fun `end with failure should preserve hasLoadedAllPreviousMessages`() {
            // given
            sut.setEndOfOlderMessages(true)
            // when
            sut.end(
                query = QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30),
                result = failure,
            )
            // then
            assertTrue(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `end with failure should preserve hasLoadedAllNextMessages`() {
            // given
            sut.setEndOfNewerMessages(false)
            // when
            sut.end(
                query = QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "msgId", 30),
                result = failure,
            )
            // then
            assertFalse(sut.state.value.hasLoadedAllNextMessages)
        }
    }

    // endregion

    // region end() - success - older

    @Nested
    inner class EndSuccessOlder {

        private val query = QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30)

        @Test
        fun `full page should set hasLoadedAllPreviousMessages to false`() {
            // given - full page (30 messages == limit)
            val messages = (1..30).map { randomMessage(id = "m$it") }
            val channel = randomChannel(messages = messages)
            // when
            sut.end(query, Result.Success(channel))
            // then
            assertFalse(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `partial page should set hasLoadedAllPreviousMessages to true`() {
            // given - partial page (fewer messages than limit)
            val messages = (1..10).map { randomMessage(id = "m$it") }
            val channel = randomChannel(messages = messages)
            // when
            sut.end(query, Result.Success(channel))
            // then
            assertTrue(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `empty page should set hasLoadedAllPreviousMessages to true`() {
            // when
            sut.end(query, Result.Success(randomChannel(messages = emptyList())))
            // then
            assertTrue(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `should set oldestMessage to first message in response`() {
            // given
            val messages = (1..5).map { randomMessage(id = "m$it") }
            val channel = randomChannel(messages = messages)
            // when
            sut.end(query, Result.Success(channel))
            // then
            assertEquals("m1", sut.state.value.oldestMessage?.id)
        }

        @Test
        fun `should clear all loading flags on success`() {
            // given
            sut.begin(query)
            // when
            sut.end(query, Result.Success(randomChannel(messages = emptyList())))
            // then
            val state = sut.state.value
            assertFalse(state.isLoadingPreviousMessages)
            assertFalse(state.isLoadingNextMessages)
            assertFalse(state.isLoadingMiddleMessages)
        }

        @Test
        fun `should not change hasLoadedAllNextMessages`() {
            // given - currently at the latest page
            assertTrue(sut.state.value.hasLoadedAllNextMessages)
            // when
            sut.end(query, Result.Success(randomChannel(messages = emptyList())))
            // then - not changed
            assertTrue(sut.state.value.hasLoadedAllNextMessages)
        }
    }

    // endregion

    // region end() - success - newer

    @Nested
    inner class EndSuccessNewer {

        private val query = QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "msgId", 30)

        @Test
        fun `full page should set hasLoadedAllNextMessages to false`() {
            // given - full page
            val messages = (1..30).map { randomMessage(id = "m$it") }
            val channel = randomChannel(messages = messages)
            // when
            sut.end(query, Result.Success(channel))
            // then
            assertFalse(sut.state.value.hasLoadedAllNextMessages)
        }

        @Test
        fun `full page should set newestMessage to last message in response`() {
            // given
            val messages = (1..30).map { randomMessage(id = "m$it") }
            val channel = randomChannel(messages = messages)
            // when
            sut.end(query, Result.Success(channel))
            // then
            assertEquals("m30", sut.state.value.newestMessage?.id)
        }

        @Test
        fun `partial page should set hasLoadedAllNextMessages to true`() {
            // given - partial page
            val messages = (1..10).map { randomMessage(id = "m$it") }
            val channel = randomChannel(messages = messages)
            // when
            sut.end(query, Result.Success(channel))
            // then
            assertTrue(sut.state.value.hasLoadedAllNextMessages)
        }

        @Test
        fun `partial page should clear newestMessage`() {
            // given - partial page means we reached the end, no ceiling needed
            val messages = (1..10).map { randomMessage(id = "m$it") }
            val channel = randomChannel(messages = messages)
            // when
            sut.end(query, Result.Success(channel))
            // then
            assertNull(sut.state.value.newestMessage)
        }

        @Test
        fun `should clear all loading flags on success`() {
            // given
            sut.begin(query)
            // when
            sut.end(query, Result.Success(randomChannel(messages = emptyList())))
            // then
            val state = sut.state.value
            assertFalse(state.isLoadingPreviousMessages)
            assertFalse(state.isLoadingNextMessages)
            assertFalse(state.isLoadingMiddleMessages)
        }
    }

    // endregion

    // region end() - success - around

    @Nested
    inner class EndSuccessAround {

        private val query = QueryChannelRequest().withMessages(Pagination.AROUND_ID, "msgId", 30)

        @Test
        fun `should always set hasLoadedAllNextMessages to false`() {
            // given - even a partial page
            val messages = (1..5).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            assertFalse(sut.state.value.hasLoadedAllNextMessages)
        }

        @Test
        fun `should always set hasLoadedAllPreviousMessages to false`() {
            // given
            val messages = (1..5).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            assertFalse(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `should set oldestMessage to first message in response`() {
            // given
            val messages = (1..5).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            assertEquals("m1", sut.state.value.oldestMessage?.id)
        }

        @Test
        fun `should set newestMessage to last message in response`() {
            // given
            val messages = (1..5).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            assertEquals("m5", sut.state.value.newestMessage?.id)
        }

        @Test
        fun `should clear all loading flags on success`() {
            // given
            sut.begin(query)
            // when
            val messages = (1..5).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            val state = sut.state.value
            assertFalse(state.isLoadingPreviousMessages)
            assertFalse(state.isLoadingNextMessages)
            assertFalse(state.isLoadingMiddleMessages)
        }

        @Test
        fun `should set both end flags to false even when previously true`() {
            // given - both flags were set
            sut.setEndOfOlderMessages(true)
            sut.setEndOfNewerMessages(true)
            // when
            val messages = (1..10).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            assertFalse(sut.state.value.hasLoadedAllNextMessages)
            assertFalse(sut.state.value.hasLoadedAllPreviousMessages)
        }
    }

    // endregion

    // region end() - success - no pagination

    @Nested
    inner class EndSuccessNoPagination {

        private val query = QueryChannelRequest().withMessages(30)

        @Test
        fun `should set hasLoadedAllNextMessages to true`() {
            // when
            sut.end(query, Result.Success(randomChannel(messages = emptyList())))
            // then
            assertTrue(sut.state.value.hasLoadedAllNextMessages)
        }

        @Test
        fun `full page should set hasLoadedAllPreviousMessages to false`() {
            // given - full page (30 messages == limit)
            val messages = (1..30).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            assertFalse(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `partial page should set hasLoadedAllPreviousMessages to true`() {
            // given - fewer than limit
            val messages = (1..10).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            assertTrue(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `should clear newestMessage ceiling`() {
            // given - simulate a mid-page state with a ceiling
            val olderQuery = QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "msgId", 30)
            sut.end(olderQuery, Result.Success(randomChannel(messages = (1..30).map { randomMessage(id = "m$it") })))
            // when - initial load resets state
            sut.end(query, Result.Success(randomChannel(messages = emptyList())))
            // then
            assertNull(sut.state.value.newestMessage)
        }

        @Test
        fun `should set oldestMessage to first message in response`() {
            // given
            val messages = (1..5).map { randomMessage(id = "m$it") }
            sut.end(query, Result.Success(randomChannel(messages = messages)))
            // then
            assertEquals("m1", sut.state.value.oldestMessage?.id)
        }
    }

    // endregion

    // region setOldestMessage

    @Nested
    inner class SetOldestMessage {

        @Test
        fun `setOldestMessage should update oldestMessage`() {
            // given
            val message = randomMessage(id = "old")
            // when
            sut.setOldestMessage(message)
            // then
            assertEquals("old", sut.state.value.oldestMessage?.id)
        }

        @Test
        fun `setOldestMessage with null should clear oldestMessage`() {
            // given
            sut.setOldestMessage(randomMessage(id = "old"))
            // when
            sut.setOldestMessage(null)
            // then
            assertNull(sut.state.value.oldestMessage)
        }
    }

    // endregion

    // region setNewestMessage

    @Nested
    inner class SetNewestMessage {

        @Test
        fun `setNewestMessage should update newestMessage`() {
            // given
            val message = randomMessage(id = "new")
            // when
            sut.setNewestMessage(message)
            // then
            assertEquals("new", sut.state.value.newestMessage?.id)
        }

        @Test
        fun `setNewestMessage with null should clear newestMessage`() {
            // given
            sut.setNewestMessage(randomMessage(id = "new"))
            // when
            sut.setNewestMessage(null)
            // then
            assertNull(sut.state.value.newestMessage)
        }
    }

    // endregion

    // region setEndOfOlderMessages

    @Nested
    inner class SetEndOfOlderMessages {

        @Test
        fun `should set hasLoadedAllPreviousMessages to true`() {
            // when
            sut.setEndOfOlderMessages(true)
            // then
            assertTrue(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `should set hasLoadedAllPreviousMessages to false`() {
            // given
            sut.setEndOfOlderMessages(true)
            // when
            sut.setEndOfOlderMessages(false)
            // then
            assertFalse(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `should not affect hasLoadedAllNextMessages`() {
            // given
            val original = sut.state.value.hasLoadedAllNextMessages
            // when
            sut.setEndOfOlderMessages(true)
            // then
            assertEquals(original, sut.state.value.hasLoadedAllNextMessages)
        }
    }

    // endregion

    // region setEndOfNewerMessages

    @Nested
    inner class SetEndOfNewerMessages {

        @Test
        fun `should set hasLoadedAllNextMessages to false`() {
            // when
            sut.setEndOfNewerMessages(false)
            // then
            assertFalse(sut.state.value.hasLoadedAllNextMessages)
        }

        @Test
        fun `should set hasLoadedAllNextMessages to true`() {
            // given
            sut.setEndOfNewerMessages(false)
            // when
            sut.setEndOfNewerMessages(true)
            // then
            assertTrue(sut.state.value.hasLoadedAllNextMessages)
        }

        @Test
        fun `setting to true should clear newestMessage ceiling`() {
            // given - a ceiling was set (mid-page state)
            sut.setNewestMessage(randomMessage(id = "ceiling"))
            // when
            sut.setEndOfNewerMessages(true)
            // then
            assertNull(sut.state.value.newestMessage)
        }

        @Test
        fun `setting to false should preserve existing newestMessage`() {
            // given
            val ceiling = randomMessage(id = "ceiling")
            sut.setNewestMessage(ceiling)
            // when
            sut.setEndOfNewerMessages(false)
            // then
            assertEquals("ceiling", sut.state.value.newestMessage?.id)
        }

        @Test
        fun `should not affect hasLoadedAllPreviousMessages`() {
            // given
            val original = sut.state.value.hasLoadedAllPreviousMessages
            // when
            sut.setEndOfNewerMessages(false)
            // then
            assertEquals(original, sut.state.value.hasLoadedAllPreviousMessages)
        }
    }

    // endregion

    // region reset

    @Nested
    inner class Reset {

        @Test
        fun `reset should restore hasLoadedAllNextMessages to true`() {
            // given
            sut.setEndOfNewerMessages(false)
            // when
            sut.reset()
            // then
            assertTrue(sut.state.value.hasLoadedAllNextMessages)
        }

        @Test
        fun `reset should restore hasLoadedAllPreviousMessages to false`() {
            // given
            sut.setEndOfOlderMessages(true)
            // when
            sut.reset()
            // then
            assertFalse(sut.state.value.hasLoadedAllPreviousMessages)
        }

        @Test
        fun `reset should clear oldest and newest messages`() {
            // given
            sut.setOldestMessage(randomMessage())
            sut.setNewestMessage(randomMessage())
            // when
            sut.reset()
            // then
            assertNull(sut.state.value.oldestMessage)
            assertNull(sut.state.value.newestMessage)
        }

        @Test
        fun `reset should clear all loading flags`() {
            // given - simulate loading state
            sut.begin(QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30))
            // when
            sut.reset()
            // then
            val state = sut.state.value
            assertFalse(state.isLoadingPreviousMessages)
            assertFalse(state.isLoadingNextMessages)
            assertFalse(state.isLoadingMiddleMessages)
        }
    }

    // endregion
}
