package io.getstream.chat.android.ui.utils.extensions

import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt

internal fun Float.spToPx(): Int = spToPxPrecise().roundToInt()
internal fun Float.spToPxPrecise(): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)
