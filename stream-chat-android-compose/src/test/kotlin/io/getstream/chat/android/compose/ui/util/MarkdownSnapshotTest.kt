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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_2_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import org.junit.Rule
import org.junit.Test

/**
 * Renders every supported markdown construct through the markdown formatter, in light and
 * dark mode, so that changes to the styling or to the block layout are visible in review.
 *
 * Span-level styling is asserted precisely in [MarkdownRendererTest]; this covers what only a
 * render shows - indentation, line spacing, and how far backgrounds reach.
 */
internal class MarkdownSnapshotTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi: Paparazzi = Paparazzi(
        // The whole construct set in both themes is taller than a phone screen, and SHRINK trims
        // the render down to the content, so the device only has to be tall enough not to clip.
        deviceConfig = PIXEL_2_HDPI.copy(screenHeight = TallEnoughForEveryConstruct),
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `every supported construct`() = snapshotWithDarkMode {
        MarkdownText(
            """
            Plain, **bold**, *italic*, ***both***, ~~struck~~ and `code()`.
            A [link](https://getstream.io) and a bare https://getstream.io too.
            A [reference][d] link, and ![an image](https://x.com/a.png) as alt text.

            [d]: https://getstream.io

            # Heading 1
            ## Heading 2
            ### Heading 3
            #### Heading 4
            ##### Heading 5
            ###### Heading 6

            - first
            - second
                - nested
                    - deeper
            1. one
            1. two

            > a quoted line
            > and its continuation
            >
            > a second paragraph, still quoted

            > a separate quote

            ```kotlin
            fun main() {
                println("hi")
            }
            ```

            ---
            after the break
            """.trimIndent(),
        )
    }

    @Composable
    private fun MarkdownText(text: String) {
        val formatter = MessageTextFormatter.markdownFormatter(
            autoTranslationEnabled = false,
            typography = ChatTheme.typography,
            colors = ChatTheme.colors,
        )
        val message = Message(id = "id", cid = "messaging:cid", text = text, user = User(id = "other"))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            text = formatter.format(message, currentUser = User(id = "me")),
            style = ChatTheme.typography.bodyDefault,
        )
    }
}

private const val TallEnoughForEveryConstruct = 3000
