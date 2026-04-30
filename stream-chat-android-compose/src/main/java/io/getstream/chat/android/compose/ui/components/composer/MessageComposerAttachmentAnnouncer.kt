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

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.models.Attachment

/**
 * Announces composer attachment additions and removals to accessibility services. Must be mounted
 * for the lifetime of the composer so size deltas are observed across attach and remove
 * transitions.
 *
 * @param attachments Current composer attachments. Size deltas drive the announcement.
 */
@Composable
internal fun MessageComposerAttachmentAnnouncer(attachments: List<Attachment>) {
    val view = LocalView.current
    val photoAttached = stringResource(R.string.stream_compose_attachment_added_photo)
    val videoAttached = stringResource(R.string.stream_compose_attachment_added_video)
    val fileAttached = stringResource(R.string.stream_compose_attachment_added_file)
    val audioAttached = stringResource(R.string.stream_compose_attachment_added_audio)
    val attachmentRemoved = stringResource(R.string.stream_compose_attachment_removed)

    var lastSize by remember { mutableIntStateOf(attachments.size) }

    LaunchedEffect(attachments.size) {
        val currentSize = attachments.size
        val message = when {
            currentSize > lastSize -> announceAddedAttachment(
                added = attachments.lastOrNull(),
                photoAttached = photoAttached,
                videoAttached = videoAttached,
                audioAttached = audioAttached,
                fileAttached = fileAttached,
            )
            currentSize < lastSize -> attachmentRemoved
            else -> null
        }
        if (message != null) {
            view.announceForAccessibility(message)
        }
        lastSize = currentSize
    }
}

@VisibleForTesting
internal fun announceAddedAttachment(
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
