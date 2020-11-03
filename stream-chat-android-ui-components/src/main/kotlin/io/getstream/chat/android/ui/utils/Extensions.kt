package io.getstream.chat.android.ui.utils

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.DimenRes

internal fun Context.getDimension(@DimenRes dimen: Int): Int {
    return resources.getDimensionPixelSize(dimen)
}

internal fun Context.getIntArray(@ArrayRes id: Int): IntArray {
    return resources.getIntArray(id)
}
