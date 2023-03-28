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
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.ui.R
import java.lang.Float.min
import kotlin.math.max
import kotlin.math.roundToInt

private const val MIN_BAR_VALUE = 0.05F
private const val DEFAULT_BAR_HEIGHT_RATIO = 0.9F
private const val EXPAND_TRACKER_WIDTH = 12
private const val HALF = 2
private const val DEFAULT_BAR_PADDING = 5
private const val DEFAULT_BAR_SPACING = 0.4
private const val DEFAULT_BAR_NUMBER = 40
private const val DEFAULT_BAR_VALUE = 0F
private const val INITIAL_PROGRESS = 0F
private const val ONE = 1
private const val ONE_HUNDRED = 100

public class AudioWavesSeekBar : LinearLayoutCompat {
    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
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

    private val barPadding = DEFAULT_BAR_PADDING.dp
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
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val paintRight = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    private var internalWaveBars: List<Float>? = null

    public var waveBars: List<Float>
        set(value) {
            internalWaveBars = value
        }
        get() = internalWaveBars ?: buildList {
            repeat(DEFAULT_BAR_NUMBER) {
                add(DEFAULT_BAR_VALUE)
            }
        }

    private var progress: Float = INITIAL_PROGRESS

    public fun setProgress(progress: Float) {
        if (!isDragging) {
            this.progress = progress
            invalidate()
        }
    }

    private fun forceProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    public fun setOnStartDrag(func: () -> Unit) {
        onStartDrag = func
    }

    public fun setOnEndDrag(func: (Int) -> Unit) {
        onEndDrag = func
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val totalWidth = measuredWidth - realPaddingStart - realPaddingEnd
        val totalBarWidth = totalWidth * (ONE - barSpacing)
        val totalSpaceWidth = totalWidth * barSpacing
        val barCount = waveBars.size

        barWidth = totalBarWidth.toFloat() / barCount
        spaceWidth = totalSpaceWidth.toFloat() / barCount
        maxHeight = measuredHeight - paddingTop - paddingBottom
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        return when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                isDragging = true
                onStartDrag()
                parent.requestDisallowInterceptTouchEvent(true)
                tracker.updateLayoutParams {
                    width += EXPAND_TRACKER_WIDTH.dp
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
                    width -= EXPAND_TRACKER_WIDTH.dp
                }
                true
            }

            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent.requestDisallowInterceptTouchEvent(false)
                tracker.updateLayoutParams {
                    width -= EXPAND_TRACKER_WIDTH.dp
                }
                true
            }

            else -> super.onTouchEvent(motionEvent)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        waveBars.forEachIndexed { index, barValue ->
            val barHeight = (maxHeight!! * max(barValue, MIN_BAR_VALUE) * barHeightRatio)

            val left = (barWidth!! + spaceWidth!!) * index + realPaddingStart
            val right = left + barWidth!!
            val top = (height - barHeight) / HALF
            val bottom = top + barHeight

            val rect = RectF(left, top, right, bottom)
            val paint = if (progressToX(progress) > left + barWidth!! / HALF) paintRight else paintLeft

            tracker.x = trackerPosition(progressToX(progress)) - tracker.width / HALF

            canvas.drawRoundRect(rect, barWidth!! / HALF, barWidth!! / HALF, paint)
        }
    }

    private fun trackerPosition(positionX: Float) =
        min(
            max(realPaddingStart.toFloat() + tracker.width / HALF, positionX),
            (width - realPaddingEnd - tracker.width / HALF).toFloat()
        )

    private fun progressToX(progress: Float): Float =
        (progress / ONE_HUNDRED) * seekWidth() + realPaddingStart

    private fun xToProgress(x: Float): Float {
        val croppedX = min(max(realPaddingStart.toFloat(), x), width - realPaddingEnd.toFloat())
        return ONE_HUNDRED * ((croppedX - realPaddingStart) / seekWidth())
    }
}

private val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()
