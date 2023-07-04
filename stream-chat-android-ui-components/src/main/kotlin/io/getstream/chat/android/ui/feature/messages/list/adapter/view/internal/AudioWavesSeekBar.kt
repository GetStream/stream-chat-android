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
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import java.lang.Float.min
import kotlin.math.max

private const val MIN_BAR_VALUE = 0.05F
private const val DEFAULT_BAR_HEIGHT_RATIO = 0.9F
private const val EXPAND_TRACKER_WIDTH = 10
private const val DEFAULT_BAR_PADDING = 5
private const val DEFAULT_BAR_SPACING = 0.4
private const val DEFAULT_BAR_NUMBER = 40
private const val DEFAULT_BAR_VALUE = 0F
private const val INITIAL_PROGRESS = 0F

/**
 * Custom view that presents a Seekbar that shows and interacts with audio wave bars.
 */
@Suppress("MagicNumber")
internal class AudioWavesSeekBar : LinearLayoutCompat {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val tracker: ImageView

    init {
        orientation = HORIZONTAL

        tracker = ImageView(context).apply {
            setBackgroundResource(R.drawable.stream_ui_share_rectangle)
        }

        val layoutParamsButton = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        addView(tracker, layoutParamsButton)
    }

    private val barPadding = DEFAULT_BAR_PADDING.dpToPx()
    private val realPaddingStart = paddingStart + barPadding
    private val realPaddingEnd = paddingEnd + barPadding
    private var barWidth: Float? = null
    private var spaceWidth: Float? = null
    private var maxHeight: Int? = null
    private val barSpacing = DEFAULT_BAR_SPACING
    private var barHeightRatio: Float = DEFAULT_BAR_HEIGHT_RATIO
    private var onStartDrag: () -> Unit = {}
    private var onEndDrag: (Int) -> Unit = {}
    private var isDragging = false

    private fun seekWidth(): Int = width - realPaddingStart - realPaddingEnd

    private val paintLeft = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stream_ui_accent_blue)
        style = Paint.Style.FILL
    }

    private val paintRight = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stream_ui_grey)
        style = Paint.Style.FILL
    }

    private var internalWaveBars: List<Float>? = null

    internal var waveBars: List<Float>
        set(value) {
            internalWaveBars = value
            invalidate()
        }
        get() = internalWaveBars ?: buildList {
            repeat(DEFAULT_BAR_NUMBER) {
                add(DEFAULT_BAR_VALUE)
            }
        }

    private var progress: Float = INITIAL_PROGRESS

    internal fun setProgress(progress: Float) {
        if (!isDragging) {
            this.progress = progress
            invalidate()
        }
    }

    private fun forceProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    internal fun setOnStartDrag(func: () -> Unit) {
        onStartDrag = func
    }

    internal fun setOnEndDrag(func: (Int) -> Unit) {
        onEndDrag = func
    }

    /**
     * In the method onMeasure the view calculates the size of important parts of the view, like the max height of
     * the bars, the size of each bar, etc. It is important to do that in this method to avoid recalculating the
     * sizes on each draw.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val totalWidth = measuredWidth - realPaddingStart - realPaddingEnd
        val totalBarWidth = totalWidth * (1 - barSpacing)
        val totalSpaceWidth = totalWidth * barSpacing
        val barCount = waveBars.size

        barWidth = totalBarWidth.toFloat() / barCount
        spaceWidth = totalSpaceWidth.toFloat() / barCount
        maxHeight = measuredHeight - paddingTop - paddingBottom
    }

    /**
     * This methods intercepts any [MotionEvent] in this view. When the user is interacting with this view, it
     * intercepts the actions, so it is not possible to perform actions like scrolling while interacting with this view.
     *
     * The progress of the view will change accordingly with the horizontal movement of the user. The wave bars and
     * the tracker will move accordingly with the progress.
     */
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        return when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                isDragging = true
                onStartDrag()
                parent.requestDisallowInterceptTouchEvent(true)
                tracker.updateLayoutParams {
                    width += EXPAND_TRACKER_WIDTH.dpToPx()
                }
                forceProgress(xToProgress(motionEvent.x))
                true
            }

            MotionEvent.ACTION_MOVE -> {
                forceProgress(xToProgress(motionEvent.x))
                true
            }

            MotionEvent.ACTION_UP -> {
                isDragging = false
                onEndDrag(xToProgress(motionEvent.x).toInt())
                parent.requestDisallowInterceptTouchEvent(false)
                tracker.updateLayoutParams {
                    width -= EXPAND_TRACKER_WIDTH.dpToPx()
                }
                true
            }

            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent.requestDisallowInterceptTouchEvent(false)
                tracker.updateLayoutParams {
                    width -= EXPAND_TRACKER_WIDTH.dpToPx()
                }
                true
            }

            else -> super.onTouchEvent(motionEvent)
        }
    }

    private val rect = RectF()

    /**
     * In onDraw all the bars are drawn and the tracker position is calculated.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        waveBars.forEachIndexed { index, barValue ->
            val barHeight = (maxHeight!! * max(barValue, MIN_BAR_VALUE) * barHeightRatio)

            val left = (barWidth!! + spaceWidth!!) * index + realPaddingStart
            val right = left + barWidth!!
            val top = (height - barHeight) / 2
            val bottom = top + barHeight

            rect.set(left, top, right, bottom)
            val paint = if (progressToX(progress) > left + barWidth!! / 2) paintLeft else paintRight

            tracker.x = trackerPosition(progressToX(progress)) - tracker.width / 2

            canvas.drawRoundRect(rect, barWidth!! / 2, barWidth!! / 2, paint)
        }
    }

    /**
     * Calculates the tracker position not allowing it go beyond the bounds of the seekbar.
     */
    private fun trackerPosition(positionX: Float) =
        min(
            max(realPaddingStart.toFloat() + tracker.width / 2, positionX),
            (width - realPaddingEnd - tracker.width / 2).toFloat()
        )

    private fun progressToX(progress: Float): Float =
        (progress / 100) * seekWidth() + realPaddingStart

    private fun xToProgress(x: Float): Float {
        val croppedX = min(max(realPaddingStart.toFloat(), x), width - realPaddingEnd.toFloat())
        return 100 * ((croppedX - realPaddingStart) / seekWidth())
    }
}
