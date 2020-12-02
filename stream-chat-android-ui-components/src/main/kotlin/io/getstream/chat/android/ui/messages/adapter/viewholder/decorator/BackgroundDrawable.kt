package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

internal open class BackgroundDrawable(
    @ColorInt val color: Int,
    protected val topLeftCornerPx: Float,
    protected val topRightCornerPx: Float,
    protected val bottomRightCornerPx: Float,
    protected val bottomLeftCornerPx: Float
) : Drawable() {

    protected val paint: Paint

    init {
        require(topLeftCornerPx >= 0)
        require(topRightCornerPx >= 0)
        require(bottomLeftCornerPx >= 0)
        require(bottomRightCornerPx >= 0)

        paint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = this@BackgroundDrawable.color
        }
    }

    override fun draw(canvas: Canvas) {
        roundedRect(
            0f,
            0f,
            bounds.width().toFloat(),
            bounds.height().toFloat(),
            topLeftCornerPx,
            topRightCornerPx,
            bottomRightCornerPx,
            bottomLeftCornerPx
        ).let {
            canvas.drawPath(it, paint)
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    protected fun roundedRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        topLeftCornerPx: Float,
        topRightCornerPx: Float,
        bottomRightCornerPx: Float,
        bottomLeftCornerPx: Float
    ): Path {
        return Path().apply {
            val width = right - left
            val height = bottom - top
            moveTo(right, top + topRightCornerPx)
            // Top right corner
            rQuadTo(0f, -topRightCornerPx, -topRightCornerPx, -topRightCornerPx)
            rLineTo(-(width - topLeftCornerPx - topRightCornerPx), 0f)
            // Top left corner
            rQuadTo(-topLeftCornerPx, 0f, -topLeftCornerPx, topLeftCornerPx)
            rLineTo(0f, height - topLeftCornerPx - bottomLeftCornerPx)
            // Bottom left corner
            rQuadTo(0f, bottomLeftCornerPx, bottomLeftCornerPx, bottomLeftCornerPx)
            rLineTo(width - bottomRightCornerPx - bottomLeftCornerPx, 0f)
            // Bottom right corner
            rQuadTo(bottomRightCornerPx, 0f, bottomRightCornerPx, -bottomRightCornerPx)
            rLineTo(0f, -(height - bottomRightCornerPx - topRightCornerPx))
            close()
        }
    }
}

internal class BackgroundStrokeDrawable(
    @ColorInt color: Int,
    @ColorInt val strokeColor: Int,
    val strokeWidth: Float,
    topLeftCornerPx: Float,
    topRightCornerPx: Float,
    bottomRightCornerPx: Float,
    bottomLeftCornerPx: Float
) : BackgroundDrawable(color, topLeftCornerPx, topRightCornerPx, bottomRightCornerPx, bottomLeftCornerPx) {

    private val strokePaint = Paint().apply {
        this.color = strokeColor
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        // outer rect
        roundedRect(
            0f,
            0f,
            bounds.width().toFloat(),
            bounds.height().toFloat(),
            topLeftCornerPx,
            topRightCornerPx,
            bottomRightCornerPx,
            bottomLeftCornerPx
        ).let {
            canvas.drawPath(it, strokePaint)
        }

        // inner rect
        roundedRect(
            strokeWidth,
            strokeWidth,
            bounds.width().toFloat() - strokeWidth,
            bounds.height() - strokeWidth,
            topLeftCornerPx,
            topRightCornerPx,
            bottomRightCornerPx,
            bottomLeftCornerPx
        ).let { canvas.drawPath(it, paint) }
    }
}