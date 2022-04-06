/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import io.getstream.chat.android.compose.R
import kotlin.math.abs
import kotlin.math.roundToInt

private const val GRADIENT_DARKER_COLOR_FACTOR = 1.3f
private const val GRADIENT_LIGHTER_COLOR_FACTOR = 0.7f

/**
 * Used for gradient color adjustment when the user doesn't have an image.
 *
 * @param color The color to adjust.
 * @param factor The factor by which we adjust the color.
 * @return [Int] ARGB value of the color after adjustment.
 */
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

/**
 * Generates a gradient for an initials avatar based on the user initials.
 *
 * @param initials The user initials to use for gradient colors.
 * @return The [Brush] that represents the gradient.
 */
@Composable
@ReadOnlyComposable
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

/**
 * Applies the given mirroring scaleX based on the [layoutDirection] that's currently configured in the UI.
 *
 * Useful since the Painter from Compose doesn't know how to parse `autoMirrored` flags in SVGs.
 */
public fun Modifier.mirrorRtl(layoutDirection: LayoutDirection): Modifier {
    return this.scale(
        scaleX = if (layoutDirection == LayoutDirection.Ltr) 1f else -1f,
        scaleY = 1f
    )
}
