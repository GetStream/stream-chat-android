package io.getstream.chat.android.ui.message.list.adapter.internal

import androidx.recyclerview.widget.DiffUtil
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff

internal object MessageListItemDiffCallback : DiffUtil.ItemCallback<MessageListItem>() {
    override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return oldItem.getStableId() == newItem.getStableId()
    }

    override fun areContentsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean =
        when (oldItem) {
            is MessageListItem.MessageItem -> {
                newItem as MessageListItem.MessageItem
                oldItem.message.text == newItem.message.text &&
                    oldItem.message.reactionScores == newItem.message.reactionScores &&
                    oldItem.message.reactionCounts == newItem.message.reactionCounts &&
                    oldItem.message.attachments == newItem.message.attachments &&
                    oldItem.message.replyCount == newItem.message.replyCount &&
                    oldItem.message.syncStatus == newItem.message.syncStatus &&
                    oldItem.message.deletedAt == newItem.message.deletedAt &&
                    oldItem.positions == newItem.positions &&
                    oldItem.isMessageRead == newItem.isMessageRead &&
                    oldItem.isThreadMode == newItem.isThreadMode
            }
            is MessageListItem.DateSeparatorItem -> oldItem.date == (newItem as? MessageListItem.DateSeparatorItem)?.date
            is MessageListItem.ThreadSeparatorItem -> oldItem == (newItem as? MessageListItem.ThreadSeparatorItem)
            is MessageListItem.LoadingMoreIndicatorItem -> true
            is MessageListItem.TypingItem -> oldItem.users.map(User::id) == ((newItem) as? MessageListItem.TypingItem)?.users?.map(
                User::id
            )
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
            )
        } else {
            null
        }
    }
}
