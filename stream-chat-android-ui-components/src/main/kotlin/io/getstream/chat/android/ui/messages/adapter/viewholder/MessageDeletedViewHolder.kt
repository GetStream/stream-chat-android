package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageDeletedBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

public class MessageDeletedViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    internal val binding: StreamUiItemMessageDeletedBinding = StreamUiItemMessageDeletedBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        val shouldBeVisible = data.message.deletedAt != null && data.isMine
        binding.root.run {
            isVisible = shouldBeVisible
            layoutParams = layoutParams.apply {
                height = if (shouldBeVisible) ViewGroup.LayoutParams.WRAP_CONTENT else 0
            }
        }
    }
}
