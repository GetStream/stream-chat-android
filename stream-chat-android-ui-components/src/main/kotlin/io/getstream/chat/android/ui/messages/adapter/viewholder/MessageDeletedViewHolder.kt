package io.getstream.chat.android.ui.messages.adapter.viewholder

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamItemMessageDeletedBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class MessageDeletedViewHolder(
    private val currentUser: User,
    private val binding: StreamItemMessageDeletedBinding
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {
    override fun bind(data: MessageListItem.MessageItem) {
        binding.root.isVisible = when {
            data.message.deletedAt != null && data.message.user == currentUser -> true
            else -> false
        }
    }
}