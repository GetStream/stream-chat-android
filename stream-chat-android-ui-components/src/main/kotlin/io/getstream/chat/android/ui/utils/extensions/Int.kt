package io.getstream.chat.android.ui.utils.extensions

import android.content.res.Resources
import kotlin.math.ceil

internal fun Int.dpToPx(): Int = ceil(dpToPxPrecise()).toInt()
internal fun Int.dpToPxPrecise(): Float = (this * Resources.getSystem().displayMetrics.density)
