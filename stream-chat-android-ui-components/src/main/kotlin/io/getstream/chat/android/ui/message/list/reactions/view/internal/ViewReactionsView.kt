package io.getstream.chat.android.ui.message.list.reactions.view.internal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.UiUtils
import io.getstream.chat.android.ui.common.extensions.hasSingleReaction
import io.getstream.chat.android.ui.common.extensions.supportedReactionCounts
import io.getstream.chat.android.ui.message.list.reactions.ReactionClickListener
import io.getstream.chat.android.ui.message.list.reactions.internal.ReactionItem
import io.getstream.chat.android.ui.message.list.reactions.internal.ReactionsAdapter

@InternalStreamChatApi
public class ViewReactionsView : RecyclerView {

    private lateinit var reactionsViewStyle: ViewReactionsViewStyle
    private lateinit var reactionsAdapter: ReactionsAdapter
    private lateinit var bubbleDrawer: ViewReactionsBubbleDrawer

    private var reactionClickListener: ReactionClickListener? = null
    private var isMyMessage: Boolean = false
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

    public fun setMessage(message: Message, isMyMessage: Boolean, commitCallback: () -> Unit = {}) {
        this.isMyMessage = isMyMessage
        this.isSingleReaction = message.hasSingleReaction()

        reactionsAdapter.submitList(createReactionItems(message)) {
            val horizontalPadding = if (isSingleReaction) 0 else reactionsViewStyle.horizontalPadding
            setPadding(horizontalPadding, 0, horizontalPadding, 0)

            commitCallback()
        }
    }

    public fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bubbleDrawer.drawReactionsBubble(canvas, width, isMyMessage, isSingleReaction)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.reactionsViewStyle = ViewReactionsViewStyle(context, attrs)
        this.bubbleDrawer = ViewReactionsBubbleDrawer(reactionsViewStyle)

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

    private fun createReactionItems(message: Message): List<ReactionItem> {
        return message.supportedReactionCounts.keys
            .mapNotNull { type ->
                UiUtils.getReactionIcon(type)?.let {
                    ReactionItem(
                        type = type,
                        isMine = message.ownReactions.any { it.type == type },
                        iconDrawableRes = it
                    )
                }
            }.sortedBy { if (isMyMessage) it.isMine else !it.isMine }
    }
}
