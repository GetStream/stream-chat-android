package io.getstream.chat.android.ui.common.extensions.internal

import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.core.content.res.getColorOrThrow
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun TypedArray.use(block: (TypedArray) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block(this)
    recycle()
}

internal inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T): T {
    return getInt(index, -1).let {
        if (it >= 0) enumValues<T>()[it] else default
    }
}

@ColorInt
internal fun TypedArray.getColorOrNull(@StyleableRes index: Int): Int? = try {
    getColorOrThrow(index)
} catch (ex: Exception) {
    null
}
