package io.getstream.chat.android.compose.ui.util

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.compose.R

internal object MimeTypeIconProvider {

    private val mimeTypesToIconResMap: Map<String, Int> = mapOf(
        ModelType.attach_mime_pdf to R.drawable.stream_compose_ic_file_pdf,
        ModelType.attach_mime_csv to R.drawable.stream_compose_ic_file_csv,
        ModelType.attach_mime_tar to R.drawable.stream_compose_ic_file_tar,
        ModelType.attach_mime_zip to R.drawable.stream_compose_ic_file_zip,
        ModelType.attach_mime_rar to R.drawable.stream_compose_ic_file_rar,
        ModelType.attach_mime_7z to R.drawable.stream_compose_ic_file_7z,
        ModelType.attach_mime_doc to R.drawable.stream_compose_ic_file_doc,
        ModelType.attach_mime_docx to R.drawable.stream_compose_ic_file_docx,
        ModelType.attach_mime_txt to R.drawable.stream_compose_ic_file_txt,
        ModelType.attach_mime_rtf to R.drawable.stream_compose_ic_file_rtf,
        ModelType.attach_mime_html to R.drawable.stream_compose_ic_file_html,
        ModelType.attach_mime_md to R.drawable.stream_compose_ic_file_md,
        ModelType.attach_mime_odt to R.drawable.stream_compose_ic_file_odt,
        ModelType.attach_mime_xls to R.drawable.stream_compose_ic_file_xls,
        ModelType.attach_mime_xlsx to R.drawable.stream_compose_ic_file_xlsx,
        ModelType.attach_mime_ppt to R.drawable.stream_compose_ic_file_ppt,
        ModelType.attach_mime_pptx to R.drawable.stream_compose_ic_file_pptx,
        ModelType.attach_mime_mov to R.drawable.stream_compose_ic_file_mov,
        ModelType.attach_mime_mp4 to R.drawable.stream_compose_ic_file_mov,
        ModelType.attach_mime_m4a to R.drawable.stream_compose_ic_file_mp3,
        ModelType.attach_mime_mp3 to R.drawable.stream_compose_ic_file_mp3,
        // For compatibility with other front end SDKs
        ModelType.attach_mime_quicktime to R.drawable.stream_compose_ic_file_mov,
        ModelType.attach_mime_video_quicktime to R.drawable.stream_compose_ic_file_mov,
        ModelType.attach_mime_video_mp4 to R.drawable.stream_compose_ic_file_mov,
    )

    fun getIconRes(mimeType: String?): Int {
        if (mimeType == null) {
            return R.drawable.stream_compose_ic_file_generic
        }
        return mimeTypesToIconResMap[mimeType] ?: when {
            mimeType.contains("audio") -> R.drawable.stream_compose_ic_file_mp3
            mimeType.contains("video") -> R.drawable.stream_compose_ic_file_mov
            else -> R.drawable.stream_compose_ic_file_generic
        }
    }
}
