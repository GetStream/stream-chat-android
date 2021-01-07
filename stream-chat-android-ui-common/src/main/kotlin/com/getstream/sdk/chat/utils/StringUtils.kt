package com.getstream.sdk.chat.utils

public object StringUtils {

    public fun removeTimePrefix(attachmentName: String?, usedDateFormat: String): String? {
        val regex = "^prefix_\\S+_stm_".toRegex()

        return if (attachmentName?.contains(regex) == true) {
            attachmentName.removeRange(0, 12 + usedDateFormat.length)
        } else {
            attachmentName
        }
    }
}
