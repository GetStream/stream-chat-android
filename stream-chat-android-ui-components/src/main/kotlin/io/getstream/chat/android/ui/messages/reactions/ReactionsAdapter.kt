package io.getstream.chat.android.ui.messages.reactions

import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageReactionBinding
import io.getstream.chat.android.ui.utils.UiUtils
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getColorCompat

internal class ReactionsAdapter(
    @Px private val itemSize: Int,
    private val reactionClickListener: ReactionClickListener
) : ListAdapter<ReactionItem, ReactionsAdapter.ReactionViewHolder>(ReactionItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {
        return StreamUiItemMessageReactionBinding
            .inflate(parent.inflater, parent, false)
            .let { ReactionViewHolder(it, itemSize, reactionClickListener) }
    }

    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReactionViewHolder(
        private val binding: StreamUiItemMessageReactionBinding,
        @Px private val itemSize: Int,
        private val reactionClickListener: ReactionClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var reactionItem: ReactionItem

        init {
            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
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
                R.color.stream_ui_accent_blue
            } else {
                R.color.stream_ui_text_grey
            }
            binding.reactionIcon.setColorFilter(context.getColorCompat(iconTintResId))
        }
    }
}
