package io.getstream.chat.android.ui.message.list.reactions.edit.internal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.view.updateLayoutParams
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

    public fun setMessage(message: Message, isMyMessage: Boolean) {
        this.isMyMessage = isMyMessage

        val reactionItems = ChatUI.supportedReactions.reactions.map { (type, reactionDrawable) ->
            ReactionItem(
                type = type,
                isMine = message.ownReactions.any { it.type == type },
                reactionDrawable = reactionDrawable
            )
        }

        if (reactionItems.size > 5) {
            val timesBigger = ceil(reactionItems.size.toFloat() / 5).toInt()
            bubbleHeight = bubbleHeight.times(timesBigger)
        }

        minimumHeight = bubbleHeight + 16.dpToPx()

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

        layoutManager = GridLayoutManager(context, 5)
        itemAnimator = null
        overScrollMode = View.OVER_SCROLL_NEVER
        setWillNotDraw(false)
    }

    internal fun applyStyle(editReactionsViewStyle: EditReactionsViewStyle) {
        this.reactionsViewStyle = editReactionsViewStyle
        this.bubbleDrawer = EditReactionsBubbleDrawer(reactionsViewStyle)

        reactionsViewStyle.horizontalPadding.let {
            setPadding(it, 0, it, 0)
        }

        adapter = ReactionsAdapter(reactionsViewStyle.itemSize) {
            reactionClickListener?.onReactionClick(it)
        }.also { reactionsAdapter = it }
    }
}
