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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import io.getstream.chat.android.compose.ui.util.DefaultPollSwitchItemFactory
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.PollOption
import io.getstream.chat.android.models.VotingVisibility
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class AttachmentsPickerPollUtilsTest {

    @Test
    fun testPollConfigWithoutSwitchOptions() {
        // given
        val pollQuestion = "Poll question"
        val options = listOf(
            PollOptionItem(title = "Answer 1"),
            PollOptionItem(title = "Answer 2"),
            PollOptionItem(title = ""),
        )
        val context = mock<Context>()
        whenever(context.getString(any())).thenReturn("")
        val switches = DefaultPollSwitchItemFactory(context).providePollSwitchItemList()
        // when
        val pollConfig = pollConfigFrom(pollQuestion, options, switches)
        // then
        val expected = PollConfig(
            name = "Poll question",
            options = listOf(PollOption("Answer 1"), PollOption("Answer 2")),
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
    fun testPollConfigWithSwitchOptions() {
        // given
        val pollQuestion = "Poll question"
        val options = listOf(
            PollOptionItem(title = "Answer 1"),
            PollOptionItem(title = "Answer 2"),
            PollOptionItem(title = ""),
        )
        val context = mock<Context>()
        whenever(context.getString(any())).thenReturn("")
        val switches = listOf(
            PollSwitchItem(
                title = "Multiple answers",
                pollSwitchInput = PollSwitchInput(
                    value = "2",
                    description = "Enter an integer between 1 and 2",
                    minValue = 1,
                    maxValue = 2,
                    keyboardType = KeyboardType.Number,
                ),
                key = PollSwitchItemKeys.MAX_VOTES_ALLOWED,
                enabled = true,
            ),
            PollSwitchItem(
                title = "Anonymous poll",
                key = PollSwitchItemKeys.VOTING_VISIBILITY,
                enabled = true,
            ),
            PollSwitchItem(
                title = "Suggest an option",
                key = PollSwitchItemKeys.ALLOW_USER_SUGGESTED_OPTIONS,
                enabled = true,
            ),
            PollSwitchItem(
                title = "Add a comment",
                key = PollSwitchItemKeys.ALLOW_ANSWERS,
                enabled = true,
            ),
        )

        // when
        val pollConfig = pollConfigFrom(pollQuestion, options, switches)
        // then
        val expected = PollConfig(
            name = "Poll question",
            options = listOf(PollOption("Answer 1"), PollOption("Answer 2")),
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
