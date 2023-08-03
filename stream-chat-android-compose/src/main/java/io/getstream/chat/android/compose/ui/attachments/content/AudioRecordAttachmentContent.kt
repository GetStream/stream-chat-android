/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.DurationFormatter

/**
 * Represents fallback content for unsupported attachments.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun AudioRecordAttachmentContent(
    modifier: Modifier = Modifier,
    audioTrack: Attachment,
    onPlayPress: (Attachment) -> Unit,
) {
    val audioPlayer = ChatClient.instance().audioPlayer

    val duration = (audioTrack.duration ?: 0f)
        .let(DurationFormatter::formatDurationInSeconds)

    var trackProgress by remember { mutableStateOf(0F) }
    var durationText by remember { mutableStateOf(duration) }
    var playing by remember { mutableStateOf(false) }
    var speedState by remember { mutableStateOf(1F) }

    audioPlayer.run {
        val audioHash = audioTrack.hashCode()

        onProgressStateChange(audioHash) { progressData ->
            trackProgress = progressData.progress.toFloat()
            durationText = DurationFormatter.formatDurationInMillis(progressData.currentPosition)
        }

        onAudioStateChange(audioHash) { audioState ->
            playing = audioState == AudioState.PLAYING
        }

        onSpeedChange(audioHash) { speed ->
            speedState = speed
        }
    }

    Surface(
        modifier = modifier
            .padding(2.dp)
            .fillMaxWidth(),
        color = ChatTheme.colors.appBackground,
        shape = ChatTheme.shapes.attachment,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(elevation = 2.dp, shape = CircleShape) {
                IconButton(
                    onClick = {
                        onPlayPress(audioTrack)
                    },
                    modifier = Modifier
                        .width(36.dp)
                        .height(36.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_play),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }

            Text(
                text = durationText,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
            )

            Slider(
                value = trackProgress,
                onValueChange = {},
                modifier = Modifier.weight(1F),
            )

            if (playing) {
                Card(elevation = 2.dp, shape = CircleShape) {
                    TextButton(onClick = { audioPlayer.changeSpeed() }) {
                        Text(text = "x$speedState")
                    }
                }
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_file_mp3),
                    contentDescription = "MP3 file",
                )
            }
        }
    }
}

// @Preview(showSystemUi = true, showBackground = true)
// @Composable
// internal fun DefaultPreview() {
//     val attachment = Attachment(type = "audio_recording")
//
//     val attachmentState = AttachmentState(Message(attachments = mutableListOf(attachment)))
//
//     AudioRecordAttachmentContent(
//         Modifier
//             .width(200.dp)
//             .height(60.dp),
//         attachmentState,
//     )
// }
