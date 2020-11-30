package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.ui.utils.UiUtils

public class EditReactionsView : ReactionsView {
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    public constructor(context: Context) : super(context) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        setStyle(EditReactionsViewStyle(context, attrs))
    }

    override fun createReactionItems(message: Message, isMyMessage: Boolean): List<ReactionsAdapter.ReactionItem> {
        return UiUtils.getReactionTypes().keys.map { reactionType ->
            message.ownReactions
                .any { it.type == reactionType }
                .let { ReactionsAdapter.ReactionItem(Reaction(type = reactionType), it) }
        }
    }

    override fun drawReactionsBubble(canvas: Canvas, isMyMessage: Boolean) {
        drawBubbleRoundRect(canvas)
        drawLargeTailBubble(canvas, isMyMessage)
        drawSmallTailBubble(canvas, isMyMessage)
    }

    private fun drawBubbleRoundRect(canvas: Canvas) {
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            reactionsViewStyle.bubbleHeight.toFloat(),
            reactionsViewStyle.bubbleRadius.toFloat(),
            reactionsViewStyle.bubbleRadius.toFloat(),
            bubblePaint
        )
    }

    private fun drawLargeTailBubble(canvas: Canvas, isMyMessage: Boolean) {
        val offset = reactionsViewStyle.largeTailBubbleOffset.toFloat().let {
            if (isMyMessage) it else -it
        }
        canvas.drawCircle(
            (width / 2).toFloat() + offset,
            reactionsViewStyle.largeTailBubbleCy.toFloat(),
            reactionsViewStyle.largeTailBubbleRadius.toFloat(),
            bubblePaint
        )
    }

    private fun drawSmallTailBubble(canvas: Canvas, isMyMessage: Boolean) {
        val offset = reactionsViewStyle.smallTailBubbleOffset.toFloat().let {
            if (isMyMessage) it else -it
        }
        canvas.drawCircle(
            width / 2 + offset,
            reactionsViewStyle.smallTailBubbleCy.toFloat(),
            reactionsViewStyle.smallTailBubbleRadius.toFloat(),
            bubblePaint
        )
    }
}
