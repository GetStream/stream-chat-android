package io.getstream.chat.android.ui.messages.reactions

import androidx.recyclerview.widget.DiffUtil

public class ReactionItemDiffCallback : DiffUtil.ItemCallback<ReactionItem>() {
    override fun areItemsTheSame(oldItem: ReactionItem, newItem: ReactionItem): Boolean {
        return oldItem.reaction.messageId == newItem.reaction.messageId &&
            oldItem.reaction.fetchUserId() == newItem.reaction.fetchUserId() &&
            oldItem.reaction.type == newItem.reaction.type &&
            oldItem.isMine == newItem.isMine
    }

    override fun areContentsTheSame(oldItem: ReactionItem, newItem: ReactionItem): Boolean {
        return oldItem == newItem
    }
}
