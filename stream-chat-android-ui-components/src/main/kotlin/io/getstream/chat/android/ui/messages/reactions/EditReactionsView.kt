package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.ui.utils.UiUtils
import io.getstream.chat.android.ui.utils.extensions.isMineReactionOfType

public class EditReactionsView : ReactionsView {
    private val bubblePaintMine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val bubblePaintTheirs = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
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
        EditReactionsViewStyle(context, attrs).apply {
            setStyle(this)
            bubblePaintMine.color = bubbleColorMine
            bubblePaintTheirs.color = bubbleColorTheirs
        }
    }

    override fun createReactionItems(message: Message, isMyMessage: Boolean): List<ReactionItem> {
        return UiUtils.getReactionTypes().keys.map { reactionType ->
            message.latestReactions
                .any { it.isMineReactionOfType(reactionType) }
                .let { isMine -> ReactionItem(Reaction(type = reactionType), isMine) }
        }
    }

    override fun drawReactionsBubble(canvas: Canvas, isMyMessage: Boolean, isMirrored: Boolean) {
        val bubblePaint = if (isMyMessage) bubblePaintMine else bubblePaintTheirs
        drawBubbleRoundRect(canvas, bubblePaint)
        drawLargeTailBubble(canvas, bubblePaint, isMirrored)
        drawSmallTailBubble(canvas, bubblePaint, isMirrored)
    }

    private fun drawBubbleRoundRect(canvas: Canvas, paint: Paint) {
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            reactionsViewStyle.bubbleHeight.toFloat(),
            reactionsViewStyle.bubbleRadius.toFloat(),
            reactionsViewStyle.bubbleRadius.toFloat(),
            paint
        )
    }

    private fun drawLargeTailBubble(canvas: Canvas, paint: Paint, isMirrored: Boolean) {
        val offset = reactionsViewStyle.largeTailBubbleOffset.toFloat().let {
            if (isMirrored) it else -it
        }
        canvas.drawCircle(
            (width / 2).toFloat() + offset,
            reactionsViewStyle.largeTailBubbleCy.toFloat(),
            reactionsViewStyle.largeTailBubbleRadius.toFloat(),
            paint
        )
    }

    private fun drawSmallTailBubble(canvas: Canvas, paint: Paint, isMirrored: Boolean) {
        val offset = reactionsViewStyle.smallTailBubbleOffset.toFloat().let {
            if (isMirrored) it else -it
        }
        canvas.drawCircle(
            width / 2 + offset,
            reactionsViewStyle.smallTailBubbleCy.toFloat(),
            reactionsViewStyle.smallTailBubbleRadius.toFloat(),
            paint
        )
    }
}
