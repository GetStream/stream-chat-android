package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState

@Composable
public fun AudioRecordGroupContent(
    modifier: Modifier = Modifier,
    attachmentState: AttachmentState,
) {
    val audioPlayer = ChatClient.instance().audioPlayer
    val audioTracks =
        attachmentState.message
            .attachments
            .filter { attachment -> attachment.isAudioRecording() && attachment.assetUrl != null }

    Column(modifier = modifier) {
        audioTracks.forEach { track ->
            AudioRecordAttachmentContent(audioTrack = track) { audioTrack ->
                audioPlayer.clearTracks()
                audioTracks.forEachIndexed { index, track ->
                    audioPlayer.registerTrack(track.assetUrl!!, track.hashCode(), index)
                }
                audioTrack.assetUrl?.let { trackUrl ->
                    audioPlayer.play(trackUrl, audioTrack.hashCode())
                }
            }
        }
    }
}
