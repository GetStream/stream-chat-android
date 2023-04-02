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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents fallback content for unsupported attachments.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun AudioRecordAttachmentContent(
    modifier: Modifier = Modifier,
    attachmentState: AttachmentState,
) {
    val audioAttachment = attachmentState.message.attachments.firstOrNull { it.isAudioRecording() }
    val audioPlayer = ChatClient.instance().audioPlayer

    Surface(
        modifier = modifier
            .padding(2.dp)
            .fillMaxWidth(),
        color = ChatTheme.colors.appBackground,
        shape = ChatTheme.shapes.attachment
    ) {
        //Todo remove this later!
        if (audioAttachment != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(elevation = 2.dp, shape = CircleShape) {
                    IconButton(onClick = {
                        audioAttachment.assetUrl?.let { trackUrl ->
                            audioPlayer.play(trackUrl, audioAttachment.hashCode())
                        }
                    },
                        modifier = Modifier
                            .width(36.dp)
                            .height(36.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.stream_compose_ic_play),
                            contentDescription = null,
                            tint = Color.Black,
                        )
                    }
                }

                Text(text = "00:00",
                    Modifier
                        .fillMaxHeight()
                        .padding(8.dp))

                Slider(value = 0F, onValueChange = {}, modifier = Modifier.weight(1F))

                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_file_mp3),
                    contentDescription = "MP3 file",
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                text = "Not an audio attachment!!",
            )
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
