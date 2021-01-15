package io.getstream.chat.android.ui.messages.reactions.user

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageReactionBinding
import io.getstream.chat.android.ui.messages.reactions.view.ViewReactionsBubbleDrawer
import io.getstream.chat.android.ui.messages.reactions.view.ViewReactionsViewStyle
import io.getstream.chat.android.ui.utils.UiUtils
import io.getstream.chat.android.ui.utils.extensions.getColorCompat

public class SingleReactionView : FrameLayout {

    private val binding = StreamUiItemMessageReactionBinding.inflate(context.inflater, this, true)

    private lateinit var reactionsViewStyle: ViewReactionsViewStyle
    private lateinit var bubbleDrawer: ViewReactionsBubbleDrawer

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

    public fun setReaction(reaction: Reaction, isMyReaction: Boolean = false) {
        // according to the design, current user reactions have the same style
        // as reactions on the current user messages in the message list
        this.isMyMessage = !isMyReaction
        setReactionIcon(reaction)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bubbleDrawer.drawReactionsBubble(canvas, width, isMyMessage, isSingleReaction = true, inverseBubbleStyle = true)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.reactionsViewStyle = ViewReactionsViewStyle(context, attrs)
        this.bubbleDrawer = ViewReactionsBubbleDrawer(reactionsViewStyle)

        setWillNotDraw(false)
        minimumHeight = reactionsViewStyle.totalHeight
        binding.reactionIcon.setColorFilter(context.getColorCompat(R.color.stream_ui_accent_blue))
    }

    private fun setReactionIcon(reaction: Reaction) {
        val reactionType = reaction.type
        val reactionIcon = UiUtils.getReactionTypes()[reactionType]
        if (reactionIcon != null) {
            binding.reactionIcon.setImageResource(reactionIcon)
        } else {
            // better to have a proper fallback icon
            binding.reactionIcon.setImageDrawable(null)
        }
    }
}
