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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import app.cash.turbine.test
import io.getstream.chat.android.compose.ui.theme.PollFeatureConfig
import io.getstream.chat.android.compose.ui.theme.PollsConfig
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.ui.common.utils.PollsConstants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class CreatePollViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val defaultConfig = PollsConfig()

    // updateQuestion tests

    @Test
    fun `Given a new question When updateQuestion is called Then state should contain the new question`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)
        val newQuestion = "What is your favorite color?"

        viewModel.updateQuestion(newQuestion)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(newQuestion, state.question)
        }
    }

    @Test
    fun `Given empty question When updateQuestion with empty string Then state should have empty question`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("Initial question")
        viewModel.updateQuestion("")

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.question)
        }
    }

    // updateOptions tests

    @Test
    fun `Given new options When updateOptions is called Then state should contain the new options`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)
        val newOptions = listOf(
            PollOptionItem(title = "Red", key = "1"),
            PollOptionItem(title = "Blue", key = "2"),
        )

        viewModel.updateOptions(newOptions)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(newOptions, state.optionItemList)
        }
    }

    @Test
    fun `Given empty options list When updateOptions is called Then state should have empty options`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateOptions(emptyList())

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(emptyList<PollOptionItem>(), state.optionItemList)
        }
    }

    // Feature toggle tests

    @Test
    fun `Given multiple votes enabled When updateMultipleVotes is called Then state should reflect it`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateMultipleVotes(true)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.multipleVotesEnabled)
        }
    }

    @Test
    fun `Given multiple votes disabled When updateMultipleVotes is called Then limitVotes should be reset`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateMultipleVotes(true)
        viewModel.updateLimitVotes(true)
        viewModel.updateMaxVotes("5")
        viewModel.updateMultipleVotes(false)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.multipleVotesEnabled)
            assertFalse(state.limitVotesEnabled)
            assertEquals("5", state.maxVotesPerUserText)
        }
    }

    @Test
    fun `Given anonymous poll enabled When updateAnonymousPoll is called Then state should reflect it`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateAnonymousPoll(true)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.anonymousPollEnabled)
        }
    }

    // updateLimitVotes tests

    @Test
    fun `Given limitVotes enabled When updateLimitVotes(true) Then state should reflect it`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateMultipleVotes(true)
        viewModel.updateLimitVotes(true)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.limitVotesEnabled)
        }
    }

    @Test
    fun `Given limitVotes disabled When updateLimitVotes(false) Then maxVotesPerUser should be preserved`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateMultipleVotes(true)
        viewModel.updateLimitVotes(true)
        viewModel.updateMaxVotes("5")
        viewModel.updateLimitVotes(false)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.limitVotesEnabled)
            assertEquals("5", state.maxVotesPerUserText)
        }
    }

    // updateMaxVotes tests

    @Test
    fun `Given a value When updateMaxVotes is called Then maxVotesPerUser should be that value`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateMaxVotes("5")

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("5", state.maxVotesPerUserText)
        }
    }

    // reset tests

    @Test
    fun `Given modified state When reset is called Then state should return to initial values`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        // Modify the state
        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "Red", key = "1"),
                PollOptionItem(title = "Blue", key = "2"),
            ),
        )
        viewModel.updateMultipleVotes(true)
        viewModel.updateLimitVotes(true)
        viewModel.updateMaxVotes("5")

        // Reset the state
        viewModel.reset()

        // Verify state is back to initial values
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.question)
            assertEquals(emptyList<PollOptionItem>(), state.optionItemList)
            assertFalse(state.multipleVotesEnabled)
            assertFalse(state.limitVotesEnabled)
            assertEquals(PollsConstants.MULTIPLE_ANSWERS_RANGE.first.toString(), state.maxVotesPerUserText)
            assertFalse(state.hasError)
            assertFalse(state.isCreationEnabled)
            assertFalse(state.hasChanges)
        }
    }

    // isCreationEnabled tests

    @Test
    fun `Given valid question and options with no errors When checking isCreationEnabled Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "Red", key = "1"),
                PollOptionItem(title = "Blue", key = "2"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isCreationEnabled)
        }
    }

    @Test
    fun `Given blank question When checking isCreationEnabled Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("   ")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "Red", key = "1"),
                PollOptionItem(title = "Blue", key = "2"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isCreationEnabled)
        }
    }

    @Test
    fun `Given no options When checking isCreationEnabled Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(emptyList())

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isCreationEnabled)
        }
    }

    @Test
    fun `Given options with only blank titles When checking isCreationEnabled Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "   ", key = "1"),
                PollOptionItem(title = "", key = "2"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isCreationEnabled)
        }
    }

    @Test
    fun `Given options with validation errors When checking isCreationEnabled Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(
                    title = "Red",
                    key = "1",
                    pollOptionError = PollOptionDuplicated("Duplicate option"),
                ),
                PollOptionItem(title = "Blue", key = "2"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isCreationEnabled)
        }
    }

    @Test
    fun `Given enabled multiple votes When checking isCreationEnabled Then should still return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(
            listOf(PollOptionItem(title = "Red", key = "1")),
        )
        viewModel.updateMultipleVotes(true)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isCreationEnabled)
        }
    }

    // hasChanges tests

    @Test
    fun `Given question is not blank When checking hasChanges Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("What is your favorite color?")

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasChanges)
        }
    }

    @Test
    fun `Given option with non-blank title When checking hasChanges Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateOptions(
            listOf(PollOptionItem(title = "Red", key = "1")),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasChanges)
        }
    }

    @Test
    fun `Given blank question and no options When checking hasChanges Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateQuestion("   ")
        viewModel.updateOptions(emptyList())

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasChanges)
        }
    }

    // hasError tests

    @Test
    fun `Given option with error When checking hasError Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateOptions(
            listOf(
                PollOptionItem(
                    title = "Red",
                    key = "1",
                    pollOptionError = PollOptionDuplicated("Duplicate option"),
                ),
                PollOptionItem(title = "Blue", key = "2"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasError)
        }
    }

    @Test
    fun `Given no errors in options and switches When checking hasError Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "Red", key = "1"),
                PollOptionItem(title = "Blue", key = "2"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasError)
        }
    }

    @Test
    fun `Given enabled multiple votes When checking hasError Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.updateMultipleVotes(true)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasError)
        }
    }

    // Visibility tests

    @Test
    fun `Given default config When checking switchItems Then all features should be present`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.switchItems.test {
            val items = awaitItem()
            assertEquals(
                listOf(
                    PollSwitchState.MultipleVotes::class,
                    PollSwitchState.AnonymousPoll::class,
                    PollSwitchState.SuggestAnOption::class,
                    PollSwitchState.AllowComments::class,
                ),
                items.map { it::class },
            )
        }
    }

    @Test
    fun `Given config with hidden features When checking switchItems Then features should be absent`() = runTest {
        val config = PollsConfig(
            multipleVotes = PollFeatureConfig(configurable = false),
            anonymousPoll = PollFeatureConfig(configurable = false),
        )
        val viewModel = CreatePollViewModel(config)

        viewModel.switchItems.test {
            val items = awaitItem()
            assertEquals(
                listOf(
                    PollSwitchState.SuggestAnOption::class,
                    PollSwitchState.AllowComments::class,
                ),
                items.map { it::class },
            )
        }
    }

    // Default value tests

    @Test
    fun `Given config with default values When created Then state should reflect defaults`() = runTest {
        val config = PollsConfig(
            multipleVotes = PollFeatureConfig(defaultValue = true),
            anonymousPoll = PollFeatureConfig(defaultValue = true),
        )
        val viewModel = CreatePollViewModel(config)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.multipleVotesEnabled)
            assertTrue(state.anonymousPollEnabled)
            assertFalse(state.suggestAnOptionEnabled)
            assertFalse(state.allowCommentsEnabled)
        }
    }

    // Integration tests

    @Test
    fun `Given complete poll creation flow When user enters data Then state should update correctly`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.state.test {
            // Initial state
            var state = awaitItem()
            assertEquals("", state.question)
            assertEquals(emptyList<PollOptionItem>(), state.optionItemList)
            assertFalse(state.hasChanges)
            assertFalse(state.isCreationEnabled)

            // User enters question
            viewModel.updateQuestion("What is your favorite color?")
            state = awaitItem()
            assertTrue(state.hasChanges)
            assertFalse(state.isCreationEnabled)

            // User adds options
            viewModel.updateOptions(
                listOf(
                    PollOptionItem(title = "Red", key = "1"),
                    PollOptionItem(title = "Blue", key = "2"),
                    PollOptionItem(title = "Green", key = "3"),
                ),
            )
            state = awaitItem()
            assertTrue(state.hasChanges)
            assertTrue(state.isCreationEnabled)
            assertFalse(state.hasError)

            // User enables multiple votes — no error since stepper always has a valid value
            viewModel.updateMultipleVotes(true)
            state = awaitItem()
            assertFalse(state.hasError)
            assertTrue(state.isCreationEnabled)

            // User enables limit votes and sets max
            viewModel.updateLimitVotes(true)
            state = awaitItem()
            assertTrue(state.limitVotesEnabled)

            viewModel.updateMaxVotes("5")
            state = awaitItem()
            assertEquals("5", state.maxVotesPerUserText)

            // User enables anonymous poll
            viewModel.updateAnonymousPoll(true)
            state = awaitItem()
            assertTrue(state.anonymousPollEnabled)
            assertTrue(state.isCreationEnabled)
        }
    }

    @Test
    fun `Given poll with errors When errors are introduced Then isCreationEnabled should become false`() = runTest {
        val viewModel = CreatePollViewModel(defaultConfig)

        viewModel.state.test {
            // Initial state
            awaitItem()

            // Set up valid poll
            viewModel.updateQuestion("What is your favorite color?")
            awaitItem()

            viewModel.updateOptions(
                listOf(
                    PollOptionItem(title = "Red", key = "1"),
                    PollOptionItem(title = "Blue", key = "2"),
                ),
            )
            var state = awaitItem()
            assertTrue(state.isCreationEnabled)

            // Introduce error
            viewModel.updateOptions(
                listOf(
                    PollOptionItem(
                        title = "Red",
                        key = "1",
                        pollOptionError = PollOptionDuplicated("Duplicate option"),
                    ),
                    PollOptionItem(title = "Blue", key = "2"),
                ),
            )
            state = awaitItem()
            assertFalse(state.isCreationEnabled)
            assertTrue(state.hasError)
        }
    }
}
