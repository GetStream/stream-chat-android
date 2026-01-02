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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.TextContainerStyle
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size

/**
 * Represents the playback timer.
 *
 * @param progress The progress of the audio playback.
 * @param durationInMs The duration of the audio in milliseconds.
 * @param style The style for the timer component.
 */
@Composable
internal fun PlaybackTimerBox(
    progress: Float,
    durationInMs: Int?,
    style: TextContainerStyle,
) {
    Box(
        modifier = Modifier
            .size(style.size)
            .padding(style.padding)
            .background(style.backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        PlaybackTimerText(progress, durationInMs, style.textStyle)
    }
}

@Composable
internal fun PlaybackTimerText(
    progress: Float,
    durationInMs: Int?,
    style: TextStyle,
) {
    val finalDurationInMs = durationInMs ?: 0
    val playbackInMs = (progress * finalDurationInMs).toInt()
    val playbackText = ChatTheme.durationFormatter.format(
        if (progress > 0) playbackInMs else finalDurationInMs,
    )
    Text(
        style = style,
        text = playbackText,
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
                .height(80.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            PlaybackTimerBox(
                progress = 1f,
                durationInMs = 120_000,
                style = ChatTheme.ownMessageTheme.audioRecording.timerStyle,
            )
        }
    }
}
