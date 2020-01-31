package io.getstream.chat.android.client.utils

import java.util.*

object UndefinedDate : Date(-1L)

fun Date.isUndefined(): Boolean {
    return this == UndefinedDate
}