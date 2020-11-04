package io.getstream.chat.android.ui.utils.extensions

import android.content.res.TypedArray
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun TypedArray.use(block: (TypedArray) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block(this)
    recycle()
}