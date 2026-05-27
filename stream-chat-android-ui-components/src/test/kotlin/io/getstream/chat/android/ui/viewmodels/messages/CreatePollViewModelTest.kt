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

package io.getstream.chat.android.ui.viewmodels.messages

import app.cash.turbine.test
import io.getstream.chat.android.models.CreatePollParams
import io.getstream.chat.android.models.PollOption
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.poll.CreatePollViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class CreatePollViewModelTest {

    @Test
    fun `Given poll options set When creating poll config Should emit matching CreatePollParams`() = runTest {
        val viewModel = CreatePollViewModel()
        viewModel.onTitleChanged("Favorite color?")
        viewModel.createOption()
        viewModel.onOptionTextChanged(id = 0, text = "Blue")
        viewModel.createOption()
        viewModel.onOptionTextChanged(id = 1, text = "Red")
        viewModel.setAnnonymousPoll(true)
        viewModel.setSuggestAnOption(true)
        viewModel.setAllowAnswers(true)
        viewModel.setAllowMultipleVotes(true)
        viewModel.setMaxAnswer(3)

        viewModel.createPollParams.test {
            awaitItem() shouldBeEqualTo null

            viewModel.createPollConfig()

            awaitItem() shouldBeEqualTo CreatePollParams(
                name = "Favorite color?",
                options = listOf(PollOption(text = "Blue"), PollOption(text = "Red")),
                votingVisibility = VotingVisibility.ANONYMOUS,
                enforceUniqueVote = false,
                maxVotesAllowed = 3,
                allowUserSuggestedOptions = true,
                allowAnswers = true,
            )
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }
}
