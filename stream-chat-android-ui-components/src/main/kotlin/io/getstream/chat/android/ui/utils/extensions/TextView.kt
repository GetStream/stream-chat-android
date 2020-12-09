package io.getstream.chat.android.ui.utils.extensions

import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.Px

internal fun TextView.setTextSizePx(@Px size: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
}
