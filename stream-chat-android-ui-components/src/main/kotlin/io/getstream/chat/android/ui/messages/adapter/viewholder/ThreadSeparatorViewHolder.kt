package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemThreadDividerBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.utils.extensions.context

public class ThreadSeparatorViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    internal val binding: StreamUiItemThreadDividerBinding =
        StreamUiItemThreadDividerBinding.inflate(
            parent.inflater,
            parent,
            false
        )
) : BaseMessageItemViewHolder<MessageListItem.ThreadSeparatorItem>(binding.root, decorators) {

    override fun bindData(data: MessageListItem.ThreadSeparatorItem, diff: MessageListItemPayloadDiff?) {
        binding.threadSeparatorLabel.text = context.resources.getQuantityString(
            R.plurals.stream_ui_thread_separator_replies_label,
            data.messageCount,
            data.messageCount,
        )
    }
}
