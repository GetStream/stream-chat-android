package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.ui.R
import java.lang.Float.min
import kotlin.math.max
import kotlin.math.roundToInt

private const val MIN_BAR_VALUE = 0.05F

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

        setOnTouchListener(DragListener())

        tracker = ImageView(context).apply {
            setBackgroundResource(R.drawable.stream_ui_share_rectangle)
        }

        val layoutParamsButton = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        addView(tracker, layoutParamsButton)
    }

    private val barPadding = 5.dp
    private val realPaddingStart = paddingStart + barPadding
    private val realPaddingEnd = paddingEnd + barPadding
    private var barWidth: Float? = null
    private var spaceWidth: Float? = null
    private var maxHeight: Int? = null
    private val barSpacing = 0.4

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
            repeat(40) {
                add(0F)
            }
        }

    private var progress: Float = 0F

    private var progressCorrection = 1F

    public fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val totalWidth = measuredWidth - realPaddingStart - realPaddingEnd
        val totalBarWidth = totalWidth * (1 - barSpacing)
        val totalSpaceWidth = totalWidth * barSpacing
        val barCount = waveBars.size

        barWidth = totalBarWidth.toFloat() / barCount
        spaceWidth = totalSpaceWidth.toFloat() / barCount
        maxHeight = measuredHeight - paddingTop - paddingBottom

        progressCorrection = barCount / 100F
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        waveBars.forEachIndexed { index, barValue ->
            val barHeight = maxHeight!! * max(barValue, MIN_BAR_VALUE)

            val left = (barWidth!! + spaceWidth!!) * index + realPaddingStart
            val right = left + barWidth!!
            val top = (height - barHeight) / 2
            val bottom = top + barHeight

            val rect = RectF(left, top, right, bottom)
            val paint = if (progressToX(progress) > left + barWidth!! / 2f) paintRight else paintLeft

            tracker.x = trackerPosition(progressToX(progress)) - tracker.width / 2

            canvas.drawRoundRect(rect, barWidth!! / 2f, barWidth!! / 2f, paint)
        }
    }

    private fun trackerPosition(positionX: Float) =
        min(
            max(realPaddingStart.toFloat() + tracker.width / 2, positionX),
            (width - realPaddingEnd - tracker.width / 2).toFloat()
        )

    private fun progressToX(progress: Float): Float =
        (progress / 100) * seekWidth() + realPaddingStart

    private fun xToProgress(x: Float): Float {
        val croppedX = min(max(realPaddingStart.toFloat(), x), width - realPaddingEnd.toFloat())
        return 100F * ((croppedX - realPaddingStart) / seekWidth())
    }

    private inner class DragListener : OnTouchListener {
        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            return when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    tracker.updateLayoutParams {
                        width += 10.dp
                    }
                    setProgress(xToProgress(motionEvent.x))
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    setProgress(xToProgress(motionEvent.x))
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    tracker.updateLayoutParams {
                        width -= 10.dp
                    }
                    performClick()
                }

                else -> false
            }
        }
    }
}

private val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()
