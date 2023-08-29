package io.getstream.chat.android.ui.common.extensions.internal

import android.view.View
import androidx.annotation.Px
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout

internal fun View.setPaddingStart(@Px start: Int) {
    val isRtl = context.isRtlLayout

    if (isRtl) {
        setPadding(paddingLeft, paddingTop, start, paddingBottom)
    } else {
        setPadding(start, paddingTop, paddingRight, paddingBottom)
    }
}

internal fun View.setPaddingEnd(@Px start: Int) {
    val isRtl = context.isRtlLayout

    if (isRtl) {
        setPadding(start, paddingTop, paddingRight, paddingBottom)
    } else {
        setPadding(paddingLeft, paddingTop, start, paddingBottom)
    }

}