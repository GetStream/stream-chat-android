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

import io.getstream.chat.android.compose.ui.components.attachments.files.FileIconData
import io.getstream.chat.android.ui.common.model.MimeType
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MimeTypeIconProviderTest {

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("mimeTypeCases")
    fun `getIcon returns the correct FileIconData`(
        mimeType: String?,
        expectedIcon: FileIconData,
        expectedLabel: String,
    ) {
        MimeTypeIconProvider.getIcon(mimeType) `should be equal to` expectedIcon
    }

    companion object {
        @JvmStatic
        fun mimeTypeCases() = listOf(
            // Audio
            Arguments.of(MimeType.MIME_TYPE_AAC, FileIconData.Aac, "aac"),
            Arguments.of(MimeType.MIME_TYPE_M4A, FileIconData.M4a, "m4a"),
            Arguments.of(MimeType.MIME_TYPE_AUDIO_MP4, FileIconData.M4a, "m4a (audio/mp4)"),
            Arguments.of(MimeType.MIME_TYPE_MP3, FileIconData.Mp3, "mp3"),

            // Code
            Arguments.of(MimeType.MIME_TYPE_HTML, FileIconData.Html, "html"),
            Arguments.of(MimeType.MIME_TYPE_MD, FileIconData.Md, "md"),

            // Compression
            Arguments.of(MimeType.MIME_TYPE_7Z, FileIconData.G7z, "7z"),
            Arguments.of(MimeType.MIME_TYPE_RAR, FileIconData.Rar, "rar"),
            Arguments.of(MimeType.MIME_TYPE_TAR, FileIconData.Tar, "tar"),
            Arguments.of(MimeType.MIME_TYPE_ZIP, FileIconData.Zip, "zip"),

            // PDF
            Arguments.of(MimeType.MIME_TYPE_PDF, FileIconData.Pdf, "pdf"),

            // Presentation
            Arguments.of(MimeType.MIME_TYPE_PPT, FileIconData.Ppt, "ppt"),
            Arguments.of(MimeType.MIME_TYPE_PPTX, FileIconData.Pptx, "pptx"),

            // Spreadsheet
            Arguments.of(MimeType.MIME_TYPE_CSV, FileIconData.Csv, "csv"),
            Arguments.of(MimeType.MIME_TYPE_XLS, FileIconData.Xls, "xls"),
            Arguments.of(MimeType.MIME_TYPE_XLSX, FileIconData.Xlsx, "xlsx"),

            // Text
            Arguments.of(MimeType.MIME_TYPE_DOC, FileIconData.Doc, "doc"),
            Arguments.of(MimeType.MIME_TYPE_DOCX, FileIconData.Docx, "docx"),
            Arguments.of(MimeType.MIME_TYPE_ODT, FileIconData.Odt, "odt"),
            Arguments.of(MimeType.MIME_TYPE_RTF, FileIconData.Rtf, "rtf"),
            Arguments.of(MimeType.MIME_TYPE_TXT, FileIconData.Txt, "txt"),

            // Video
            Arguments.of(MimeType.MIME_TYPE_MOV, FileIconData.Mov, "mov"),
            Arguments.of(MimeType.MIME_TYPE_QUICKTIME, FileIconData.Mov, "quicktime"),
            Arguments.of(MimeType.MIME_TYPE_VIDEO_QUICKTIME, FileIconData.Mov, "video/quicktime"),
            Arguments.of(MimeType.MIME_TYPE_MP4, FileIconData.Mp4, "mp4"),
            Arguments.of(MimeType.MIME_TYPE_VIDEO_MP4, FileIconData.Mp4, "video/mp4"),

            // Generic fallbacks
            Arguments.of(null, FileIconData.Generic, "null"),
            Arguments.of("audio/wav", FileIconData.AudioGeneric, "audio/wav (generic audio)"),
            Arguments.of("audio/ogg", FileIconData.AudioGeneric, "audio/ogg (generic audio)"),
            Arguments.of("video/webm", FileIconData.VideoGeneric, "video/webm (generic video)"),
            Arguments.of("application/octet-stream", FileIconData.Generic, "unknown type"),
        )
    }
}
