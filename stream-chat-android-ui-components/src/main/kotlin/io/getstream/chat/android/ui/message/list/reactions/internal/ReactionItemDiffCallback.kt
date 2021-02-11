package io.getstream.chat.android.ui.message.list.reactions.internal

import androidx.recyclerview.widget.DiffUtil

internal class ReactionItemDiffCallback : DiffUtil.ItemCallback<ReactionItem>() {
    override fun areItemsTheSame(oldItem: ReactionItem, newItem: ReactionItem): Boolean {
        return oldItem.type == newItem.type
    }

    override fun areContentsTheSame(oldItem: ReactionItem, newItem: ReactionItem): Boolean {
        return oldItem == newItem
    }
}
