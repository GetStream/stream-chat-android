package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.extensions.limitTo
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.log.taggedLogger
import kotlin.math.pow
import kotlin.math.sqrt

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

    private val slider: ImageView

    init {
        orientation = HORIZONTAL

        slider = ImageView(context).apply {
            setBackgroundResource(R.drawable.stream_ui_share_rectangle)
            isVisible = false
        }

        val layoutParamsButton = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        addView(slider, layoutParamsButton)

        setWillNotDraw(false)
    }

    private var barLimit = 100
    private var barWidth: Float? = null
    private var spaceWidth: Float? = null
    private var maxHeight: Int? = null
    private val barSpacing = DEFAULT_BAR_SPACING
    private var barHeightRatio: Float = DEFAULT_BAR_HEIGHT_RATIO
    private var onStartDrag: () -> Unit = {}
    private var onEndDrag: (Int) -> Unit = {}
    private var isDragging = false

    public var onSliderProgressChange: (Float) -> Unit = {}

    private val paintPassed = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stream_ui_accent_blue)
        style = Paint.Style.FILL
    }

    private val paintUpcoming = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stream_ui_grey)
        style = Paint.Style.FILL
    }

    private val _waveform = arrayListOf<Float>()

    public var progress: Float = INITIAL_PROGRESS
        set(value) {
            field = value
            invalidate()
        }

    public var isSliderVisible: Boolean
        get() = slider.visibility == View.VISIBLE
        set(value) {
            slider.isVisible = value
        }

    public var waveform: List<Float>
        get() = _waveform
        set(value) {
            _waveform.clear()
            _waveform.addAll(value)
            invalidate()
        }

    public fun clearData() {
        this._waveform.clear()
        invalidate()
    }

    public fun addValue(normalized: Float) {
        if (normalized > 1 || normalized < 0) {
            logger.w { "[addValue] rejected (Normalized value must be between 0 and 1): $normalized" }
            return
        }
        this._waveform.add(normalized)
        invalidate()
    }

    internal fun updateProgress(progress: Float) {
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

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                slider.updateLayoutParams {
                    width += EXPAND_TRACKER_WIDTH.dpToPx()
                }
                progress = event.x / viewportRect.width()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                progress = event.x / viewportRect.width()
                onSliderProgressChange(progress)
                true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
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

        val progressX = viewportW * progress

        var deltaX = 0f
        for (index in _waveform.lastIndex downTo minVisibleIndex) {
            val value = _waveform[index]
            val barHeight = maxOf(maxBarHeight * value, barW.toFloat())

            val relativeIndex = index - minVisibleIndex
            val top = centerY - barHeight / 2
            val bottom = centerY + barHeight / 2
            val start = (minStart + perBarW * relativeIndex).toFloat()
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

            val passed = !isSliderVisible || barRect.centerX() < progressX
            canvas.drawRoundRect(barRect, rx, ry, if (passed) paintPassed else paintUpcoming)
        }
        val sliderX = progressX - slider.width / 2f
        val finalSliderX = sliderX.limitTo(viewportRect.left, viewportRect.right - slider.width)
        slider.translationX = finalSliderX
    }
}

private fun List<Float>.downsampleRms(targetSamples: Int): List<Float> {
    val sourceSamples = size
    val sourceStep = sourceSamples / targetSamples
    val target = ArrayList<Float>(targetSamples)
    for (targetIndex in 0 until targetSamples) {
        var sum = 0f
        for (sourceIndex in 0 until sourceStep) {
            val sourceSample = this[targetIndex * sourceStep + sourceIndex]
            sum += sourceSample.pow(2)
        }
        target.add(sqrt(sum / sourceStep))
    }
    return target
}