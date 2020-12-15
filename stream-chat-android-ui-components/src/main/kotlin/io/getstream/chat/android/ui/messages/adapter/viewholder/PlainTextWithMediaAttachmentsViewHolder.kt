package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.ListenerContainer
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.databinding.StreamUiPlainTextWithMediaAttachmentsViewBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class PlainTextWithMediaAttachmentsViewHolder(
    parent: ViewGroup,
    private val listenerContainer: ListenerContainer?,
    private val binding: StreamUiPlainTextWithMediaAttachmentsViewBinding = StreamUiPlainTextWithMediaAttachmentsViewBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {
    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) { }
}
