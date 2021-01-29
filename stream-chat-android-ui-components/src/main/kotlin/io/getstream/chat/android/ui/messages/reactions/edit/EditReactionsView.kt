package io.getstream.chat.android.ui.messages.reactions.edit

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.messages.reactions.ReactionClickListener
import io.getstream.chat.android.ui.messages.reactions.ReactionItem
import io.getstream.chat.android.ui.messages.reactions.ReactionsAdapter
import io.getstream.chat.android.ui.utils.ReactionType

@InternalStreamChatApi
public class EditReactionsView : RecyclerView {

    private lateinit var reactionsViewStyle: EditReactionsViewStyle
    private lateinit var reactionsAdapter: ReactionsAdapter
    private lateinit var bubbleDrawer: EditReactionsBubbleDrawer

    private var reactionClickListener: ReactionClickListener? = null
    private var isMyMessage: Boolean = false

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

    public fun setMessage(message: Message, isMyMessage: Boolean) {
        this.isMyMessage = isMyMessage

        val reactionItems = ReactionType.values().map { reactionType ->
            ReactionItem(
                type = reactionType.type,
                isMine = message.ownReactions.any { it.type == reactionType.type },
                iconDrawableRes = reactionType.iconRes
            )
        }
        reactionsAdapter.submitList(reactionItems)
    }

    public fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bubbleDrawer.drawReactionsBubble(canvas, width, isMyMessage, true)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.reactionsViewStyle = EditReactionsViewStyle(context, attrs)
        this.bubbleDrawer = EditReactionsBubbleDrawer(reactionsViewStyle)

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        itemAnimator = null
        overScrollMode = View.OVER_SCROLL_NEVER
        setWillNotDraw(false)
        minimumHeight = reactionsViewStyle.totalHeight
        reactionsViewStyle.horizontalPadding.let {
            setPadding(it, 0, it, 0)
        }

        adapter = ReactionsAdapter(reactionsViewStyle.itemSize) {
            reactionClickListener?.onReactionClick(it)
        }.also { reactionsAdapter = it }
    }
}
