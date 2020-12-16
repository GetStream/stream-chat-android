package io.getstream.chat.android.ui.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import io.getstream.chat.android.ui.R
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

internal fun getColorList(normalColor: Int, pressedColor: Int, disabledColor: Int) = ColorStateList(
    arrayOf(
        intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_pressed),
        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed),
        intArrayOf(-android.R.attr.state_enabled)
    ),
    intArrayOf(normalColor, pressedColor, disabledColor)
)

internal fun getBackgroundColor(context: Context): Int {
    return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO -> R.color.stream_ui_alabaster
        Configuration.UI_MODE_NIGHT_YES -> R.color.stream_ui_black
        else -> R.color.stream_ui_alabaster
    }
}

internal fun getTextColor(context: Context) : Int {
    return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO ->  R.color.stream_ui_black
        Configuration.UI_MODE_NIGHT_YES -> R.color.stream_ui_white
        else -> R.color.stream_ui_black
    }
}
