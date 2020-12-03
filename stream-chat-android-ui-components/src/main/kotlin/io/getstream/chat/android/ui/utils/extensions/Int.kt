package io.getstream.chat.android.ui.utils.extensions

import android.content.res.Resources

internal fun Int.dpToPx(): Int = dpToPxPrecise().toInt()
internal fun Int.dpToPxPrecise(): Float = (this * Resources.getSystem().displayMetrics.density)
