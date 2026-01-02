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

import androidx.compose.ui.text.input.KeyboardType
import app.cash.turbine.test
import io.getstream.chat.android.test.TestCoroutineExtension
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

    private val defaultSwitches = listOf(
        PollSwitchItem(
            title = "Multiple answers",
            enabled = false,
            key = "maxVotesAllowed",
        ),
        PollSwitchItem(
            title = "Anonymous poll",
            enabled = false,
            key = "votingVisibility",
        ),
    )

    // updateQuestion tests

    @Test
    fun `Given a new question When updateQuestion is called Then state should contain the new question`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)
        val newQuestion = "What is your favorite color?"

        viewModel.updateQuestion(newQuestion)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(newQuestion, state.question)
        }
    }

    @Test
    fun `Given empty question When updateQuestion with empty string Then state should have empty question`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

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
        val viewModel = CreatePollViewModel(defaultSwitches)
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
        val viewModel = CreatePollViewModel(defaultSwitches)

        viewModel.updateOptions(emptyList())

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(emptyList<PollOptionItem>(), state.optionItemList)
        }
    }

    // updateSwitches tests

    @Test
    fun `Given new switches When updateSwitches is called Then state should contain the new switches`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)
        val newSwitches = listOf(
            PollSwitchItem(
                title = "Multiple answers",
                enabled = true,
                key = "maxVotesAllowed",
            ),
        )

        viewModel.updateSwitches(newSwitches)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(newSwitches, state.switchItemList)
        }
    }

    // reset tests

    @Test
    fun `Given modified state When reset is called Then state should return to initial values`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

        // Modify the state
        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "Red", key = "1"),
                PollOptionItem(title = "Blue", key = "2"),
            ),
        )
        viewModel.updateSwitches(
            listOf(
                PollSwitchItem(
                    title = "Multiple answers",
                    enabled = true,
                    key = "maxVotesAllowed",
                ),
            ),
        )

        // Reset the state
        viewModel.reset()

        // Verify state is back to initial values
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.question)
            assertEquals(emptyList<PollOptionItem>(), state.optionItemList)
            assertEquals(defaultSwitches, state.switchItemList)
            assertFalse(state.hasError)
            assertFalse(state.isCreationEnabled)
            assertFalse(state.hasChanges)
        }
    }

    // isCreationEnabled tests

    @Test
    fun `Given valid question and options with no errors When checking isCreationEnabled Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

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
        val viewModel = CreatePollViewModel(defaultSwitches)

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
        val viewModel = CreatePollViewModel(defaultSwitches)

        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(emptyList())

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isCreationEnabled)
        }
    }

    @Test
    fun `Given options with only blank titles When checking isCreationEnabled Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

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
        val viewModel = CreatePollViewModel(defaultSwitches)

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
    fun `Given enabled switch with missing mandatory input When checking isCreationEnabled Then should return false`() = runTest {
        val switchesWithEnabledInput = listOf(
            PollSwitchItem(
                title = "Multiple answers",
                enabled = true,
                key = "maxVotesAllowed",
                pollSwitchInput = PollSwitchInput(
                    value = "",
                    keyboardType = KeyboardType.Number,
                ),
            ),
        )
        val viewModel = CreatePollViewModel(switchesWithEnabledInput)

        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "Red", key = "1"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isCreationEnabled)
        }
    }

    // hasChanges tests

    @Test
    fun `Given question is not blank When checking hasChanges Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

        viewModel.updateQuestion("What is your favorite color?")

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasChanges)
        }
    }

    @Test
    fun `Given option with non-blank title When checking hasChanges Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "Red", key = "1"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasChanges)
        }
    }

    @Test
    fun `Given both question and options are present When checking hasChanges Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

        viewModel.updateQuestion("What is your favorite color?")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "Red", key = "1"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasChanges)
        }
    }

    @Test
    fun `Given blank question and no options When checking hasChanges Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

        viewModel.updateQuestion("   ")
        viewModel.updateOptions(emptyList())

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasChanges)
        }
    }

    @Test
    fun `Given blank question and options with blank titles When checking hasChanges Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

        viewModel.updateQuestion("")
        viewModel.updateOptions(
            listOf(
                PollOptionItem(title = "   ", key = "1"),
                PollOptionItem(title = "", key = "2"),
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasChanges)
        }
    }

    // hasError tests

    @Test
    fun `Given option with error When checking hasError Then should return true`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

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
    fun `Given switch with error When checking hasError Then should return true`() = runTest {
        val switchesWithError = listOf(
            PollSwitchItem(
                title = "Multiple answers",
                enabled = false,
                key = "maxVotesAllowed",
                pollOptionError = PollOptionNumberExceed("Value exceeds maximum"),
            ),
        )
        val viewModel = CreatePollViewModel(switchesWithError)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasError)
        }
    }

    @Test
    fun `Given enabled switch with empty mandatory input When checking hasError Then should return true`() = runTest {
        val switchesWithEnabledInput = listOf(
            PollSwitchItem(
                title = "Multiple answers",
                enabled = true,
                key = "maxVotesAllowed",
                pollSwitchInput = PollSwitchInput(
                    value = "",
                    keyboardType = KeyboardType.Number,
                ),
            ),
        )
        val viewModel = CreatePollViewModel(switchesWithEnabledInput)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasError)
        }
    }

    @Test
    fun `Given no errors in options and switches When checking hasError Then should return false`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

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
    fun `Given disabled switch with empty input When checking hasError Then should return false`() = runTest {
        val switchesWithDisabledInput = listOf(
            PollSwitchItem(
                title = "Multiple answers",
                enabled = false,
                key = "maxVotesAllowed",
                pollSwitchInput = PollSwitchInput(
                    value = "",
                    keyboardType = KeyboardType.Number,
                ),
            ),
        )
        val viewModel = CreatePollViewModel(switchesWithDisabledInput)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasError)
        }
    }

    @Test
    fun `Given enabled switch with non-empty input When checking hasError Then should return false`() = runTest {
        val switchesWithValidInput = listOf(
            PollSwitchItem(
                title = "Multiple answers",
                enabled = true,
                key = "maxVotesAllowed",
                pollSwitchInput = PollSwitchInput(
                    value = "5",
                    keyboardType = KeyboardType.Number,
                ),
            ),
        )
        val viewModel = CreatePollViewModel(switchesWithValidInput)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasError)
        }
    }

    // Integration tests

    @Test
    fun `Given complete poll creation flow When user enters data Then state should update correctly`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

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

            // User enables a switch
            val updatedSwitches = listOf(
                PollSwitchItem(
                    title = "Multiple answers",
                    enabled = true,
                    key = "maxVotesAllowed",
                    pollSwitchInput = PollSwitchInput(
                        value = "2",
                        keyboardType = KeyboardType.Number,
                    ),
                ),
                PollSwitchItem(
                    title = "Anonymous poll",
                    enabled = true,
                    key = "votingVisibility",
                ),
            )
            viewModel.updateSwitches(updatedSwitches)
            state = awaitItem()
            assertTrue(state.isCreationEnabled)
            assertFalse(state.hasError)
        }
    }

    @Test
    fun `Given poll with errors When errors are introduced Then isCreationEnabled should become false`() = runTest {
        val viewModel = CreatePollViewModel(defaultSwitches)

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
