package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction

public abstract class ReactionsView : RecyclerView {

    internal lateinit var reactionsViewStyle: ReactionsViewStyle

    private lateinit var reactionsAdapter: ReactionsAdapter
    private var isMyMessage: Boolean = false
    private var reactionClickListener: ReactionClickListener? = null

    public constructor(context: Context) : super(context) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    internal fun setStyle(reactionsViewStyle: ReactionsViewStyle) {
        this.reactionsViewStyle = reactionsViewStyle

        minimumHeight = reactionsViewStyle.totalHeight
        val horizontalPadding = reactionsViewStyle.horizontalPadding
        setPadding(horizontalPadding, 0, horizontalPadding, 0)

        adapter = ReactionsAdapter(reactionsViewStyle) {
            reactionClickListener?.onReactionClick(it)
        }.also { reactionsAdapter = it }
    }

    public open fun setMessage(message: Message, isMyMessage: Boolean = false) {
        this.isMyMessage = isMyMessage

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, isMyMessage)
        reactionsAdapter.submitList(createReactionItems(message, isMyMessage))
    }

    public fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawReactionsBubble(canvas, isMyMessage)
    }

    protected abstract fun drawReactionsBubble(canvas: Canvas, isMyMessage: Boolean)

    internal abstract fun createReactionItems(
        message: Message,
        isMyMessage: Boolean
    ): List<ReactionsAdapter.ReactionItem>

    private fun init() {
        setWillNotDraw(false)
        overScrollMode = View.OVER_SCROLL_NEVER
        itemAnimator = null
    }

    public fun interface ReactionClickListener {
        public fun onReactionClick(reaction: Reaction)
    }
}
