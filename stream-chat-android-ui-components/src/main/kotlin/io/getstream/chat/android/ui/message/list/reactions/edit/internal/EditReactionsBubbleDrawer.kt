package io.getstream.chat.android.ui.message.list.reactions.edit.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.message.list.reactions.edit.EditReactionsViewStyle

private const val LARGE_TAIL_BUBBLE_OFFSET_CORRECTION_DP = 2

internal class EditReactionsBubbleDrawer(
    private val editReactionsViewStyle: EditReactionsViewStyle,
) {

    private val bubblePaintMine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = editReactionsViewStyle.bubbleColorMine
        style = Paint.Style.FILL
    }

    private val bubblePaintTheirs = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = editReactionsViewStyle.bubbleColorTheirs
        style = Paint.Style.FILL
    }

    private var bubbleWidth: Int = 0
    private var bubbleHeight: Int = 0
    private var isMyMessage: Boolean = false
    private var isSingleReaction: Boolean = false

    fun drawReactionsBubble(
        context: Context,
        canvas: Canvas,
        bubbleWidth: Int,
        bubbleHeight: Int,
        isMyMessage: Boolean,
        isSingleReaction: Boolean,
    ) {
        this.bubbleWidth = bubbleWidth
        this.bubbleHeight = bubbleHeight
        this.isMyMessage = isMyMessage
        this.isSingleReaction = isSingleReaction

        val bubblePaint = if (isMyMessage) bubblePaintMine else bubblePaintTheirs
        val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        drawBubbleRoundRect(canvas, bubblePaint)
        drawLargeTailBubble(canvas, bubblePaint, isRtl)
        drawSmallTailBubble(canvas, bubblePaint, isRtl)
    }

    private fun drawBubbleRoundRect(canvas: Canvas, paint: Paint) {
        canvas.drawRoundRect(
            0f,
            0f,
            bubbleWidth.toFloat(),
            bubbleHeight.toFloat(),
            editReactionsViewStyle.bubbleRadius.toFloat(),
            editReactionsViewStyle.bubbleRadius.toFloat(),
            paint
        )
    }

    private fun drawLargeTailBubble(canvas: Canvas, paint: Paint, isRtl: Boolean) {
        val offset = editReactionsViewStyle.largeTailBubbleOffset.toFloat().let { bubbleOffset ->
            parseOffset(isRtl, isMyMessage, bubbleOffset)
        }
        canvas.drawCircle(
            (bubbleWidth / 2).toFloat() + offset,
            largeTailBubbleInitialPosition() + editReactionsViewStyle.largeTailBubbleCyOffset.toFloat(),
            editReactionsViewStyle.largeTailBubbleRadius.toFloat(),
            paint
        )
    }

    private fun parseOffset(isRtl: Boolean, isMyMessage: Boolean, offset: Float): Float {
        return when {
            isMyMessage && !isRtl -> offset

            isMyMessage && isRtl -> -offset

            !isMyMessage && !isRtl -> -offset

            !isMyMessage && isRtl -> offset

            else -> offset
        }
    }

    private fun drawSmallTailBubble(canvas: Canvas, paint: Paint, isRtl: Boolean) {
        val offset = editReactionsViewStyle.smallTailBubbleOffset.toFloat().let { bubbleOffset ->
            parseOffset(isRtl, isMyMessage, bubbleOffset)
        }

        canvas.drawCircle(
            bubbleWidth / 2 + offset,
            largeTailBubbleInitialPosition() +
                editReactionsViewStyle.largeTailBubbleRadius.toFloat() +
                editReactionsViewStyle.smallTailBubbleCyOffset.toFloat(),
            editReactionsViewStyle.smallTailBubbleRadius.toFloat(),
            paint
        )
    }

    private fun largeTailBubbleInitialPosition(): Float {
        return bubbleHeight.toFloat() - LARGE_TAIL_BUBBLE_OFFSET_CORRECTION_DP.dpToPx()
    }
}
