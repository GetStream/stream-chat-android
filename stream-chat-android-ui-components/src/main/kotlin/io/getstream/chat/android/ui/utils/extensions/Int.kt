package io.getstream.chat.android.ui.utils.extensions

import android.content.res.Resources
import kotlin.math.roundToInt

internal fun Int.dpToPx(): Int = dpToPxPrecise().roundToInt()
internal fun Int.dpToPxPrecise(): Float = (this * Resources.getSystem().displayMetrics.density)
