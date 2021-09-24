package io.getstream.chat.android.ui.message.list.reactions.view.internal

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle

internal class ViewReactionsBubbleDrawer(
    private val viewReactionsViewStyle: ViewReactionsViewStyle,
) {

    private val bubblePaintTheirs = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = viewReactionsViewStyle.bubbleColorTheirs
        style = Paint.Style.FILL
    }
    private val bubblePaintMine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = viewReactionsViewStyle.bubbleColorMine
        style = Paint.Style.FILL
    }
    private val bubbleStrokePaintMine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = viewReactionsViewStyle.bubbleBorderColor ?: viewReactionsViewStyle.bubbleBorderColorMine
        strokeWidth = viewReactionsViewStyle.bubbleBorderWidthMine
        style = Paint.Style.STROKE
    }

    private val bubbleStrokePaintTheirs by lazy {
        check(shouldDrawTheirsBorder()) {
            "You need to specify either bubbleBorderColorTheirs and bubbleBorderWidthTheirs to draw a border for another user reaction bubble"
        }
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = viewReactionsViewStyle.bubbleBorderColorTheirs!!
            strokeWidth = viewReactionsViewStyle.bubbleBorderWidthTheirs!!
            style = Paint.Style.STROKE
        }
    }

    private var bubbleWidth: Int = 0
    private var isMyMessage: Boolean = false
    private var isSingleReaction: Boolean = false

    fun drawReactionsBubble(
        canvas: Canvas,
        bubbleWidth: Int,
        isMyMessage: Boolean,
        isSingleReaction: Boolean,
        inverseBubbleStyle: Boolean = false,
    ) {
        this.isMyMessage = isMyMessage
        this.bubbleWidth = bubbleWidth
        this.isSingleReaction = isSingleReaction

        val path = Path().apply {
            op(createBubbleRoundRectPath(), Path.Op.UNION)
            op(createLargeTailBubblePath(), Path.Op.UNION)
            op(createSmallTailBubblePath(), Path.Op.UNION)
        }

        val outlineStyle = if (inverseBubbleStyle) !isMyMessage else isMyMessage
        if (outlineStyle) {
            canvas.drawPath(path, bubblePaintMine)
            canvas.drawPath(path, bubbleStrokePaintMine)
        } else {
            canvas.drawPath(path, bubblePaintTheirs)
            if (shouldDrawTheirsBorder()) {
                canvas.drawPath(path, bubbleStrokePaintTheirs)
            }
        }
    }

    private fun shouldDrawTheirsBorder(): Boolean =
        viewReactionsViewStyle.bubbleBorderColorTheirs != null && viewReactionsViewStyle.bubbleBorderWidthTheirs != null

    private fun createBubbleRoundRectPath(): Path {
        val strokeOffset = getStrokeOffset()
        return Path().apply {
            addRoundRect(
                strokeOffset,
                strokeOffset,
                bubbleWidth.toFloat() - strokeOffset,
                viewReactionsViewStyle.bubbleHeight.toFloat(),
                viewReactionsViewStyle.bubbleRadius.toFloat(),
                viewReactionsViewStyle.bubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    private fun getStrokeOffset(): Float {
        return when {
            isMyMessage -> {
                viewReactionsViewStyle.bubbleBorderWidthMine / 2
            }
            shouldDrawTheirsBorder() -> {
                viewReactionsViewStyle.bubbleBorderWidthTheirs!! / 2
            }
            else -> {
                0f
            }
        }
    }

    private fun createLargeTailBubblePath(): Path {
        return Path().apply {
            addCircle(
                calculateBubbleCenterX(viewReactionsViewStyle.largeTailBubbleOffset.toFloat()),
                viewReactionsViewStyle.largeTailBubbleCy.toFloat(),
                viewReactionsViewStyle.largeTailBubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    private fun createSmallTailBubblePath(): Path {
        return Path().apply {
            addCircle(
                calculateBubbleCenterX(viewReactionsViewStyle.smallTailBubbleOffset.toFloat()),
                viewReactionsViewStyle.smallTailBubbleCy.toFloat(),
                viewReactionsViewStyle.smallTailBubbleRadius.toFloat() - getStrokeOffset(),
                Path.Direction.CW
            )
        }
    }

    private fun calculateBubbleCenterX(bubbleOffset: Float): Float {
        return if (isMyMessage) {
            if (isSingleReaction) {
                bubbleWidth / 2 + bubbleOffset
            } else {
                bubbleWidth + bubbleOffset - MULTIPLE_REACTIONS_BASELINE_OFFSET
            }
        } else {
            if (isSingleReaction) {
                bubbleWidth / 2 - bubbleOffset
            } else {
                MULTIPLE_REACTIONS_BASELINE_OFFSET - bubbleOffset
            }
        }
    }

    private companion object {
        private val MULTIPLE_REACTIONS_BASELINE_OFFSET = 32.dpToPx()
    }
}
