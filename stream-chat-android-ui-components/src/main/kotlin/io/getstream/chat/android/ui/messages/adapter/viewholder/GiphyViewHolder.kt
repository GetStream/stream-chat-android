package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageGiphyBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.ListenerContainer
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff

public class GiphyViewHolder(
    parent: ViewGroup,
    internal val binding: StreamUiItemMessageGiphyBinding = StreamUiItemMessageGiphyBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    ),
    private val listenerContainer: ListenerContainer? = null
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {
    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        data.message.attachments.firstOrNull()?.let(binding.mediaAttachmentView::showAttachment)

        binding.giphyTextLabel.text = data.message.text

        listenerContainer?.also { listeners ->
            binding.cardView.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
            binding.cancelButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(
                    data.message,
                    GiphyAction.CANCEL
                )
            }
            binding.sendButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(
                    data.message,
                    GiphyAction.SEND
                )
            }
            binding.nextButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(
                    data.message,
                    GiphyAction.SHUFFLE
                )
            }
        }
    }
}
