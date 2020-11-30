package io.getstream.chat.android.ui.utils

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.ui.R

@SuppressWarnings("unused")
internal object UiUtils {

    private val reactionTypes: Map<String, Int> = mapOf(
        ReactionType.LOVE.toString() to R.drawable.stream_ic_reaction_love,
        ReactionType.THUMBS_UP.toString() to R.drawable.stream_ic_reaction_thumbs_up,
        ReactionType.THUMBS_DOWN.toString() to R.drawable.stream_ic_reaction_thumbs_down,
        ReactionType.LOL.toString() to R.drawable.stream_ic_reaction_lol,
        ReactionType.WUT.toString() to R.drawable.stream_ic_reaction_wut
    )

    private val mimeTypesToIconResMap: Map<String, Int> = mapOf(
        ModelType.attach_mime_pdf to R.drawable.stream_ic_file_pdf,
        ModelType.attach_mime_csv to R.drawable.stream_ic_file_csv,
        ModelType.attach_mime_tar to R.drawable.stream_ic_file_tar,
        ModelType.attach_mime_zip to R.drawable.stream_ic_file_zip,
        ModelType.attach_mime_rar to R.drawable.stream_ic_file_rar,
        ModelType.attach_mime_7z to R.drawable.stream_ic_file_7z,
        ModelType.attach_mime_doc to R.drawable.stream_ic_file_doc,
        ModelType.attach_mime_docx to R.drawable.stream_ic_file_docx,
        ModelType.attach_mime_txt to R.drawable.stream_ic_file_txt,
        ModelType.attach_mime_rtf to R.drawable.stream_ic_file_rtf,
        ModelType.attach_mime_html to R.drawable.stream_ic_file_html,
        ModelType.attach_mime_md to R.drawable.stream_ic_file_md,
        ModelType.attach_mime_odt to R.drawable.stream_ic_file_odt,
        ModelType.attach_mime_xls to R.drawable.stream_ic_file_xls,
        ModelType.attach_mime_xlsx to R.drawable.stream_ic_file_xlsx,
        ModelType.attach_mime_ppt to R.drawable.stream_ic_file_ppt,
        ModelType.attach_mime_pptx to R.drawable.stream_ic_file_pptx,
        ModelType.attach_mime_mov to R.drawable.stream_ic_file_mov,
        ModelType.attach_mime_mp4 to R.drawable.stream_ic_file_mov,
        ModelType.attach_mime_m4a to R.drawable.stream_ic_file_mp3,
        ModelType.attach_mime_mp3 to R.drawable.stream_ic_file_mp3,
    )

    fun getIcon(mimeType: String?): Int {
        if (mimeType == null) {
            return R.drawable.stream_ic_file
        }
        return mimeTypesToIconResMap[mimeType] ?: when {
            mimeType.contains("audio") -> R.drawable.stream_ic_file_mp3
            mimeType.contains("video") -> R.drawable.stream_ic_file_mov
            else -> R.drawable.stream_ic_file
        }
    }

    fun getReactionTypes(): Map<String, Int> {
        return reactionTypes
    }
}

public enum class ReactionType(private val reactionType: String) {
    LOVE("love"),
    THUMBS_UP("thumbs_up"),
    THUMBS_DOWN("thumbs_down"),
    LOL("lol"),
    WUT("wut");

    override fun toString(): String = reactionType
}
