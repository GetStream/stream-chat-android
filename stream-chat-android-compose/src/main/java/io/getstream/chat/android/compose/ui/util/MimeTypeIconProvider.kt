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
import io.getstream.chat.android.ui.common.model.MimeType

/**
 * Provides icons for file attachments.
 */
internal object MimeTypeIconProvider {
    /**
     * Returns the [FileIconData] corresponding to the given MIME type.
     *
     * @param mimeType The MIME type (i.e. application/pdf).
     * @return The [FileIconData] for the given MIME type.
     */
    fun getIcon(mimeType: String?): FileIconData = when (mimeType) {
        MimeType.MIME_TYPE_PDF -> FileIconData.Pdf

        MimeType.MIME_TYPE_CSV -> FileIconData.Csv
        MimeType.MIME_TYPE_XLS -> FileIconData.Xls
        MimeType.MIME_TYPE_XLSX -> FileIconData.Xlsx

        MimeType.MIME_TYPE_TAR -> FileIconData.Tar
        MimeType.MIME_TYPE_ZIP -> FileIconData.Zip
        MimeType.MIME_TYPE_RAR -> FileIconData.Rar
        MimeType.MIME_TYPE_7Z -> FileIconData.G7z

        MimeType.MIME_TYPE_DOC -> FileIconData.Doc
        MimeType.MIME_TYPE_DOCX -> FileIconData.Docx
        MimeType.MIME_TYPE_TXT -> FileIconData.Txt
        MimeType.MIME_TYPE_RTF -> FileIconData.Rtf
        MimeType.MIME_TYPE_ODT -> FileIconData.Odt

        MimeType.MIME_TYPE_PPT -> FileIconData.Ppt
        MimeType.MIME_TYPE_PPTX -> FileIconData.Pptx

        MimeType.MIME_TYPE_HTML -> FileIconData.Html
        MimeType.MIME_TYPE_MD -> FileIconData.Md

        MimeType.MIME_TYPE_M4A -> FileIconData.M4a
        MimeType.MIME_TYPE_MP3 -> FileIconData.Mp3
        MimeType.MIME_TYPE_AAC -> FileIconData.Aac

        MimeType.MIME_TYPE_MOV,
        MimeType.MIME_TYPE_QUICKTIME,
        MimeType.MIME_TYPE_VIDEO_QUICKTIME,
        -> FileIconData.Mov

        MimeType.MIME_TYPE_MP4,
        MimeType.MIME_TYPE_VIDEO_MP4,
        -> FileIconData.Mp4

        else -> when {
            mimeType == null -> FileIconData.Generic
            mimeType.contains(AttachmentType.AUDIO) -> FileIconData.AudioGeneric
            mimeType.contains(AttachmentType.VIDEO) -> FileIconData.VideoGeneric
            else -> FileIconData.Generic
        }
    }
}

internal enum class FileIconData(val resId: Int, val typeName: String) {
    Pdf(R.drawable.stream_compose_ic_file_pdf, "pdf"),
    Csv(R.drawable.stream_compose_ic_file_spreadsheet, "csv"),
    Xls(R.drawable.stream_compose_ic_file_spreadsheet, "xls"),
    Xlsx(R.drawable.stream_compose_ic_file_spreadsheet, "xlsx"),
    Tar(R.drawable.stream_compose_ic_file_compression, "tar"),
    Zip(R.drawable.stream_compose_ic_file_compression, "zip"),
    Rar(R.drawable.stream_compose_ic_file_compression, "rar"),
    G7z(R.drawable.stream_compose_ic_file_compression, "7z"),
    Doc(R.drawable.stream_compose_ic_file_doc, "doc"),
    Docx(R.drawable.stream_compose_ic_file_doc, "docx"),
    Txt(R.drawable.stream_compose_ic_file_doc, "txt"),
    Rtf(R.drawable.stream_compose_ic_file_doc, "rtf"),
    Odt(R.drawable.stream_compose_ic_file_doc, "odt"),
    Ppt(R.drawable.stream_compose_ic_file_presentation, "ppt"),
    Pptx(R.drawable.stream_compose_ic_file_presentation, "pptx"),
    Html(R.drawable.stream_compose_ic_file_code, "html"),
    Md(R.drawable.stream_compose_ic_file_code, "md"),
    M4a(R.drawable.stream_compose_ic_file_audio, "m4a"),
    Mp3(R.drawable.stream_compose_ic_file_audio, "mp3"),
    Aac(R.drawable.stream_compose_ic_file_audio, "aac"),
    Mov(R.drawable.stream_compose_ic_file_video, "mov"),
    Mp4(R.drawable.stream_compose_ic_file_video, "mp4"),
    Generic(R.drawable.stream_compose_ic_file_generic, ""),
    AudioGeneric(R.drawable.stream_compose_ic_file_audio, "audio"),
    VideoGeneric(R.drawable.stream_compose_ic_file_video, "video"),
}
