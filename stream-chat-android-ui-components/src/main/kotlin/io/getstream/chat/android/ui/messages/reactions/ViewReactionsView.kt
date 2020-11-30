package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getColorCompat

public class ViewReactionsView : ReactionsView {

    private val theirsMessageReactionsBubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColorCompat(R.color.stream_grey_90)
    }

    private val myMessageReactionsBubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val myMessageReactionsBubbleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // one dp stroke looks too thin
        strokeWidth = 1.dpToPx() * 1.5f
        style = Paint.Style.STROKE
        color = context.getColorCompat(R.color.stream_grey_90)
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
        setStyle(ViewReactionsViewStyle(context, attrs))
    }

    override fun createReactionItems(message: Message, isMyMessage: Boolean): List<ReactionsAdapter.ReactionItem> {
        val reactionsMap = mutableMapOf<String, ReactionsAdapter.ReactionItem>()
        message.latestReactions.forEach { reaction ->
            val ownReaction = message.ownReactions.any { it.type == reaction.type }
            val alreadyPresent = reactionsMap.containsKey(reaction.type)
            if (!alreadyPresent || ownReaction) {
                reactionsMap[reaction.type] = ReactionsAdapter.ReactionItem(reaction, ownReaction)
            }
        }
        return reactionsMap.values
            .toList()
            .sortedBy { !it.isMine }
    }

    override fun setMessage(message: Message, isMyMessage: Boolean) {
        super.setMessage(message, isMyMessage)
        val horizontalPadding = if (message.latestReactions.size == 1) {
            0
        } else {
            reactionsViewStyle.horizontalPadding
        }
        setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    override fun drawReactionsBubble(canvas: Canvas, isMyMessage: Boolean) {
        val path = Path().apply {
            op(createBubbleRoundRectPath(), Path.Op.UNION)
            op(createLargeTailBubblePath(isMyMessage), Path.Op.UNION)
            op(createSmallTailBubblePath(isMyMessage), Path.Op.UNION)
        }
        if (isMyMessage) {
            canvas.drawPath(path, myMessageReactionsBubblePaint)
            canvas.drawPath(path, myMessageReactionsBubbleStrokePaint)
        } else {
            canvas.drawPath(path, theirsMessageReactionsBubblePaint)
        }
    }

    private fun createBubbleRoundRectPath(): Path {
        return Path().apply {
            addRoundRect(
                0f,
                0f,
                width.toFloat(),
                reactionsViewStyle.bubbleHeight.toFloat(),
                reactionsViewStyle.bubbleRadius.toFloat(),
                reactionsViewStyle.bubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    private fun createLargeTailBubblePath(isMyMessage: Boolean): Path {
        val offset = reactionsViewStyle.largeTailBubbleOffset.toFloat().let {
            if (isMyMessage) it else -it
        }
        return Path().apply {
            addCircle(
                width / 2 + offset,
                reactionsViewStyle.largeTailBubbleCy.toFloat(),
                reactionsViewStyle.largeTailBubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    private fun createSmallTailBubblePath(isMyMessage: Boolean): Path {
        val offset = reactionsViewStyle.smallTailBubbleOffset.toFloat().let {
            if (isMyMessage) it else -it
        }
        return Path().apply {
            addCircle(
                width / 2 + offset,
                reactionsViewStyle.smallTailBubbleCy.toFloat(),
                reactionsViewStyle.smallTailBubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }
}
