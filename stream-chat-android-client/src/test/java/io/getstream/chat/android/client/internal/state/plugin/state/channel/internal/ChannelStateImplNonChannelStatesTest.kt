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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class ChannelStateImplNonChannelStatesTest : ChannelStateImplTestBase() {

    // region RecoveryNeeded

    @Nested
    inner class SetRecoveryNeeded {

        @Test
        fun `recoveryNeeded should default to false`() = runTest {
            assertFalse(channelState.recoveryNeeded)
        }

        @Test
        fun `setRecoveryNeeded should set to true`() = runTest {
            channelState.setRecoveryNeeded(true)
            assertTrue(channelState.recoveryNeeded)
        }

        @Test
        fun `setRecoveryNeeded should set to false`() = runTest {
            channelState.setRecoveryNeeded(true)
            channelState.setRecoveryNeeded(false)
            assertFalse(channelState.recoveryNeeded)
        }
    }

    // endregion

    // region InsideSearch

    @Nested
    inner class SetInsideSearch {

        @Test
        fun `insideSearch should default to false`() = runTest {
            assertFalse(channelState.insideSearch.value)
        }

        @Test
        fun `setInsideSearch should set to true`() = runTest {
            channelState.setInsideSearch(true)
            assertTrue(channelState.insideSearch.value)
        }

        @Test
        fun `setInsideSearch should set to false`() = runTest {
            channelState.setInsideSearch(true)
            channelState.setInsideSearch(false)
            assertFalse(channelState.insideSearch.value)
        }
    }

    // endregion

    // region CacheLatestMessages

    @Nested
    inner class CacheLatestMessages {

        @Test
        fun `cacheLatestMessages should cache current messages`() = runTest {
            // given
            val messages = createMessages(3)
            channelState.setMessages(messages)
            // when
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList()) // Clear main messages
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals(3, cachedMessages.size)
        }

        @Test
        fun `cacheLatestMessages should limit cached messages to 25`() = runTest {
            // given - create more than CACHED_LATEST_MESSAGES_LIMIT (25) messages
            val messages = createMessages(30)
            channelState.setMessages(messages)
            // when
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList())
            // then - only last 25 messages should be cached
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals(25, cachedMessages.size)
        }

        @Test
        fun `cacheLatestMessages should keep the latest messages when limiting`() = runTest {
            // given
            val messages = createMessages(30)
            channelState.setMessages(messages)
            // when
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList())
            // then - should have the last 25 messages (indices 6-30)
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            val lastOriginalMessage = messages.last()
            assertEquals(lastOriginalMessage.id, cachedMessages.last().id)
        }

        @Test
        fun `cacheLatestMessages should handle empty messages`() = runTest {
            // given - no messages
            // when
            channelState.cacheLatestMessages()
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertTrue(cachedMessages.isEmpty())
        }
    }

    // endregion

    // region ClearCachedLatestMessages

    @Nested
    inner class ClearCachedLatestMessages {

        @Test
        fun `clearCachedLatestMessages should clear cached messages`() = runTest {
            // given
            val messages = createMessages(3)
            channelState.setMessages(messages)
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList())
            // when
            channelState.clearCachedLatestMessages()
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertTrue(cachedMessages.isEmpty())
        }

        @Test
        fun `clearCachedLatestMessages should handle already empty cache`() = runTest {
            // when
            channelState.clearCachedLatestMessages()
            // then - should not throw
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertTrue(cachedMessages.isEmpty())
        }
    }

    // endregion

    // region TrimOldestMessages

    @Nested
    inner class TrimOldestMessages {

        @Test
        fun `trimOldestMessages should not trim when messageLimit is null`() = runTest {
            // given - default channelState has messageLimit = null
            val messages = createMessages(100)
            channelState.setMessages(messages)
            // when
            channelState.trimOldestMessages()
            // then - no trimming
            assertEquals(100, channelState.messages.value.size)
        }

        @Test
        fun `trimOldestMessages should not trim when under limit plus buffer`() = runTest {
            // given - messageLimit = 50, buffer = 30, total threshold = 80
            val stateWithLimit = createChannelStateWithLimit(50)
            val messages = createMessages(79) // under 50 + 30 = 80
            stateWithLimit.setMessages(messages)
            // when
            stateWithLimit.trimOldestMessages()
            // then - no trimming
            assertEquals(79, stateWithLimit.messages.value.size)
        }

        @Test
        fun `trimOldestMessages should trim when over limit plus buffer`() = runTest {
            // given - messageLimit = 50, buffer = 30, total threshold = 80
            val stateWithLimit = createChannelStateWithLimit(50)
            val messages = createMessages(81) // over 50 + 30 = 80
            stateWithLimit.setMessages(messages)
            // when
            stateWithLimit.trimOldestMessages()
            // then - should trim to messageLimit (50), keeping newest
            assertEquals(50, stateWithLimit.messages.value.size)
        }

        @Test
        fun `trimOldestMessages should keep the newest messages`() = runTest {
            // given
            val stateWithLimit = createChannelStateWithLimit(50)
            val messages = createMessages(81)
            stateWithLimit.setMessages(messages)
            val lastMessage = messages.last()
            // when
            stateWithLimit.trimOldestMessages()
            // then - the last message should still be present
            assertEquals(lastMessage.id, stateWithLimit.messages.value.last().id)
        }

        @Test
        fun `trimOldestMessages should set endOfOlderMessages to false`() = runTest {
            // given
            val stateWithLimit = createChannelStateWithLimit(50)
            stateWithLimit.paginationManager.setEndOfOlderMessages(true)
            val messages = createMessages(81)
            stateWithLimit.setMessages(messages)
            // when
            stateWithLimit.trimOldestMessages()
            // then
            assertFalse(stateWithLimit.endOfOlderMessages.value)
        }

        @Test
        fun `trimOldestMessages at exact threshold should not trim`() = runTest {
            // given - at exactly limit + buffer = 80
            val stateWithLimit = createChannelStateWithLimit(50)
            val messages = createMessages(80)
            stateWithLimit.setMessages(messages)
            // when
            stateWithLimit.trimOldestMessages()
            // then - no trimming at exact threshold
            assertEquals(80, stateWithLimit.messages.value.size)
        }
    }

    // endregion

    // region TrimNewestMessages

    @Nested
    inner class TrimNewestMessages {

        @Test
        fun `trimNewestMessages should not trim when messageLimit is null`() = runTest {
            // given - default channelState has messageLimit = null
            val messages = createMessages(100)
            channelState.setMessages(messages)
            // when
            channelState.trimNewestMessages()
            // then - no trimming
            assertEquals(100, channelState.messages.value.size)
        }

        @Test
        fun `trimNewestMessages should not trim when under limit plus buffer`() = runTest {
            // given
            val stateWithLimit = createChannelStateWithLimit(50)
            val messages = createMessages(79)
            stateWithLimit.setMessages(messages)
            // when
            stateWithLimit.trimNewestMessages()
            // then - no trimming
            assertEquals(79, stateWithLimit.messages.value.size)
        }

        @Test
        fun `trimNewestMessages should trim when over limit plus buffer`() = runTest {
            // given
            val stateWithLimit = createChannelStateWithLimit(50)
            val messages = createMessages(81)
            stateWithLimit.setMessages(messages)
            // when
            stateWithLimit.trimNewestMessages()
            // then - should trim to messageLimit (50), keeping oldest
            assertEquals(50, stateWithLimit.messages.value.size)
        }

        @Test
        fun `trimNewestMessages should keep the oldest messages`() = runTest {
            // given
            val stateWithLimit = createChannelStateWithLimit(50)
            val messages = createMessages(81)
            stateWithLimit.setMessages(messages)
            val firstMessage = messages.first()
            // when
            stateWithLimit.trimNewestMessages()
            // then - the first message should still be present
            assertEquals(firstMessage.id, stateWithLimit.messages.value.first().id)
        }

        @Test
        fun `trimNewestMessages should set endOfNewerMessages to false`() = runTest {
            // given
            val stateWithLimit = createChannelStateWithLimit(50)
            stateWithLimit.paginationManager.setEndOfNewerMessages(true)
            val messages = createMessages(81)
            stateWithLimit.setMessages(messages)
            // when
            stateWithLimit.trimNewestMessages()
            // then
            assertFalse(stateWithLimit.endOfNewerMessages.value)
        }

        @Test
        fun `trimNewestMessages should set insideSearch to true`() = runTest {
            // given
            val stateWithLimit = createChannelStateWithLimit(50)
            stateWithLimit.setInsideSearch(false)
            val messages = createMessages(81)
            stateWithLimit.setMessages(messages)
            // when
            stateWithLimit.trimNewestMessages()
            // then
            assertTrue(stateWithLimit.insideSearch.value)
        }

        @Test
        fun `trimNewestMessages should cache messages before trimming`() = runTest {
            // given
            val stateWithLimit = createChannelStateWithLimit(50)
            val messages = createMessages(81)
            stateWithLimit.setMessages(messages)
            val lastMessage = messages.last()
            // when
            stateWithLimit.trimNewestMessages()
            // then - the latest messages should be cached (up to 25)
            val cachedMessages = stateWithLimit.toChannel().cachedLatestMessages
            assertTrue(cachedMessages.isNotEmpty())
            assertEquals(lastMessage.id, cachedMessages.last().id)
        }
    }

    // endregion

    // region Destroy

    @Nested
    inner class Destroy {

        @Test
        fun `destroy should reset all state to defaults`() = runTest {
            // given - populate various state
            channelState.setMessages(createMessages(5))
            channelState.paginationManager.setEndOfOlderMessages(true)
            channelState.paginationManager.setEndOfNewerMessages(false)
            channelState.setRecoveryNeeded(true)
            channelState.setInsideSearch(true)
            channelState.setHidden(true)
            channelState.setMuted(true)
            // when
            channelState.destroy()
            // then
            assertTrue(channelState.messages.value.isEmpty())
            assertFalse(channelState.loading.value)
            assertFalse(channelState.loadingOlderMessages.value)
            assertFalse(channelState.loadingNewerMessages.value)
            assertFalse(channelState.endOfOlderMessages.value)
            assertTrue(channelState.endOfNewerMessages.value) // defaults to true
            assertFalse(channelState.recoveryNeeded)
            assertFalse(channelState.insideSearch.value)
            assertFalse(channelState.hidden.value)
            assertFalse(channelState.muted.value)
            assertTrue(channelState.pinnedMessages.value.isEmpty())
            assertTrue(channelState.reads.value.isEmpty())
            assertEquals(0, channelState.membersCount.value)
            assertTrue(channelState.members.value.isEmpty())
            assertEquals(0, channelState.watcherCount.value)
            assertTrue(channelState.watchers.value.isEmpty())
        }
    }

    // endregion

    private fun createChannelStateWithLimit(limit: Int): ChannelStateImpl {
        return ChannelStateImpl(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            currentUser = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            mutedUsers = MutableStateFlow(emptyList()),
            liveLocations = MutableStateFlow(emptyList()),
            messageLimit = limit,
        )
    }
}
