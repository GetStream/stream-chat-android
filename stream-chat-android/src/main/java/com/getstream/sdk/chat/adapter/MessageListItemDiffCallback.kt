package com.getstream.sdk.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.client.models.User

internal object MessageListItemDiffCallback : DiffUtil.ItemCallback<MessageListItem>() {
    override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return oldItem.getStableId() == newItem.getStableId()
    }

    override fun areContentsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean =
        when (oldItem) {
            is MessageListItem.MessageItem -> {
                newItem as MessageListItem.MessageItem
                if (oldItem.message.text != newItem.message.text) {
                    false
                } else if (oldItem.message.reactionScores != newItem.message.reactionScores) {
                    false
                } else if (oldItem.message.reactionCounts != newItem.message.reactionCounts) {
                    false
                } else if (oldItem.message.attachments != newItem.message.attachments) {
                    false
                } else if (oldItem.message.replyCount != newItem.message.replyCount) {
                    false
                } else if (oldItem.message.syncStatus != newItem.message.syncStatus) {
                    false
                } else if (oldItem.message.deletedAt != newItem.message.deletedAt) {
                    false
                } else if (oldItem.positions != newItem.positions) {
                    false
                } else oldItem.messageReadBy.map { it.getUserId() } == newItem.messageReadBy.map { it.getUserId() }
            }
            is MessageListItem.DateSeparatorItem -> oldItem.date == (newItem as? MessageListItem.DateSeparatorItem)?.date
            is MessageListItem.ThreadSeparatorItem -> oldItem.date == (newItem as? MessageListItem.ThreadSeparatorItem)?.date
            is MessageListItem.LoadingMoreIndicatorItem -> true
            is MessageListItem.TypingItem -> oldItem.users.map(User::id) == ((newItem) as? MessageListItem.TypingItem)?.users?.map(User::id)
        }

    override fun getChangePayload(oldItem: MessageListItem, newItem: MessageListItem): Any? {
        return if (oldItem is MessageListItem.MessageItem) {
            newItem as MessageListItem.MessageItem
            MessageListItemPayloadDiff(
                text = oldItem.message.text != newItem.message.text,
                reactions = (oldItem.message.reactionCounts != newItem.message.reactionCounts) || (oldItem.message.reactionScores != newItem.message.reactionScores),
                attachments = oldItem.message.attachments != newItem.message.attachments,
                replies = oldItem.message.replyCount != newItem.message.replyCount,
                syncStatus = oldItem.message.syncStatus != newItem.message.syncStatus,
                deleted = oldItem.message.deletedAt != newItem.message.deletedAt,
                positions = oldItem.positions != newItem.positions,
                readBy = oldItem.messageReadBy.map { it.getUserId() } == newItem.messageReadBy.map { it.getUserId() }
            )
        } else {
            null
        }
    }
}
