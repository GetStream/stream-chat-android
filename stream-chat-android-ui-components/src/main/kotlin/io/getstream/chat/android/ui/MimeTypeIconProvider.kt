/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.ui.message.input.attachment.file.internal.FileAttachmentAdapter
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedFileAttachmentAdapter

/**
 * Provides icons for file attachments.
 *
 * @see [FileAttachmentAdapter.FileAttachmentViewHolder]
 * @see [SelectedFileAttachmentAdapter.SelectedFileAttachmentViewHolder]
 */
public fun interface MimeTypeIconProvider {

    /**
     * Returns a drawable resource for the given MIME type.
     *
     * @param mimeType The MIME type (i.e. application/pdf).
     * @return The drawable resource for the given MIME type.
     */
    public fun getIconRes(mimeType: String?): Int
}

/**
 * Provides icons for file attachments.
 */
public class MimeTypeIconProviderImpl : MimeTypeIconProvider {

    private val mimeTypesToIconResMap: Map<String, Int> = mapOf(
        ModelType.attach_mime_pdf to R.drawable.stream_ui_ic_file_pdf,
        ModelType.attach_mime_csv to R.drawable.stream_ui_ic_file_csv,
        ModelType.attach_mime_tar to R.drawable.stream_ui_ic_file_tar,
        ModelType.attach_mime_zip to R.drawable.stream_ui_ic_file_zip,
        ModelType.attach_mime_rar to R.drawable.stream_ui_ic_file_rar,
        ModelType.attach_mime_7z to R.drawable.stream_ui_ic_file_7z,
        ModelType.attach_mime_doc to R.drawable.stream_ui_ic_file_doc,
        ModelType.attach_mime_docx to R.drawable.stream_ui_ic_file_docx,
        ModelType.attach_mime_txt to R.drawable.stream_ui_ic_file_txt,
        ModelType.attach_mime_rtf to R.drawable.stream_ui_ic_file_rtf,
        ModelType.attach_mime_html to R.drawable.stream_ui_ic_file_html,
        ModelType.attach_mime_md to R.drawable.stream_ui_ic_file_md,
        ModelType.attach_mime_odt to R.drawable.stream_ui_ic_file_odt,
        ModelType.attach_mime_xls to R.drawable.stream_ui_ic_file_xls,
        ModelType.attach_mime_xlsx to R.drawable.stream_ui_ic_file_xlsx,
        ModelType.attach_mime_ppt to R.drawable.stream_ui_ic_file_ppt,
        ModelType.attach_mime_pptx to R.drawable.stream_ui_ic_file_pptx,
        ModelType.attach_mime_mov to R.drawable.stream_ui_ic_file_mov,
        ModelType.attach_mime_quicktime to R.drawable.stream_ui_ic_file_mov,
        ModelType.attach_mime_video_quicktime to R.drawable.stream_ui_ic_file_mov,
        ModelType.attach_mime_mp4 to R.drawable.stream_ui_ic_file_mov,
        ModelType.attach_mime_video_mp4 to R.drawable.stream_ui_ic_file_mp4,
        ModelType.attach_mime_m4a to R.drawable.stream_ui_ic_file_m4a,
        ModelType.attach_mime_mp3 to R.drawable.stream_ui_ic_file_mp3,
    )

    /**
     * Returns a drawable resource for the given MIME type.
     *
     * @param mimeType The MIME type (i.e. application/pdf).
     * @return The drawable resource for the given MIME type.
     */
    public override fun getIconRes(mimeType: String?): Int {
        if (mimeType == null) {
            return R.drawable.stream_ui_ic_file
        }
        return mimeTypesToIconResMap[mimeType] ?: when {
            mimeType.contains(ModelType.attach_audio) -> R.drawable.stream_ui_ic_file_audio_generic
            mimeType.contains(ModelType.attach_video) -> R.drawable.stream_ui_ic_file_video_generic
            else -> R.drawable.stream_ui_ic_file
        }
    }
}
