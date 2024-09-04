/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory

@Composable
public fun AudioRecordGroupContent(
    modifier: Modifier = Modifier,
    attachmentState: AttachmentState,
    viewModelFactory: AudioPlayerViewModelFactory,
) {

    val viewModel = viewModel(AudioPlayerViewModel::class.java, factory = viewModelFactory)

    val audioRecordings = attachmentState.message.attachments
        .filter { attachment -> attachment.isAudioRecording() && attachment.assetUrl != null }

    val playerState by viewModel.state.collectAsState()

    Column(modifier = modifier) {
        audioRecordings.forEach { audioRecording ->
            AudioRecordAttachmentContent(
                attachment = audioRecording,
                playerState = playerState,
                onPlayToggleClick = { attachment ->
                    viewModel.playOrPause(attachment)
                },
                onPlaySpeedClick = { attachment ->
                    viewModel.changeSpeed(attachment)
                },
                onPlayProgressChanged = { attachment, progress ->
                    viewModel.seekTo(attachment, progress)
                },
            )
        }
    }
}

// @Preview(showSystemUi = true, showBackground = true)
// @Composable
// internal fun AudioRecordGroupContentPreview() {
//     val attachment = Attachment(type = AttachmentType.AUDIO_RECORDING, assetUrl = "asd")
//     val attachmentState = AttachmentState(Message(attachments = mutableListOf(attachment)))
//
//     ChatPreviewTheme {
//         AudioRecordGroupContent(
//             attachmentState = attachmentState,
//             viewModelFactory = vmFactory,
//         )
//     }
// }
