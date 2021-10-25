package io.getstream.chat.android.ui.message.list.reactions.edit.internal

import android.graphics.Canvas
import android.graphics.Paint
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
        drawBubbleRoundRect(canvas, bubblePaint)
        drawLargeTailBubble(canvas, bubblePaint)
        drawSmallTailBubble(canvas, bubblePaint)
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

    private fun drawLargeTailBubble(canvas: Canvas, paint: Paint) {
        val offset = editReactionsViewStyle.largeTailBubbleOffset.toFloat().let {
            if (isMyMessage) it else -it
        }
        canvas.drawCircle(
            (bubbleWidth / 2).toFloat() + offset,
            largeTailBubbleInitialPosition() + editReactionsViewStyle.largeTailBubbleCyOffset.toFloat(),
            editReactionsViewStyle.largeTailBubbleRadius.toFloat(),
            paint
        )
    }

    private fun drawSmallTailBubble(canvas: Canvas, paint: Paint) {
        val offset = editReactionsViewStyle.smallTailBubbleOffset.toFloat().let {
            if (isMyMessage) it else -it
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
