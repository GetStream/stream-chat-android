package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.getstream.chat.android.compose.R
import kotlin.math.abs
import kotlin.math.roundToInt

private const val GRADIENT_DARKER_COLOR_FACTOR = 1.3f
private const val GRADIENT_LIGHTER_COLOR_FACTOR = 0.7f

internal fun adjustColorBrightness(color: Int, factor: Float): Int {
    val a = android.graphics.Color.alpha(color)
    val r = (android.graphics.Color.red(color) * factor).roundToInt()
    val g = (android.graphics.Color.green(color) * factor).roundToInt()
    val b = (android.graphics.Color.blue(color) * factor).roundToInt()
    return android.graphics.Color.argb(
        a,
        r.coerceAtMost(255),
        g.coerceAtMost(255),
        b.coerceAtMost(255)
    )
}

@Composable
internal fun initialsGradient(initials: String): Brush {
    val gradientBaseColors = LocalContext.current.resources.getIntArray(R.array.stream_compose_avatar_gradient_colors)

    val baseColorIndex = abs(initials.hashCode()) % gradientBaseColors.size
    val baseColor = gradientBaseColors[baseColorIndex]

    return Brush.linearGradient(
        listOf(
            Color(adjustColorBrightness(baseColor, GRADIENT_DARKER_COLOR_FACTOR)),
            Color(adjustColorBrightness(baseColor, GRADIENT_LIGHTER_COLOR_FACTOR)),
        )
    )
}
