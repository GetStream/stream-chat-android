package com.getstream.sdk.chat.utils

internal object StringUtils {

    fun removeTimePrefix(attachmentName: String?, usedDateFormat: String): String? {
        val dataFormatSize = usedDateFormat.length + 1
        val regex = "^STREAM_\\S{$dataFormatSize}".toRegex()

        return if (attachmentName?.contains(regex) == true) {
            attachmentName.replaceFirst(regex, "")
        } else {
            attachmentName
        }
    }
}
