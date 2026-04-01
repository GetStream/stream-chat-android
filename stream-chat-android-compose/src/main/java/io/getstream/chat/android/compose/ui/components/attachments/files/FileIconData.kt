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

package io.getstream.chat.android.compose.ui.components.attachments.files

import io.getstream.chat.android.compose.R

internal enum class FileIconData(val resId: Int, val typeName: String) {
    Pdf(R.drawable.stream_design_ic_filetype_pdf, "pdf"),
    Csv(R.drawable.stream_design_ic_filetype_spreadsheet, "csv"),
    Xls(R.drawable.stream_design_ic_filetype_spreadsheet, "xls"),
    Xlsx(R.drawable.stream_design_ic_filetype_spreadsheet, "xlsx"),
    Tar(R.drawable.stream_design_ic_filetype_compression, "tar"),
    Zip(R.drawable.stream_design_ic_filetype_compression, "zip"),
    Rar(R.drawable.stream_design_ic_filetype_compression, "rar"),
    G7z(R.drawable.stream_design_ic_filetype_compression, "7z"),
    Doc(R.drawable.stream_design_ic_filetype_text, "doc"),
    Docx(R.drawable.stream_design_ic_filetype_text, "docx"),
    Txt(R.drawable.stream_design_ic_filetype_text, "txt"),
    Rtf(R.drawable.stream_design_ic_filetype_text, "rtf"),
    Odt(R.drawable.stream_design_ic_filetype_text, "odt"),
    Ppt(R.drawable.stream_design_ic_filetype_presentation, "ppt"),
    Pptx(R.drawable.stream_design_ic_filetype_presentation, "pptx"),
    Html(R.drawable.stream_design_ic_filetype_code, "html"),
    Md(R.drawable.stream_design_ic_filetype_code, "md"),
    M4a(R.drawable.stream_design_ic_filetype_audio, "m4a"),
    Mp3(R.drawable.stream_design_ic_filetype_audio, "mp3"),
    Aac(R.drawable.stream_design_ic_filetype_audio, "aac"),
    Mov(R.drawable.stream_design_ic_filetype_video, "mov"),
    Mp4(R.drawable.stream_design_ic_filetype_video, "mp4"),
    Generic(R.drawable.stream_design_ic_filetype_other, ""),
    AudioGeneric(R.drawable.stream_design_ic_filetype_audio, "audio"),
    VideoGeneric(R.drawable.stream_design_ic_filetype_video, "video"),
}
