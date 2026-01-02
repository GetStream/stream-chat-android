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

package io.getstream.chat.android.compose.ui.attachments.preview.handler

import android.content.Context
import io.getstream.chat.android.compose.ui.attachments.preview.MediaPreviewActivity
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType

/**
 * Shows a preview for the audio/video stream in the attachment using Exoplayer library.
 */
public class MediaAttachmentPreviewHandler(private val context: Context) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        val assetUrl = attachment.assetUrl
        val mimeType = attachment.mimeType ?: ""
        val type = attachment.type ?: ""

        return when {
            assetUrl.isNullOrEmpty() -> false
            mimeType.isBlank() && type.isBlank() -> false
            AttachmentType.AUDIO in mimeType -> true
            AttachmentType.VIDEO in mimeType -> true
            AttachmentType.AUDIO in type -> true
            AttachmentType.VIDEO in type -> true
            buildMimeSubTypeList().any { subtype -> mimeType.contains(subtype) } -> true
            else -> false
        }
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        context.startActivity(
            MediaPreviewActivity.getIntent(
                context = context,
                url = requireNotNull(attachment.assetUrl),
                title = attachment.title ?: attachment.name,
            ),
        )
    }

    /**
     * Provides a list of MIME subtypes.
     */
    private fun buildMimeSubTypeList() = listOf(
        // mp3
        "mpeg-3", "x-mpeg3", "mp3", "mpeg", "x-mpeg",
        // aac
        "aac",
        // webm
        "webm",
        // wav
        "wav", "x-wav",
        // flac
        "flac", "x-flac",
        // ac3
        "ac3",
        // ogg
        "ogg", "x-ogg",
        // mp4
        "mp4",
        // m4a
        "x-m4a",
        // matroska
        "x-matroska",
        // vorbis
        "vorbis",
        // quicktime
        "quicktime",
    )
}
