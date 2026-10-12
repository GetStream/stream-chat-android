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

import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_2_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll
import org.junit.Rule
import org.junit.Test

internal class PollViewResultDialogTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_2_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `poll results`() = snapshot { PollResultsSheet() }

    @Test
    fun `poll results in dark mode`() = snapshot(isInDarkMode = true) { PollResultsSheet() }

    @Test
    fun `translated poll results`() {
        val poll = PreviewPollData.poll1.let { poll ->
            poll.copy(
                nameI18n = mapOf("language" to "en", "it_text" to "Dove andiamo in vacanza?"),
                options = poll.options.mapIndexed { index, option ->
                    option.copy(textI18n = mapOf("language" to "en", "it_text" to "Opzione ${index + 1}"))
                },
            )
        }
        snapshot {
            PollViewResultDialog(
                selectedPoll = SelectedPoll(
                    poll = poll,
                    message = PreviewMessageData.messageWithPoll.copy(poll = poll),
                    pollSelectionType = PollSelectionType.ViewResult,
                ),
                onDismissRequest = {},
                onBackPressed = {},
                currentUser = User(id = "user", language = "it"),
            )
        }
    }
}
