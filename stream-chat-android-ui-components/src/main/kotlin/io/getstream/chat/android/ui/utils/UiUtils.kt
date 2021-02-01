package io.getstream.chat.android.ui.utils

import androidx.annotation.DrawableRes
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.ui.R

public object UiUtils {

    private val reactionTypes: Map<String, Int> = ReactionType.values()
        .associate { it.type to it.iconRes }

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
        ModelType.attach_mime_mp4 to R.drawable.stream_ui_ic_file_mov,
        ModelType.attach_mime_m4a to R.drawable.stream_ui_ic_file_mp3,
        ModelType.attach_mime_mp3 to R.drawable.stream_ui_ic_file_mp3,
    )

    public fun getIcon(mimeType: String?): Int {
        if (mimeType == null) {
            return R.drawable.stream_ui_ic_file
        }
        return mimeTypesToIconResMap[mimeType] ?: when {
            mimeType.contains("audio") -> R.drawable.stream_ui_ic_file_mp3
            mimeType.contains("video") -> R.drawable.stream_ui_ic_file_mov
            else -> R.drawable.stream_ui_ic_file
        }
    }

    @DrawableRes
    internal fun getReactionIcon(type: String): Int? {
        return reactionTypes[type]
    }
}

public enum class ReactionType(
    public val type: String,
    @DrawableRes public val iconRes: Int,
) {
    LOVE("love", R.drawable.stream_ui_ic_reaction_love),
    THUMBS_UP("thumbs_up", R.drawable.stream_ui_ic_reaction_thumbs_up),
    THUMBS_DOWN("thumbs_down", R.drawable.stream_ui_ic_reaction_thumbs_down),
    LOL("lol", R.drawable.stream_ui_ic_reaction_lol),
    WUT("wut", R.drawable.stream_ui_ic_reaction_wut);
}
