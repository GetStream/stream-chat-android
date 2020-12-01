package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamItemMessageDeletedBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GapDecorator

public class MessageDeletedViewHolder(
    parent: ViewGroup,
    internal val binding: StreamItemMessageDeletedBinding = StreamItemMessageDeletedBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    private val decorators = listOf<Decorator>(BackgroundDecorator(), GapDecorator())

    override fun bind(data: MessageListItem.MessageItem) {
        decorators.forEach { it.decorate(this, data) }
        val shouldBeVisible = data.message.deletedAt != null && data.isMine
        binding.root.run {
            isVisible = shouldBeVisible
            layoutParams = layoutParams.apply {
                height = if (shouldBeVisible) ViewGroup.LayoutParams.WRAP_CONTENT else 0
            }
        }
    }
}
