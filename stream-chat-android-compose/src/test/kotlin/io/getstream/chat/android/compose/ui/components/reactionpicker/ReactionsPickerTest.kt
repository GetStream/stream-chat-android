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

package io.getstream.chat.android.compose.ui.components.reactionpicker

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.previewdata.PreviewReactionOptionData
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.previewdata.PreviewMessageData
import org.junit.Rule
import org.junit.Test

internal class ReactionsPickerTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `Default reaction picker`() {
        snapshotWithDarkMode {
            ReactionsPicker(
                message = PreviewMessageData.message1,
                onMessageAction = { },
                reactionTypes = PreviewReactionOptionData.reactionPickerIcons(5),
            )
        }
    }

    @Test
    fun `Reaction picker with more reactions`() {
        snapshotWithDarkMode {
            ReactionsPicker(
                message = PreviewMessageData.message1,
                onMessageAction = { },
                reactionTypes = PreviewReactionOptionData.reactionPickerIcons(12),
            )
        }
    }

    @Test
    fun `Reaction picker with header`() {
        snapshotWithDarkMode {
            ReactionsPicker(
                message = PreviewMessageData.message1,
                onMessageAction = { },
                reactionTypes = PreviewReactionOptionData.reactionPickerIcons(5),
                headerContent = {
                    Text(
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .padding(8.dp),
                        text = "My custom header",
                        style = ChatTheme.typography.title1,
                        color = ChatTheme.colors.textHighEmphasis,
                    )
                    Text(
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .padding(8.dp),
                        text = "Our reactions often reveal truths that actions alone can hide.",
                        style = ChatTheme.typography.footnoteItalic,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                },
            )
        }
    }
}
