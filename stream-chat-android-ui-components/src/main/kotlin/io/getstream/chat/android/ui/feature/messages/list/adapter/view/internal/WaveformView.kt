package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.log.taggedLogger

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
internal class WaveformView : LinearLayoutCompat {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val logger by taggedLogger("WaveformView")

    private val tracker: ImageView

    init {
        orientation = HORIZONTAL

        tracker = ImageView(context).apply {
            setBackgroundResource(R.drawable.stream_ui_share_rectangle)
        }
        tracker.visibility = View.INVISIBLE

        val layoutParamsButton = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        addView(tracker, layoutParamsButton)

        setWillNotDraw(false)
    }

    private var barWidth: Float? = null
    private var spaceWidth: Float? = null
    private var maxHeight: Int? = null
    private val barSpacing = DEFAULT_BAR_SPACING
    private var barHeightRatio: Float = DEFAULT_BAR_HEIGHT_RATIO
    private var onStartDrag: () -> Unit = {}
    private var onEndDrag: (Int) -> Unit = {}
    private var isDragging = false

    private val viewportWidth: Int get() = width - paddingStart - paddingEnd

    private val paintLeft = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stream_ui_accent_blue)
        style = Paint.Style.FILL
    }

    private val paintRight = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stream_ui_grey)
        style = Paint.Style.FILL
    }

    private val waveformData = arrayListOf<Float>()

    public fun setData(data: List<Float>) {
        this.waveformData.clear()
        this.waveformData.addAll(data)
        invalidate()
    }

    public fun clearData() {
        this.waveformData.clear()
        invalidate()
    }

    public fun addValue(normalized: Float) {
        if (normalized > 1 || normalized < 0) {
            logger.w { "[addValue] rejected (Normalized value must be between 0 and 1): $normalized" }
            return
        }
        this.waveformData.add(normalized)
        invalidate()
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

        val viewportWidth = measuredWidth - paddingStart - paddingEnd
        val totalBarWidth = viewportWidth * (1 - barSpacing)
        val totalSpaceWidth = viewportWidth * barSpacing
        val barCount = waveformData.size

        barWidth = totalBarWidth.toFloat() / barCount
        spaceWidth = totalSpaceWidth.toFloat() / barCount
        maxHeight = measuredHeight - paddingTop - paddingBottom
    }

    private val barRect = RectF()

    /**
     * In onDraw all the bars are drawn and the tracker position is calculated.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val barW = 2.dpToPx()
        val spacerW = 1.dpToPx()
        val occupiedW = barW + spacerW

        val maxBarHeight = height - paddingBottom - paddingTop
        val centerY = height / 2f
        val maxEnd = width - paddingEnd
        val minStart = paddingStart

        val maxBarCount = (maxEnd - minStart) / occupiedW + 1
        val minVisibleIndex = maxOf((waveformData.size - maxBarCount), 0)

        var deltaX = 0f
        for (index in waveformData.lastIndex downTo minVisibleIndex) {
            val value = waveformData[index]
            val barHeight = maxOf(maxBarHeight * value, barW.toFloat())

            val relativeIndex = index - minVisibleIndex
            val top = centerY - barHeight / 2
            val bottom = centerY + barHeight / 2
            val start = (minStart + occupiedW * relativeIndex).toFloat()
            val end = start + barW



            val rx = barW / 2f
            val ry = rx

            barRect.set(start, top, end, bottom)
            // if (deltaX == 0f && end > maxEnd) {
            //     deltaX = maxEnd - end
            //     logger.i { "[onDraw] index: $index, end: $end($maxEnd) -> end is out of viewport" }
            // }
            // if (deltaX != 0f) {
            //     logger.v { "[onDraw] index: $index, deltaX: $deltaX -> moving into viewport" }
            //     barRect.offset(deltaX, 0f)
            // }


            if (barRect.left < minStart) {
                barRect.left = minStart.toFloat()
            } else if (barRect.right > maxEnd) {
                barRect.right = maxEnd.toFloat()
            }

            canvas.drawRoundRect(barRect, rx, ry, paintLeft)
        }
    }
}