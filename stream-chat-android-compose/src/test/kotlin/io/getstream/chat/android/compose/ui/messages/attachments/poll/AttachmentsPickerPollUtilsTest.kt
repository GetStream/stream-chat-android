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

import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.PollOption
import io.getstream.chat.android.models.VotingVisibility
import org.amshove.kluent.`should be equal to`
import org.junit.Test

internal class AttachmentsPickerPollUtilsTest {

    @Test
    fun testPollConfigWithAllDisabled() {
        // given
        val pollQuestion = "Poll question"
        val options = listOf(
            PollOptionItem(title = "Answer 1"),
            PollOptionItem(title = "Answer 2"),
            PollOptionItem(title = ""),
        )
        val state = CreatePollViewState(
            question = pollQuestion,
            optionItemList = options,
        )
        // when
        val pollConfig = pollConfigFrom(pollQuestion, options, state)
        // then
        val expected = PollConfig(
            name = "Poll question",
            options = listOf(PollOption(text = "Answer 1"), PollOption(text = "Answer 2")),
            description = "",
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = true,
            maxVotesAllowed = 1,
            allowUserSuggestedOptions = false,
            allowAnswers = false,
        )
        pollConfig `should be equal to` expected
    }

    @Test
    fun testPollConfigWithMultipleVotesNoLimit() {
        // given
        val pollQuestion = "Poll question"
        val options = listOf(
            PollOptionItem(title = "Answer 1"),
            PollOptionItem(title = "Answer 2"),
        )
        val state = CreatePollViewState(
            question = pollQuestion,
            optionItemList = options,
            multipleVotesEnabled = true,
            limitVotesEnabled = false,
        )
        // when
        val pollConfig = pollConfigFrom(pollQuestion, options, state)
        // then
        val expected = PollConfig(
            name = "Poll question",
            options = listOf(PollOption(text = "Answer 1"), PollOption(text = "Answer 2")),
            description = "",
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = false,
            maxVotesAllowed = null,
            allowUserSuggestedOptions = false,
            allowAnswers = false,
        )
        pollConfig `should be equal to` expected
    }

    @Test
    fun testPollConfigWithAllEnabled() {
        // given
        val pollQuestion = "Poll question"
        val options = listOf(
            PollOptionItem(title = "Answer 1"),
            PollOptionItem(title = "Answer 2"),
            PollOptionItem(title = ""),
        )
        val state = CreatePollViewState(
            question = pollQuestion,
            optionItemList = options,
            multipleVotesEnabled = true,
            limitVotesEnabled = true,
            maxVotesPerPersonText = "2",
            anonymousPollEnabled = true,
            suggestAnOptionEnabled = true,
            allowCommentsEnabled = true,
        )

        // when
        val pollConfig = pollConfigFrom(pollQuestion, options, state)
        // then
        val expected = PollConfig(
            name = "Poll question",
            options = listOf(PollOption(text = "Answer 1"), PollOption(text = "Answer 2")),
            description = "",
            votingVisibility = VotingVisibility.ANONYMOUS,
            enforceUniqueVote = false,
            maxVotesAllowed = 2,
            allowUserSuggestedOptions = true,
            allowAnswers = true,
        )
        pollConfig `should be equal to` expected
    }
}
