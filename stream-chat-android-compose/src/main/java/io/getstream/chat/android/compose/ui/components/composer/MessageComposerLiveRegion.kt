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

package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.models.Attachment

@Composable
internal fun MessageComposerLiveRegion(attachments: List<Attachment>) {
    val photoAttached = stringResource(R.string.stream_compose_attachment_added_photo)
    val videoAttached = stringResource(R.string.stream_compose_attachment_added_video)
    val fileAttached = stringResource(R.string.stream_compose_attachment_added_file)
    val audioAttached = stringResource(R.string.stream_compose_attachment_added_audio)
    val attachmentRemoved = stringResource(R.string.stream_compose_attachment_removed)

    var announcement by remember { mutableStateOf("") }
    var lastSize by remember { mutableIntStateOf(attachments.size) }

    LaunchedEffect(attachments.size) {
        val currentSize = attachments.size
        announcement = when {
            currentSize > lastSize -> announceAddedAttachment(
                added = attachments.lastOrNull(),
                photoAttached = photoAttached,
                videoAttached = videoAttached,
                audioAttached = audioAttached,
                fileAttached = fileAttached,
            )
            currentSize < lastSize -> attachmentRemoved
            else -> announcement
        }
        lastSize = currentSize
    }

    Text(
        text = announcement,
        color = Color.Transparent,
        modifier = Modifier
            .size(0.dp)
            .alpha(0f)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                hideFromAccessibility()
            },
    )
}

private fun announceAddedAttachment(
    added: Attachment?,
    photoAttached: String,
    videoAttached: String,
    audioAttached: String,
    fileAttached: String,
): String = when {
    added == null -> ""
    added.isImage() -> photoAttached
    added.isVideo() -> videoAttached
    added.isAudioRecording() -> audioAttached
    else -> fileAttached
}
