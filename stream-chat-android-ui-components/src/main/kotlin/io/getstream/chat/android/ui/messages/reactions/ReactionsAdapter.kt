package io.getstream.chat.android.ui.messages.reactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamItemMessageReactionBinding
import io.getstream.chat.android.ui.utils.UiUtils
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getColorCompat

internal class ReactionsAdapter(
    private val reactionsViewStyle: ReactionsViewStyle,
    private val reactionClickListener: ReactionClickListener
) : ListAdapter<ReactionItem, ReactionsAdapter.ReactionViewHolder>(ReactionItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {
        return StreamItemMessageReactionBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { ReactionViewHolder(it, reactionsViewStyle, reactionClickListener) }
    }

    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReactionViewHolder(
        private val binding: StreamItemMessageReactionBinding,
        reactionsViewStyle: ReactionsViewStyle,
        private val reactionClickListener: ReactionClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var reactionItem: ReactionItem

        init {
            val itemSize: Int = reactionsViewStyle.itemSize
            val itemMargin: Int = reactionsViewStyle.itemMargin
            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = itemMargin
                rightMargin = itemMargin
                topMargin = itemMargin
                bottomMargin = itemMargin
                width = itemSize
                height = itemSize
            }
            binding.root.setOnClickListener {
                reactionClickListener.onReactionClick(reactionItem.reaction)
            }
        }

        fun bind(reactionItem: ReactionItem) {
            this.reactionItem = reactionItem
            bindReactionIcon(reactionItem)
            bindReactionAffiliation(reactionItem)
        }

        private fun bindReactionIcon(reactionItem: ReactionItem) {
            val reactionType = reactionItem.reaction.type
            val reactionIcon = UiUtils.getReactionTypes()[reactionType]
            if (reactionIcon != null) {
                binding.reactionIcon.setImageResource(reactionIcon)
            } else {
                // better to have a proper fallback icon
                binding.reactionIcon.setImageDrawable(null)
            }
        }

        private fun bindReactionAffiliation(reactionItem: ReactionItem) {
            val iconTintResId = if (reactionItem.isMine) {
                R.color.stream_blue
            } else {
                R.color.stream_text_grey
            }
            binding.reactionIcon.setColorFilter(context.getColorCompat(iconTintResId))
        }
    }
}
