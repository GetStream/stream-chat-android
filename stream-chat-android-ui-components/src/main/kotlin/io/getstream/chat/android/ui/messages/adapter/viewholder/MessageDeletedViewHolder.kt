package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageDeletedBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class MessageDeletedViewHolder(
    parent: ViewGroup,
    internal val binding: StreamUiItemMessageDeletedBinding = StreamUiItemMessageDeletedBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem) {
        val shouldBeVisible = data.message.deletedAt != null && data.isMine
        binding.root.run {
            isVisible = shouldBeVisible
            layoutParams = layoutParams.apply {
                height = if (shouldBeVisible) ViewGroup.LayoutParams.WRAP_CONTENT else 0
            }
        }
    }
}
