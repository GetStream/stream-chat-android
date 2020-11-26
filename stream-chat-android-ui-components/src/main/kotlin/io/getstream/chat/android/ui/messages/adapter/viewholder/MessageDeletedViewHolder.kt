package io.getstream.chat.android.ui.messages.adapter.viewholder

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamItemMessageDeletedBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class MessageDeletedViewHolder(
    private val currentUser: User,
    private val binding: StreamItemMessageDeletedBinding
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {
    override fun bind(data: MessageListItem.MessageItem) {
        val shouldBeVisible = data.message.deletedAt != null && data.message.user == currentUser
        binding.root.run {
            isVisible = shouldBeVisible
            layoutParams = if (shouldBeVisible) DEFAULT_LAYOUT_PARAMS else HIDDEN_LAYOUT_PARAMS
        }
    }

    private companion object {
        private val DEFAULT_LAYOUT_PARAMS = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
        private val HIDDEN_LAYOUT_PARAMS = RecyclerView.LayoutParams(0, 0)
    }
}