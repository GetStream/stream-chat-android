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

package io.getstream.chat.android.ui.widgets.typing.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import kotlin.math.abs
import kotlin.math.sin

/**
 * Represents the inner part of [TypingIndicatorView] with 3 animated dots.
 */
internal class TypingIndicatorAnimationView : AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    )

    init {
        setImageDrawable(TypingDrawable(context))
    }
}

/**
 * A Drawable that renders an animation with 3 dots.
 */
private class TypingDrawable(context: Context) : Drawable() {
    private val paint: Paint = Paint()

    private val intrinsicHeight: Int
    private val intrinsicWidth: Int

    private val dotSpacingPx: Float
    private val dotDiameterPx: Float
    private val dotRadiusPx: Float

    init {
        paint.color = context.getColorCompat(R.color.stream_ui_grey)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        dotSpacingPx = DOT_SPACING_DP.dpToPxPrecise()
        dotDiameterPx = DOT_SIZE_DP.dpToPxPrecise()
        dotRadiusPx = dotDiameterPx / 2

        intrinsicWidth = ((DOT_SIZE_DP + DOT_SPACING_DP) * DOT_COUNT).dpToPx()
        intrinsicHeight = DOT_SIZE_DP.dpToPx()
    }

    /**
     * Returns the intrinsic or minimum width of the Drawable.
     */
    override fun getIntrinsicWidth(): Int = intrinsicWidth

    /**
     * Returns the intrinsic or minimum height of the Drawable.
     */
    override fun getIntrinsicHeight(): Int = intrinsicHeight

    /**
     * Draws the current frame of the animation and schedules the next frame.
     *
     * @param canvas The canvas to draw into.
     */
    override fun draw(canvas: Canvas) {
        for (dotIndex in 0 until DOT_COUNT) {
            paint.alpha = calculateAlpha(dotIndex)
            canvas.drawCircle(
                calculateCx(dotIndex),
                dotRadiusPx,
                dotRadiusPx,
                paint,
            )
        }
        invalidateSelf()
    }

    /**
     * Calculates the alpha of the dot to be drawn according to the current timestamp.
     *
     * @param dotIndex The index of the current dot to be drawn.
     * @return The alpha component of the paint's color from 0 to 255.
     */
    private fun calculateAlpha(dotIndex: Int): Int {
        val animationOffset = (DOT_COUNT - dotIndex) * ANIMATION_OFFSET_MILLIS
        val timeInCycle = (System.currentTimeMillis() + animationOffset) % FULL_ANIMATION_DURATION

        val coefficient: Float = if (timeInCycle > DOTS_ANIMATION_DURATION_MILLIS) {
            0f
        } else {
            abs(sin(Math.PI * timeInCycle / DOTS_ANIMATION_DURATION_MILLIS.toFloat())).toFloat()
        }

        return ((0.5f + 0.5f * coefficient) * 255).toInt()
    }

    /**
     * Calculates the x-coordinate of the dot to be drawn.
     *
     * @param dotIndex The index of the current dot to be drawn.
     * @return The x-coordinate of the center of the dot.
     */
    private fun calculateCx(dotIndex: Int): Float {
        return dotIndex * dotDiameterPx + dotIndex * dotSpacingPx + dotRadiusPx
    }

    override fun setAlpha(alpha: Int): Unit = Unit

    override fun setColorFilter(colorFilter: ColorFilter?): Unit = Unit

    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    companion object {

        /**
         * The number of dots to be rendered in the animation.
         */
        private const val DOT_COUNT = 3

        /**
         * The duration of dots animation.
         */
        private const val DOTS_ANIMATION_DURATION_MILLIS = 500

        /**
         * The delay between dots animations.
         */
        private const val DELAY_DURATION_MILLIS = 300

        /**
         * The full duration between animation cycles.
         */
        private const val FULL_ANIMATION_DURATION = DOTS_ANIMATION_DURATION_MILLIS + DELAY_DURATION_MILLIS

        /**
         * The amount of time each dot is highlighted.
         */
        private const val ANIMATION_OFFSET_MILLIS = DOTS_ANIMATION_DURATION_MILLIS / DOT_COUNT

        /**
         * The spacing between dots in DP.
         */
        private const val DOT_SPACING_DP = 3

        /**
         * The size of each dot in DP.
         */
        private const val DOT_SIZE_DP = 5
    }
}
