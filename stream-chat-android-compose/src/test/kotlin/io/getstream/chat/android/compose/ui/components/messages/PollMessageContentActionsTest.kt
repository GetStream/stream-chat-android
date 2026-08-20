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
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import org.junit.Rule
import org.junit.Test
import java.util.Date

/**
 * A poll with every secondary action is taller than the default device frame, so this test
 * uses its own taller frame instead of sharing [PollMessageContentTest]'s rule.
 */
internal class PollMessageContentActionsTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_2_HDPI.copy(screenHeight = 2182),
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `poll content with all secondary actions`() {
        // More than five options shows See All, suggestions enabled shows Suggest Option, and
        // an existing answer turns the add-comment button into View Comments.
        val poll = PreviewPollData.poll1.copy(
            options = List(6) { index -> Option(id = "option-$index", text = "option$index") },
            allowUserSuggestedOptions = true,
            answersCount = 1,
            answers = listOf(
                Answer(
                    id = "answer1",
                    pollId = PreviewPollData.poll1.id,
                    text = "A comment",
                    createdAt = Date(),
                    updatedAt = Date(),
                    user = null,
                ),
            ),
        )
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
}
