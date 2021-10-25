package io.getstream.chat.android.ui.message.list.reactions.edit.internal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.message.list.reactions.ReactionClickListener
import io.getstream.chat.android.ui.message.list.reactions.edit.EditReactionsViewStyle
import io.getstream.chat.android.ui.message.list.reactions.internal.ReactionItem
import io.getstream.chat.android.ui.message.list.reactions.internal.ReactionsAdapter
import kotlin.math.ceil

private const val TAIL_BUBBLE_SPACE_DP = 16

@InternalStreamChatApi
public class EditReactionsView : RecyclerView {

    private lateinit var reactionsViewStyle: EditReactionsViewStyle
    private lateinit var reactionsAdapter: ReactionsAdapter
    private lateinit var bubbleDrawer: EditReactionsBubbleDrawer

    private var reactionClickListener: ReactionClickListener? = null
    private var isMyMessage: Boolean = false

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private var bubbleHeight: Int = 0
    private var reactionsColumns: Int = 5

    public fun setMessage(message: Message, isMyMessage: Boolean) {
        this.isMyMessage = isMyMessage

        val reactionItems = ChatUI.supportedReactions.reactions.map { (type, reactionDrawable) ->
            ReactionItem(
                type = type,
                isMine = message.ownReactions.any { it.type == type },
                reactionDrawable = reactionDrawable
            )
        }

        if (reactionItems.size > reactionsColumns) {
            val timesBigger = ceil(reactionItems.size.toFloat() / reactionsColumns).toInt()
            bubbleHeight = bubbleHeight.times(timesBigger)
        }

        minimumHeight = bubbleHeight + TAIL_BUBBLE_SPACE_DP.dpToPx()

        reactionsAdapter.submitList(reactionItems)
    }

    public fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bubbleDrawer.drawReactionsBubble(canvas, width, bubbleHeight, isMyMessage, true)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val style = EditReactionsViewStyle(context, attrs)
        applyStyle(style)

        bubbleHeight = style.bubbleHeight

        itemAnimator = null
        overScrollMode = View.OVER_SCROLL_NEVER
        setWillNotDraw(false)
    }

    internal fun applyStyle(editReactionsViewStyle: EditReactionsViewStyle) {
        this.reactionsViewStyle = editReactionsViewStyle
        this.bubbleDrawer = EditReactionsBubbleDrawer(reactionsViewStyle)

        reactionsColumns = editReactionsViewStyle.reactionsColumn

        reactionsViewStyle.horizontalPadding.let {
            setPadding(it, 0, it, 0)
        }

        layoutManager = GridLayoutManager(context, reactionsColumns)

        adapter = ReactionsAdapter(reactionsViewStyle.itemSize) {
            reactionClickListener?.onReactionClick(it)
        }.also { reactionsAdapter = it }
    }
}
