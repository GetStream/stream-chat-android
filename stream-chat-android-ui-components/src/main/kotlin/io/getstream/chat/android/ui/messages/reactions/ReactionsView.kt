package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.use
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getEnum

public abstract class ReactionsView : RecyclerView {

    internal lateinit var reactionsViewStyle: ReactionsViewStyle

    private lateinit var reactionsAdapter: ReactionsAdapter
    private lateinit var orientation: Orientation
    private var isMyMessage: Boolean = false
    private var reactionClickListener: ReactionClickListener? = null

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
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

    public fun setOrientation(orientation: Orientation) {
        this.orientation = orientation
        invalidate()
    }

    public fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawReactionsBubble(canvas, isMyMessage, isMirrored())
    }

    protected abstract fun drawReactionsBubble(canvas: Canvas, isMyMessage: Boolean, isMirrored: Boolean)

    internal abstract fun createReactionItems(
        message: Message,
        isMyMessage: Boolean
    ): List<ReactionsAdapter.ReactionItem>

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)

        setWillNotDraw(false)
        overScrollMode = View.OVER_SCROLL_NEVER
        itemAnimator = null
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.StreamReactionsViewStyle, 0, 0).use {
            orientation =
                it.getEnum(R.styleable.StreamReactionsViewStyle_streamReactionsViewOrientation, Orientation.UNDEFINED)
        }
    }

    private fun isMirrored(): Boolean {
        return when (orientation) {
            Orientation.LEFT -> false
            Orientation.RIGHT -> true
            Orientation.UNDEFINED -> isMyMessage
        }
    }

    public fun interface ReactionClickListener {
        public fun onReactionClick(reaction: Reaction)
    }

    public enum class Orientation {
        LEFT,
        RIGHT,
        UNDEFINED
    }
}
