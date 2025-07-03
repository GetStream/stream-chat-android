/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.messages.translations

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
internal class MessageOriginalTranslationsStoreTest {

    private val testMessageId1 = "test-message-id-1"
    private val testMessageId2 = "test-message-id-2"

    private lateinit var translationsStore: MessageOriginalTranslationsStore

    @Before
    fun setUp() {
        // Initialize the store before each test
        translationsStore = MessageOriginalTranslationsStore.forChannel("cid")
    }

    @After
    fun tearDown() {
        // Clear the store after each test to ensure a clean state
        translationsStore.clear()
    }

    @Test
    fun `originalTextMessageIds should initially contain an empty set`() = runTest {
        translationsStore.originalTextMessageIds.test {
            Assertions.assertEquals(emptySet<String>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shouldShowOriginalText should return false for a message that is not in the store`() {
        Assertions.assertFalse(translationsStore.shouldShowOriginalText(testMessageId1))
    }

    @Test
    fun `showOriginalText should add the message ID to the store`() = runTest {
        translationsStore.originalTextMessageIds.test {
            // Initial state is empty
            Assertions.assertEquals(emptySet<String>(), awaitItem())

            // Show original text for a message
            translationsStore.showOriginalText(testMessageId1)

            // Verify that the message ID was added
            Assertions.assertEquals(setOf(testMessageId1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hideOriginalText should remove the message ID from the store`() = runTest {
        // First add the message ID
        translationsStore.showOriginalText(testMessageId1)

        translationsStore.originalTextMessageIds.test {
            // Verify initial state
            Assertions.assertEquals(setOf(testMessageId1), awaitItem())

            // Hide original text for the message
            translationsStore.hideOriginalText(testMessageId1)

            // Verify that the message ID was removed
            Assertions.assertEquals(emptySet<String>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hideOriginalText should do nothing if the message ID is not in the store`() = runTest {
        translationsStore.originalTextMessageIds.test {
            // Initial state is empty
            Assertions.assertEquals(emptySet<String>(), awaitItem())

            // Try to hide original text for a message that's not in the store
            translationsStore.hideOriginalText(testMessageId1)

            // Verify that the state did not change
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleOriginalText should add the message ID if it's not in the store`() = runTest {
        translationsStore.originalTextMessageIds.test {
            // Initial state is empty
            Assertions.assertEquals(emptySet<String>(), awaitItem())

            // Toggle original text for a message
            translationsStore.toggleOriginalText(testMessageId1)

            // Verify that the message ID was added
            Assertions.assertEquals(setOf(testMessageId1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleOriginalText should remove the message ID if it's already in the store`() = runTest {
        // First add the message ID
        translationsStore.showOriginalText(testMessageId1)

        translationsStore.originalTextMessageIds.test {
            // Verify initial state
            Assertions.assertEquals(setOf(testMessageId1), awaitItem())

            // Toggle original text for the message
            translationsStore.toggleOriginalText(testMessageId1)

            // Verify that the message ID was removed
            Assertions.assertEquals(emptySet<String>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shouldShowOriginalText should return true for a message that is in the store`() = runTest {
        // Add the message ID to the store
        translationsStore.showOriginalText(testMessageId1)

        // Verify that shouldShowOriginalText returns true for the message
        Assertions.assertTrue(translationsStore.shouldShowOriginalText(testMessageId1))
    }

    @Test
    fun `store should handle multiple message IDs correctly`() = runTest {
        // Add the first message ID
        translationsStore.showOriginalText(testMessageId1)

        translationsStore.originalTextMessageIds.test {
            // Verify initial state
            Assertions.assertEquals(setOf(testMessageId1), awaitItem())

            // Add another message ID
            translationsStore.showOriginalText(testMessageId2)

            // Verify that both message IDs are in the store
            Assertions.assertEquals(setOf(testMessageId1, testMessageId2), awaitItem())

            // Remove one message ID
            translationsStore.hideOriginalText(testMessageId1)

            // Verify that only the second message ID remains
            Assertions.assertEquals(setOf(testMessageId2), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear should remove all message IDs from the store`() = runTest {
        // Add some message IDs
        translationsStore.showOriginalText(testMessageId1)
        translationsStore.showOriginalText(testMessageId2)

        translationsStore.originalTextMessageIds.test {
            // Verify initial state
            Assertions.assertEquals(setOf(testMessageId1, testMessageId2), awaitItem())

            // Clear the store
            translationsStore.clear()

            // Verify that all message IDs were removed
            Assertions.assertEquals(emptySet<String>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
