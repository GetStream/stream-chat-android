package io.getstream.chat.android.ui.common.extensions.internal

import android.content.res.Resources
import kotlin.math.roundToInt

internal fun Int.dpToPx(): Int = dpToPxPrecise().roundToInt()
internal fun Int.dpToPxPrecise(): Float = (this * Resources.getSystem().displayMetrics.density)
