package io.getstream.chat.android.ui.message.list.reactions.internal

import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageReactionBinding
import io.getstream.chat.android.ui.message.list.reactions.ReactionClickListener

internal class ReactionsAdapter(
    @Px private val itemSize: Int,
    private val reactionClickListener: ReactionClickListener,
) : ListAdapter<ReactionItem, ReactionsAdapter.ReactionViewHolder>(ReactionItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {
        return StreamUiItemMessageReactionBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { ReactionViewHolder(it, itemSize, reactionClickListener) }
    }

    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReactionViewHolder(
        private val binding: StreamUiItemMessageReactionBinding,
        @Px private val itemSize: Int,
        private val reactionClickListener: ReactionClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var reactionItem: ReactionItem

        init {
            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                width = itemSize
                height = itemSize
            }
            binding.root.setOnClickListener {
                reactionClickListener.onReactionClick(reactionItem.type)
            }
        }

        fun bind(reactionItem: ReactionItem) {
            this.reactionItem = reactionItem
            binding.reactionIcon.setImageDrawable(reactionItem.drawable)
        }
    }
}
