package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.Utils.dpToPx
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder

internal class BackgroundDecorator : BaseDecorator() {

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        val context = viewHolder.itemView.context
        val backgroundColor = ContextCompat.getColor(context, MESSAGE_DELETED_BACKGROUND)
        val radius = dpToPx(DEFAULT_CORNER_RADIUS.toInt()).toFloat()
        viewHolder.binding.deleteLabel.background = BackgroundDrawable(
            color = backgroundColor,
            topLeftCornerPx = radius,
            topRightCornerPx = radius,
            bottomRightCornerPx = if (data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else radius,
            bottomLeftCornerPx = radius
        )
    }

    private class BackgroundDrawable(
        @ColorInt
        val color: Int,
        val topLeftCornerPx: Float,
        val topRightCornerPx: Float,
        val bottomRightCornerPx: Float,
        val bottomLeftCornerPx: Float
    ) : Drawable() {

        init {
            require(topLeftCornerPx >= 0)
            require(topRightCornerPx >= 0)
            require(bottomLeftCornerPx >= 0)
            require(bottomRightCornerPx >= 0)
        }

        private val paint = Paint().apply {
            color = this@BackgroundDrawable.color
            isAntiAlias = true
            style = Paint.Style.FILL
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

        private fun roundedRect(
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

    companion object {
        private val MESSAGE_DELETED_BACKGROUND = R.color.stream_grey_light_opacity_50
        private const val DEFAULT_CORNER_RADIUS = 16f
    }
}
