package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState

@Composable
public fun AudioRecordGroupContent(
    modifier: Modifier = Modifier,
    attachmentState: AttachmentState,
) {
    val audioTracks = attachmentState.message.attachments.filter { attachment -> attachment.isAudioRecording() }

    Column(modifier = modifier) {
        audioTracks.forEach { track ->
            AudioRecordAttachmentContent(audioTrack = track)
        }
    }
}
