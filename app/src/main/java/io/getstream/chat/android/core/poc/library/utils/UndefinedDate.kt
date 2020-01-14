package io.getstream.chat.android.core.poc.library.utils

import java.util.*

object UndefinedDate : Date(-1L)

fun Date.isUndefined(): Boolean {
    return this == UndefinedDate
}