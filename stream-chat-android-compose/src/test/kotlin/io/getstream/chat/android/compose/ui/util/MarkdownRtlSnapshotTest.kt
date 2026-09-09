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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import org.junit.Rule
import org.junit.Test

/**
 * Renders markdown in a right-to-left layout, where a quote's rail has to be mirrored to the side
 * its text is set in from. Lists need nothing: their indent and their marker are both laid out
 * from the start edge already.
 *
 * Only right-to-left content, and the layout direction is provided rather than left to the
 * device's locale. Paparazzi resolves an unspecified text direction from the layout alone, where a
 * device resolves it from the content of each paragraph, so a message mixing the two directions
 * renders here in a way no device would show and cannot be covered by a snapshot.
 */
internal class MarkdownRtlSnapshotTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi: Paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_2.copy(screenHeight = 1200),
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `right to left content`() = snapshotWithDarkMode {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            MarkdownText(
                """
                > هذا اقتباس طويل بما يكفي لكي يلتف على سطر ثانٍ ونرى أين ينتهي الخط
                >
                > وهذه فقرة ثانية من الاقتباس نفسه

                - عنصر قائمة طويل بما يكفي لكي يلتف على سطر ثانٍ ونرى مكانه
                    - عنصر متداخل طويل أيضاً لكي يلتف على سطر ثانٍ
                """.trimIndent(),
            )
        }
    }

    @Composable
    private fun MarkdownText(text: String) {
        val formatter = MessageTextFormatter.markdownFormatter(
            autoTranslationEnabled = false,
            typography = ChatTheme.typography,
            colors = ChatTheme.colors,
        )
        val message = Message(id = "id", cid = "messaging:cid", text = text, user = User(id = "other"))
        val styled = formatter.format(message, currentUser = User(id = "me"))
        val layout = remember(styled) { mutableStateOf<TextLayoutResult?>(null) }
        // The style and background a message actually renders on, so the snapshot shows the
        // heading levels against the same base weight the bubble gives paragraph text.
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.otherMessageTheme.backgroundColor)
                .padding(12.dp)
                .blockQuoteRails(
                    annotations = styled.getStringAnnotations(0, styled.length),
                    layout = layout::value,
                    color = ChatTheme.colors.textLowEmphasis,
                    indentPerDepth = MarkdownStyles.BlockQuoteIndent,
                ),
            text = styled,
            style = ChatTheme.otherMessageTheme.textStyle,
            onTextLayout = { layout.value = it },
        )
    }
}
