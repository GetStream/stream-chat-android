package com.getstream.sdk.chat.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.pow

@InternalStreamChatApi
public object MediaStringUtil {

    @JvmStatic
    public fun convertVideoLength(videoLength: Long): String {
        val hours = videoLength / 3600
        val minutes = videoLength % 3600 / 60
        val seconds = videoLength % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @JvmStatic
    public fun convertFileSizeByteCount(bytes: Long): String {
        val unit = 1000
        if (bytes <= 0) return 0.toString() + " B"
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        val df = DecimalFormat("###.##")
        return df.format(bytes / unit.toDouble().pow(exp.toDouble())) + " " + pre + "B"
    }
}
