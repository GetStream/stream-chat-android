package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.isSingleReaction

public class ViewReactionsView : ReactionsView {

    // one dp stroke looks too thin
    private val bubbleStrokeWidth: Float = 1.dpToPx() * 1.5f

    private val bubblePaintTheirs = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val bubblePaintMine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val bubbleStrokePaintMine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = bubbleStrokeWidth
        style = Paint.Style.STROKE
    }

    private var isSingleReaction: Boolean = true

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
        ViewReactionsViewStyle(context, attrs).apply {
            setStyle(this)
            bubblePaintTheirs.color = bubbleColorTheirs
            bubblePaintMine.color = bubbleColorMine
            bubbleStrokePaintMine.color = bubbleBorderColor
        }
    }

    override fun createReactionItems(message: Message, isMyMessage: Boolean): List<ReactionItem> {
        val reactionsMap = mutableMapOf<String, ReactionItem>()
        message.latestReactions.forEach { reaction ->
            val ownReaction = message.ownReactions.any { it.type == reaction.type }
            val alreadyPresent = reactionsMap.containsKey(reaction.type)
            if (!alreadyPresent || ownReaction) {
                reactionsMap[reaction.type] = ReactionItem(reaction, ownReaction)
            }
        }
        return reactionsMap.values
            .toList()
            .sortedBy { !it.isMine }
    }

    override fun setMessage(message: Message, isMyMessage: Boolean) {
        super.setMessage(message, isMyMessage)
        isSingleReaction = message.isSingleReaction()

        val horizontalPadding = if (message.latestReactions.size == 1) {
            0
        } else {
            reactionsViewStyle.horizontalPadding
        }
        setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    override fun drawReactionsBubble(canvas: Canvas, isMyMessage: Boolean, isMirrored: Boolean) {
        val path = Path().apply {
            op(createBubbleRoundRectPath(isMyMessage = isMyMessage), Path.Op.UNION)
            op(createLargeTailBubblePath(isMirrored = isMirrored), Path.Op.UNION)
            op(createSmallTailBubblePath(isMirrored = isMirrored), Path.Op.UNION)
        }
        if (isMyMessage) {
            canvas.drawPath(path, bubblePaintMine)
            canvas.drawPath(path, bubbleStrokePaintMine)
        } else {
            canvas.drawPath(path, bubblePaintTheirs)
        }
    }

    private fun createBubbleRoundRectPath(isMyMessage: Boolean): Path {
        val strokeOffset: Float = if (isMyMessage) {
            bubbleStrokeWidth / 2
        } else {
            0f
        }
        return Path().apply {
            addRoundRect(
                strokeOffset,
                strokeOffset,
                width.toFloat() - strokeOffset,
                reactionsViewStyle.bubbleHeight.toFloat(),
                reactionsViewStyle.bubbleRadius.toFloat(),
                reactionsViewStyle.bubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    private fun createLargeTailBubblePath(isMirrored: Boolean): Path {
        return Path().apply {
            addCircle(
                calculateBubbleCenterX(isMirrored, reactionsViewStyle.largeTailBubbleOffset.toFloat()),
                reactionsViewStyle.largeTailBubbleCy.toFloat(),
                reactionsViewStyle.largeTailBubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    private fun createSmallTailBubblePath(isMirrored: Boolean): Path {
        return Path().apply {
            addCircle(
                calculateBubbleCenterX(isMirrored, reactionsViewStyle.smallTailBubbleOffset.toFloat()),
                reactionsViewStyle.smallTailBubbleCy.toFloat(),
                reactionsViewStyle.smallTailBubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    private fun calculateBubbleCenterX(isMirrored: Boolean, bubbleOffset: Float): Float {
        return if (isMirrored) {
            if (isSingleReaction) {
                width / 2 + bubbleOffset
            } else {
                width + bubbleOffset - MULTIPLE_REACTIONS_BASELINE_OFFSET
            }
        } else {
            if (isSingleReaction) {
                width / 2 - bubbleOffset
            } else {
                MULTIPLE_REACTIONS_BASELINE_OFFSET - bubbleOffset
            }
        }
    }

    private companion object {
        private val MULTIPLE_REACTIONS_BASELINE_OFFSET = 32.dpToPx()
    }
}
