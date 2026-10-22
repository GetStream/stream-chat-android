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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRating
import io.getstream.chat.android.ai.compose.conversation.AIReplyActions
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class AIConversationThemingTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    private val files = listOf(
        AIMessageAttachment.file("https://example.com/files/itinerary.pdf".toUri(), size = 2_400_000),
        AIMessageAttachment.file("https://example.com/files/voice-note.m4a".toUri(), mimeType = "audio/mp4"),
        AIMessageAttachment.file("https://example.com/files/tram-ride.mp4".toUri(), mimeType = "video/mp4"),
        AIMessageAttachment.file("https://example.com/files/photos.zip".toUri(), size = 48_000_000),
    )

    @Test
    fun `custom colors and icons`() {
        snapshot {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AIUserMessage(
                    text = "Plan a weekend in Lisbon around these.",
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    colors = AIUserMessageDefaults.colors(background = Ink, text = Paper),
                )
                files.forEach { file ->
                    AIMessageAttachmentItem(
                        attachment = file,
                        nameTextStyle = Typography().titleSmall.copy(fontWeight = FontWeight.Bold),
                        colors = attachmentColors(),
                        icons = attachmentIcons(),
                    )
                }
                AIReplyActionsRow(
                    text = "Here is the plan.",
                    actions = AIReplyActions(rating = AIMessageRating.Negative, onRegenerate = {}, onRate = {}),
                    colors = AIReplyActionsDefaults.colors(icon = Coral),
                    icons = AIReplyActionsDefaults.icons(
                        copy = painterResource(R.drawable.stream_ai_compose_ic_add),
                        share = painterResource(R.drawable.stream_ai_compose_ic_send),
                        regenerate = painterResource(R.drawable.stream_ai_compose_ic_sparkles),
                        ratePositive = painterResource(R.drawable.stream_ai_compose_ic_check),
                        ratedNegative = painterResource(R.drawable.stream_ai_compose_ic_close),
                    ),
                )
            }
        }
    }

    @Composable
    private fun attachmentColors() = AIMessageAttachmentDefaults.colors(
        background = Paper,
        border = Coral,
        iconBackground = Coral,
        icon = Paper,
        name = Ink,
        details = Coral,
    )

    @Composable
    private fun attachmentIcons() = AIMessageAttachmentDefaults.icons(
        pdf = painterResource(R.drawable.stream_ai_compose_ic_reasoning),
        audio = painterResource(R.drawable.stream_ai_compose_ic_mic),
        video = painterResource(R.drawable.stream_ai_compose_ic_device),
        archive = painterResource(R.drawable.stream_ai_compose_ic_hand),
    )
}

private val Ink = Color(0xFF1B2A41)
private val Paper = Color(0xFFFFF4E0)
private val Coral = Color(0xFFE4572E)
