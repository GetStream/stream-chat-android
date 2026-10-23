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

package io.getstream.chat.android.compose.ui.components.poll

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling.PollStyle
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.randomOption
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomVote
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class PollOptionVotingRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val votedOption = randomOption()
    private val otherOption = randomOption()

    @Test
    fun `tapping another option on a single answer poll casts the vote`() {
        val poll = pollWithOwnVote(enforceUniqueVote = true, maxVotesAllowed = 1)

        val result = clickUnvotedOption(poll)

        assertEquals(VoteCalls(cast = 1, removed = 0), result)
    }

    @Test
    fun `tapping another option is ignored when the vote limit is reached`() {
        val poll = pollWithOwnVote(enforceUniqueVote = false, maxVotesAllowed = 1)

        val result = clickUnvotedOption(poll)

        assertEquals(VoteCalls(cast = 0, removed = 0), result)
    }

    private fun pollWithOwnVote(enforceUniqueVote: Boolean, maxVotesAllowed: Int): Poll = randomPoll(
        enforceUniqueVote = enforceUniqueVote,
        maxVotesAllowed = maxVotesAllowed,
        closed = false,
        options = listOf(votedOption, otherOption),
        ownVotes = listOf(randomVote(optionId = votedOption.id)),
    )

    private fun clickUnvotedOption(poll: Poll): VoteCalls {
        var cast = 0
        var removed = 0
        composeTestRule.setContent {
            ChatTheme {
                PollOptionVotingRow(
                    poll = poll,
                    option = otherOption,
                    voteCount = 0,
                    totalVoteCount = 1,
                    users = emptyList(),
                    checked = false,
                    currentUser = null,
                    autoTranslationEnabled = false,
                    style = PollStyle(
                        textColor = Color.Black,
                        outlineColor = Color.Black,
                        progressColor = Color.Black,
                        trackColor = Color.Black,
                    ),
                    onCastVote = { cast++ },
                    onRemoveVote = { removed++ },
                )
            }
        }
        composeTestRule.onNodeWithTag("Stream_PollOptionVotingRow").performClick()
        return VoteCalls(cast = cast, removed = removed)
    }

    private data class VoteCalls(val cast: Int, val removed: Int)
}
