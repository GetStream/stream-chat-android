package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiItemThreadDividerBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

internal class ThreadSeparatorViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemThreadDividerBinding =
        StreamUiItemThreadDividerBinding.inflate(
            parent.streamThemeInflater,
            parent,
            false
        ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.ThreadSeparatorItem>(binding.root, decorators) {

    override fun bindData(
        data: MessageListItem.ThreadSeparatorItem,
        diff: MessageListItemPayloadDiff?,
    ) {
        super.bindData(data, diff)

        binding.threadSeparatorLabel.setTextStyle(style.threadSeparatorTextStyle)
        binding.threadSeparatorLabel.text = context.resources.getQuantityString(
            R.plurals.stream_ui_message_list_thread_separator,
            data.messageCount,
            data.messageCount,
        )
    }
}
