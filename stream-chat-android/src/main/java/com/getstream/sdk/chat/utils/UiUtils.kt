package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

@InternalStreamChatApi
public object UiUtils {
    private val fileSizeFormat = DecimalFormat("#,##0.#")
    private val fileSizeUnits = arrayOf("B", "KB", "MB", "GB", "TB")

    private val mimeTypesToIconResMap: Map<String, Int> = mapOf(
        ModelType.attach_mime_pdf to R.drawable.stream_ic_file_pdf,
        ModelType.attach_mime_csv to R.drawable.stream_ic_file_csv,
        ModelType.attach_mime_tar to R.drawable.stream_ic_file_tar,
        ModelType.attach_mime_zip to R.drawable.stream_ic_file_zip,
        ModelType.attach_mime_doc to R.drawable.stream_ic_file_doc,
        ModelType.attach_mime_xls to R.drawable.stream_ic_file_xls,
        ModelType.attach_mime_ppt to R.drawable.stream_ic_file_ppt,
        ModelType.attach_mime_mov to R.drawable.stream_ic_file_mov,
        ModelType.attach_mime_mp4 to R.drawable.stream_ic_file_mov,
        ModelType.attach_mime_m4a to R.drawable.stream_ic_file_mp3,
        ModelType.attach_mime_mp3 to R.drawable.stream_ic_file_mp3,
    )

    private val reactionTypeToSymbolMap: Map<String, String> = mapOf(
        "like" to "\uD83D\uDC4D",
        "love" to "\u2764\uFE0F",
        "haha" to "\uD83D\uDE02",
        "wow" to "\uD83D\uDE32",
        "sad" to "\uD83D\uDE41",
        "angry" to "\uD83D\uDE21"
    )

    @InternalStreamChatApi
    public fun getIcon(mimeType: String?): Int {
        if (mimeType == null) {
            return R.drawable.stream_ic_file
        }
        return mimeTypesToIconResMap[mimeType] ?: when {
            mimeType.contains("audio") -> R.drawable.stream_ic_file_mp3
            mimeType.contains("video") -> R.drawable.stream_ic_file_mov
            else -> R.drawable.stream_ic_file
        }
    }

    @JvmStatic
    @InternalStreamChatApi
    public fun getReactionTypes(): Map<String, String> {
        return reactionTypeToSymbolMap
    }

    @InternalStreamChatApi
    public fun getFileSizeHumanized(fileSize: Int): String {
        if (fileSize <= 0) return "0"
        val digitGroups = (log10(fileSize.toDouble()) / log10(1024.0)).toInt()
        val size = fileSizeFormat.format(fileSize / 1024.0.pow(digitGroups.toDouble()))
        val unit = fileSizeUnits[digitGroups]
        return "$size $unit"
    }
}
