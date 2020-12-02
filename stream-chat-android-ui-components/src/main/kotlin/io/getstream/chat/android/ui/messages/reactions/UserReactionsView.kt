package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUserReactionsViewBinding
import io.getstream.chat.android.ui.utils.extensions.inflater

public class UserReactionsView : FrameLayout {

    private val binding: StreamUserReactionsViewBinding =
        StreamUserReactionsViewBinding.inflate(context.inflater, this, true)

    private var reactionClickListener: ReactionsView.ReactionClickListener? = null
    private val userReactionsAdapter: UserReactionAdapter = UserReactionAdapter {
        reactionClickListener?.onReactionClick(it)
    }

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

    public fun setMessage(message: Message) {
        bindTitle(message)
        bindReactionList(message)
    }

    public fun setReactionClickListener(reactionClickListener: ReactionsView.ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    private fun init() {
        binding.recyclerView.adapter = userReactionsAdapter
    }

    private fun bindTitle(message: Message) {
        val reactionCount = message.latestReactions.size
        binding.messageMembersTextView.text = context.resources.getQuantityString(
            R.plurals.stream_user_reactions_title,
            reactionCount,
            reactionCount
        )
    }

    private fun bindReactionList(message: Message) {
        val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager
        layoutManager.spanCount = message.latestReactions
            .size
            .coerceAtMost(MAX_COLUMNS_COUNT)

        val reactionItems = message.latestReactions
            .map { ReactionItem(it, message.ownReactions.contains(it)) }
            .toMutableList()
        userReactionsAdapter.submitList(reactionItems)
    }

    private companion object {
        private const val MAX_COLUMNS_COUNT = 4
    }
}
