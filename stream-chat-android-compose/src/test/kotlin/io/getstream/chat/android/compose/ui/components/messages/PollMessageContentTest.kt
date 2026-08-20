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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_2_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import org.junit.Rule
import org.junit.Test

internal class PollMessageContentTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_2_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `poll content`() {
        snapshotWithDarkMode {
            Box(modifier = Modifier.fillMaxWidth()) {
                PollMessageContent(
                    modifier = Modifier,
                    onCastVote = { _, _, _ -> },
                    onRemoveVote = { _, _, _ -> },
                    selectPoll = { _, _, _ -> },
                    onAddAnswer = { _, _, _ -> },
                    onClosePoll = {},
                    onAddPollOption = { _, _ -> },
                    messageItem = MessageItemState(
                        message = PreviewMessageData.messageWithPoll,
                        isMine = true,
                        ownCapabilities = ChannelCapabilities.toSet(),
                    ),
                )
            }
        }
    }

    @Test
    fun `poll content with long option text`() {
        // The first long option has voter avatars; the second long option has none, so the
        // no-avatar case (vote count kept off the wrapped text) is covered too.
        val longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do " +
            "eiusmod tempor incididunt ut labore et dolore magna aliqua"
        val poll = PreviewPollData.poll1.let { poll ->
            val noVotesOptionId = poll.options[1].id
            poll.copy(
                options = poll.options.mapIndexed { index, option ->
                    if (index < 2) option.copy(text = longText) else option
                },
                votes = poll.votes.filterNot { it.optionId == noVotesOptionId },
                voteCountsByOption = poll.voteCountsByOption - noVotesOptionId,
            )
        }
        snapshotWithDarkMode {
            Box(modifier = Modifier.fillMaxWidth()) {
                PollMessageContent(
                    modifier = Modifier,
                    onCastVote = { _, _, _ -> },
                    onRemoveVote = { _, _, _ -> },
                    selectPoll = { _, _, _ -> },
                    onAddAnswer = { _, _, _ -> },
                    onClosePoll = {},
                    onAddPollOption = { _, _ -> },
                    messageItem = MessageItemState(
                        message = PreviewMessageData.messageWithPoll.copy(poll = poll),
                        isMine = true,
                        ownCapabilities = ChannelCapabilities.toSet(),
                    ),
                )
            }
        }
    }

    @Test
    fun `poll content with mixed vote states`() {
        // Only the first option has a voter avatar; the others have none. All options must keep
        // the same height so a voter avatar does not make its option taller.
        val poll = PreviewPollData.poll1.let { poll ->
            val votedOptionId = poll.options.first().id
            poll.copy(
                options = poll.options.map { it.copy(text = "Amsterdam") },
                votes = poll.votes.filter { it.optionId == votedOptionId },
                voteCountsByOption = mapOf(votedOptionId to 3),
            )
        }
        snapshotWithDarkMode {
            Box(modifier = Modifier.fillMaxWidth()) {
                PollMessageContent(
                    modifier = Modifier,
                    onCastVote = { _, _, _ -> },
                    onRemoveVote = { _, _, _ -> },
                    selectPoll = { _, _, _ -> },
                    onAddAnswer = { _, _, _ -> },
                    onClosePoll = {},
                    onAddPollOption = { _, _ -> },
                    messageItem = MessageItemState(
                        message = PreviewMessageData.messageWithPoll.copy(poll = poll),
                        isMine = true,
                        ownCapabilities = ChannelCapabilities.toSet(),
                    ),
                )
            }
        }
    }

    @Test
    fun `closed poll content`() {
        val poll = PreviewPollData.poll1.copy(closed = true)
        snapshotWithDarkMode {
            Box(modifier = Modifier.fillMaxWidth()) {
                PollMessageContent(
                    modifier = Modifier,
                    onCastVote = { _, _, _ -> },
                    onRemoveVote = { _, _, _ -> },
                    selectPoll = { _, _, _ -> },
                    onAddAnswer = { _, _, _ -> },
                    onClosePoll = {},
                    onAddPollOption = { _, _ -> },
                    messageItem = MessageItemState(
                        message = PreviewMessageData.messageWithPoll.copy(poll = poll),
                        isMine = true,
                        ownCapabilities = ChannelCapabilities.toSet(),
                    ),
                )
            }
        }
    }

    @Test
    fun `error poll content`() {
        snapshotWithDarkMode {
            Box(modifier = Modifier.fillMaxWidth()) {
                PollMessageContent(
                    modifier = Modifier,
                    onCastVote = { _, _, _ -> },
                    onRemoveVote = { _, _, _ -> },
                    selectPoll = { _, _, _ -> },
                    onAddAnswer = { _, _, _ -> },
                    onClosePoll = {},
                    onAddPollOption = { _, _ -> },
                    messageItem = MessageItemState(
                        message = PreviewMessageData.messageWithError,
                        isMine = true,
                        ownCapabilities = ChannelCapabilities.toSet(),
                    ),
                )
            }
        }
    }
}
