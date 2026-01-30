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

package io.getstream.chat.android.compose.ui.components.audio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Composable
internal fun PlaybackTimerText(
    progress: Float,
    durationInMs: Int?,
    color: Color,
    countdown: Boolean,
) {
    val totalDurationInMs = durationInMs ?: 0
    val playbackInMs = (progress * totalDurationInMs).toInt()
    val timeToShow = if (countdown) totalDurationInMs - playbackInMs else playbackInMs
    val playbackText = ChatTheme.durationFormatter.format(timeToShow)
    Text(
        text = playbackText,
        style = ChatTheme.typography.metadataEmphasis.copy(color),
    )
}

@Preview(showBackground = true)
@Composable
internal fun PlaybackTimerPreview() {
    val waveform = mutableListOf<Float>()
    val barCount = 100
    for (i in 0 until barCount) {
        waveform.add((i + 1) / barCount.toFloat())
    }

    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            PlaybackTimerText(
                progress = 1f,
                durationInMs = 120_000,
                color = Color.Black,
                countdown = true,
            )
        }
    }
}
