package io.getstream.chat.android.ui.common.internal

import android.content.res.ColorStateList
import android.graphics.Color
import kotlin.math.roundToInt

internal fun adjustColorLBrightness(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = (Color.red(color) * factor).roundToInt()
    val g = (Color.green(color) * factor).roundToInt()
    val b = (Color.blue(color) * factor).roundToInt()
    return Color.argb(
        a,
        r.coerceAtMost(255),
        g.coerceAtMost(255),
        b.coerceAtMost(255)
    )
}

internal fun getColorList(normalColor: Int, selectedColor: Int, disabledColor: Int) = ColorStateList(
    arrayOf(
        intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected),
        intArrayOf(-android.R.attr.state_enabled)
    ),
    intArrayOf(normalColor, selectedColor, disabledColor)
)
