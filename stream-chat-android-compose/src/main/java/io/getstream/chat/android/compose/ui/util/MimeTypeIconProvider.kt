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

package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.compose.R
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.uiutils.model.MimeType

/**
 * Provides icons for file attachments.
 */
internal object MimeTypeIconProvider {

    private val mimeTypesToIconResMap: Map<String, Int> = mapOf(
        MimeType.MIME_TYPE_PDF to R.drawable.stream_compose_ic_file_pdf,
        MimeType.MIME_TYPE_CSV to R.drawable.stream_compose_ic_file_csv,
        MimeType.MIME_TYPE_TAR to R.drawable.stream_compose_ic_file_tar,
        MimeType.MIME_TYPE_ZIP to R.drawable.stream_compose_ic_file_zip,
        MimeType.MIME_TYPE_RAR to R.drawable.stream_compose_ic_file_rar,
        MimeType.MIME_TYPE_7Z to R.drawable.stream_compose_ic_file_7z,
        MimeType.MIME_TYPE_DOC to R.drawable.stream_compose_ic_file_doc,
        MimeType.MIME_TYPE_DOCX to R.drawable.stream_compose_ic_file_docx,
        MimeType.MIME_TYPE_TXT to R.drawable.stream_compose_ic_file_txt,
        MimeType.MIME_TYPE_RTF to R.drawable.stream_compose_ic_file_rtf,
        MimeType.MIME_TYPE_HTML to R.drawable.stream_compose_ic_file_html,
        MimeType.MIME_TYPE_MD to R.drawable.stream_compose_ic_file_md,
        MimeType.MIME_TYPE_ODT to R.drawable.stream_compose_ic_file_odt,
        MimeType.MIME_TYPE_XLS to R.drawable.stream_compose_ic_file_xls,
        MimeType.MIME_TYPE_XLSX to R.drawable.stream_compose_ic_file_xlsx,
        MimeType.MIME_TYPE_PPT to R.drawable.stream_compose_ic_file_ppt,
        MimeType.MIME_TYPE_PPTX to R.drawable.stream_compose_ic_file_pptx,
        MimeType.MIME_TYPE_MOV to R.drawable.stream_compose_ic_file_mov,
        MimeType.MIME_TYPE_MP4 to R.drawable.stream_compose_ic_file_mp4,
        MimeType.MIME_TYPE_M4A to R.drawable.stream_compose_ic_file_m4a,
        MimeType.MIME_TYPE_MP3 to R.drawable.stream_compose_ic_file_mp3,
        MimeType.MIME_TYPE_AAC to R.drawable.stream_compose_ic_file_aac,
        // For compatibility with other front end SDKs
        MimeType.MIME_TYPE_QUICKTIME to R.drawable.stream_compose_ic_file_mov,
        MimeType.MIME_TYPE_VIDEO_QUICKTIME to R.drawable.stream_compose_ic_file_mov,
        MimeType.MIME_TYPE_VIDEO_MP4 to R.drawable.stream_compose_ic_file_mov,
    )

    /**
     * Returns a drawable resource for the given MIME type.
     *
     * @param mimeType The MIME type (i.e. application/pdf).
     * @return The drawable resource for the given MIME type.
     */
    fun getIconRes(mimeType: String?): Int {
        if (mimeType == null) {
            return R.drawable.stream_compose_ic_file_generic
        }
        return mimeTypesToIconResMap[mimeType] ?: when {
            mimeType.contains(AttachmentType.AUDIO) -> R.drawable.stream_compose_ic_file_audio_generic
            mimeType.contains(AttachmentType.VIDEO) -> R.drawable.stream_compose_ic_file_video_generic
            else -> R.drawable.stream_compose_ic_file_generic
        }
    }
}
