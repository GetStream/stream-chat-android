/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.widgets.avatar.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.utils.extensions.getIntArray
import io.getstream.chat.android.uiutils.util.adjustColorBrightness
import kotlin.math.abs

/**
 * A Drawable that draws placeholder with initials for avatar.
 *
 * @param context The context used to load resources.
 * @param initials The initials to draw.
 * @param initialsTextStyle Text appearance for the initials.
 */
internal class AvatarPlaceholderDrawable(
    private val context: Context,
    private val initials: String,
    private val initialsTextStyle: TextStyle,
) : Drawable() {

    override fun setAlpha(alpha: Int) {
        // No-op
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // No-op
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun draw(canvas: Canvas) {
        canvas.drawGradient(initials)
        canvas.drawInitials(initials)
    }

    /**
     * Draws background gradient on the [Canvas].
     *
     * @param initials The initials to draw.
     */
    private fun Canvas.drawGradient(initials: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            shader = initialsGradient(initials, width, height)
        }

        drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            paint,
        )
    }

    /**
     * Draws initials on the [Canvas].
     *
     * @param
     */
    private fun Canvas.drawInitials(initials: String) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            typeface = initialsTextStyle.font ?: Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            color = initialsTextStyle.color
            textSize = initialsTextStyle.size.toFloat()
        }
        drawText(
            initials,
            width / 2f,
            height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f,
            textPaint,
        )
    }

    /**
     * Generates a gradient for an initials avatar based on the user initials.
     *
     * @param initials The user initials to use for gradient colors.
     * @param width The width of the [Canvas] to draw on.
     * @param height The width of the [Canvas] to draw on.
     * @return The [Shader] that represents the gradient.
     */
    private fun initialsGradient(
        initials: String,
        @Px width: Int,
        @Px height: Int,
    ): Shader {
        val gradientBaseColors = context.getIntArray(R.array.stream_ui_avatar_gradient_colors)

        val baseColorIndex = abs(initials.hashCode()) % gradientBaseColors.size
        val baseColor = gradientBaseColors[baseColorIndex]

        return LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            adjustColorBrightness(baseColor, GRADIENT_DARKER_COLOR_FACTOR),
            adjustColorBrightness(baseColor, GRADIENT_LIGHTER_COLOR_FACTOR),
            Shader.TileMode.CLAMP,
        )
    }

    companion object {
        private const val GRADIENT_DARKER_COLOR_FACTOR = 1.3f
        private const val GRADIENT_LIGHTER_COLOR_FACTOR = 0.7f
    }
}
