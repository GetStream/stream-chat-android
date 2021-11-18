package io.getstream.chat.android.ui.common.extensions.internal

import android.content.res.Resources
import android.util.DisplayMetrics
import kotlin.math.roundToInt

internal fun Int.dpToPx(): Int = dpToPxPrecise().roundToInt()
internal fun Int.dpToPxPrecise(): Float = (this * displayMetrics().density)
internal fun displayMetrics(): DisplayMetrics = Resources.getSystem().displayMetrics
