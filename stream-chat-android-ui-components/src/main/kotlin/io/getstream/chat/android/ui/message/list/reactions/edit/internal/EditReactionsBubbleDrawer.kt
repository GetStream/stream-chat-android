package io.getstream.chat.android.ui.message.list.reactions.edit.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.message.list.reactions.edit.EditReactionsViewStyle
import io.getstream.chat.android.ui.utils.isRtlLayout

private const val LARGE_TAIL_BUBBLE_OFFSET_CORRECTION_DP = 2

/**
 * Draws the edit reactions bubble.
 */
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

    /**
     * Draws the bubble of reactions choosing the correct direction.
     *
     * @param context [Context].
     * @param canvas [Canvas].
     * @param bubbleWidth The width of the bubble. This should be at least bigger than all the columns of reactions.
     * @param bubbleHeight The height of the bubble. This should be at least bigger than all the lines of reactions.
     * @param isMyMessage Whether this is the message of the current user or not.
     * @param isSingleReaction Whether there's only a single reaction of there are multiple reactions.
     */
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
        val isRtl = context.isRtlLayout

        drawBubbleRoundRect(canvas, bubblePaint)
        drawLargeTailBubble(canvas, bubblePaint, isRtl)
        drawSmallTailBubble(canvas, bubblePaint, isRtl)
    }

    /**
     * Draws the main bubble of reactions. The one that involves the reactions.
     *
     * @param canvas [Canvas].
     * @param paint [Paint].
     */
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

    /**
     * Draws the large tail bubble, normally right bellow the main bubble.
     *
     * @param canvas [Canvas].
     * @param paint [Paint].
     * @param isRtl If the bubble should be drawn with inverted direction.
     */
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

    /**
     * Parses the offset of the bubble.
     *
     * @param isRtl If the bubble offset should be drawn with inverted direction.
     * @param isMyMessage Whether this is the message of the current user or not.
     * @param offset The offset of the bubble.
     */
    private fun parseOffset(isRtl: Boolean, isMyMessage: Boolean, offset: Float): Float {
        return when {
            isMyMessage && !isRtl -> offset

            isMyMessage && isRtl -> -offset

            !isMyMessage && !isRtl -> -offset

            !isMyMessage && isRtl -> offset

            else -> offset
        }
    }

    /**
     * Draws the large tail bubble, normally right bellow the large tail bubble.
     *
     * @param canvas [Canvas].
     * @param paint [Paint].
     * @param isRtl If the bubble should be drawn with inverted direction.
     */
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

    /**
     * The large tail bubble initial position.
     */
    private fun largeTailBubbleInitialPosition(): Float {
        return bubbleHeight.toFloat() - LARGE_TAIL_BUBBLE_OFFSET_CORRECTION_DP.dpToPx()
    }
}
