/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout
import io.getstream.log.taggedLogger

private const val EXPAND_TRACKER_WIDTH = 10
private const val DEFAULT_BAR_SPACING = 0.4
private const val INITIAL_PROGRESS = 0F

/**
 * Custom view that presents a Seekbar that shows and interacts with audio wave bars.
 */
@Suppress("MagicNumber")
internal class WaveformView : LinearLayoutCompat {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    )

    private val logger by taggedLogger("WaveformView")

    private val isRtl = context.isRtlLayout

    private val slider: ImageView

    init {
        orientation = HORIZONTAL

        slider = ImageView(context).apply {
            setBackgroundResource(R.drawable.stream_ui_share_rectangle)
            isVisible = false
        }

        val layoutParamsButton = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        addView(slider, layoutParamsButton)

        setWillNotDraw(false)
    }

    private var barLimit = 100
    private var barWidth: Float? = null
    private var spaceWidth: Float? = null
    private var maxHeight: Int? = null
    private val barSpacing = DEFAULT_BAR_SPACING
    private var isDragging = false

    var onSliderDragStart: (Float) -> Unit = {}
    var onSliderDragStop: (Float) -> Unit = {}

    private val paintPassed = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stream_ui_accent_blue)
        style = Paint.Style.FILL
    }

    private val paintUpcoming = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stream_ui_grey)
        style = Paint.Style.FILL
    }

    private val _waveform = arrayListOf<Float>()

    fun setWaveformColor(@ColorInt color: Int) {
        paintPassed.color = color
    }

    var progress: Float = INITIAL_PROGRESS
        set(value) {
            logger.v { "[setProgress] progress: $value" }
            field = value
            invalidate()
        }

    var isSliderVisible: Boolean
        get() = slider.isVisible
        set(value) {
            slider.isVisible = value
        }

    var waveform: List<Float>
        get() = _waveform
        set(value) {
            _waveform.clear()
            _waveform.addAll(value)
            invalidate()
        }

    fun clearData() {
        this._waveform.clear()
        invalidate()
    }

    /**
     * In the method onMeasure the view calculates the size of important parts of the view, like the max height of
     * the bars, the size of each bar, etc. It is important to do that in this method to avoid recalculating the
     * sizes on each draw.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val viewportWidth = measuredWidth - paddingStart - paddingEnd
        val totalBarWidth = viewportWidth * (1 - barSpacing)
        val totalSpaceWidth = viewportWidth * barSpacing
        val barCount = _waveform.size

        barWidth = totalBarWidth.toFloat() / barCount
        spaceWidth = totalSpaceWidth.toFloat() / barCount
        maxHeight = measuredHeight - paddingTop - paddingBottom

        viewportRect.apply {
            left = paddingStart.toFloat()
            right = (measuredWidth - paddingEnd).toFloat()
            top = paddingTop.toFloat()
            bottom = (measuredHeight - paddingBottom).toFloat()
        }
    }

    private val viewportRect = RectF()
    private val barRect = RectF()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val adjustedX = if (isRtl) width - event.x else event.x
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                slider.updateLayoutParams {
                    width += EXPAND_TRACKER_WIDTH.dpToPx()
                }
                progress = adjustedX / viewportRect.width()
                onSliderDragStart(progress)
                true
            }

            MotionEvent.ACTION_MOVE -> {
                progress = adjustedX / viewportRect.width()
                true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                progress = adjustedX / viewportRect.width()
                onSliderDragStop(progress)
                isDragging = false
                slider.updateLayoutParams {
                    width -= EXPAND_TRACKER_WIDTH.dpToPx()
                }
                true
            }

            else -> super.onTouchEvent(event)
        }
    }

    /**
     * In onDraw all the bars are drawn and the tracker position is calculated.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewportW = viewportRect.width()
        val perBarW = viewportW / barLimit
        val spacerW = perBarW * 0.3f
        val barW = perBarW - spacerW

        val maxBarHeight = viewportRect.height()
        val centerY = height / 2f
        val maxEnd = width - paddingEnd
        val minStart = paddingStart

        val minVisibleIndex = maxOf((_waveform.size - barLimit), 0)

        val progressX = if (isRtl) viewportW * (1 - progress) else viewportW * progress

        for (index in _waveform.lastIndex downTo minVisibleIndex) {
            val value = _waveform[index]
            val barHeight = maxOf(maxBarHeight * value, barW)

            val relativeIndex = index - minVisibleIndex
            val top = centerY - barHeight / 2
            val bottom = centerY + barHeight / 2
            val start = if (isRtl) {
                maxEnd - perBarW * (relativeIndex + 1)
            } else {
                minStart + perBarW * relativeIndex
            }
            val end = if (isRtl) {
                start + barW
            } else {
                start + barW
            }

            val rx = barW / 2f
            val ry = rx

            barRect.set(start, top, end, bottom)

            if (barRect.left < minStart) {
                barRect.left = minStart.toFloat()
            } else if (barRect.right > maxEnd) {
                barRect.right = maxEnd.toFloat()
            }

            val passed = if (isRtl) {
                !isSliderVisible || barRect.centerX() > progressX
            } else {
                !isSliderVisible || barRect.centerX() < progressX
            }
            canvas.drawRoundRect(barRect, rx, ry, if (passed) paintPassed else paintUpcoming)
        }

        val sliderX = if (isRtl) {
            progressX - slider.width / 2f
        } else {
            progressX - slider.width / 2f
        }
        val finalSliderX = sliderX.coerceIn(viewportRect.left, viewportRect.right - slider.width)
        slider.x = finalSliderX
    }
}
