package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageGiphyBinding
import io.getstream.chat.android.ui.messages.adapter.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

internal class GiphyViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer,
    internal val binding: StreamUiItemMessageGiphyBinding = StreamUiItemMessageGiphyBinding.inflate(
        parent.inflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            cancelButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(data.message, GiphyAction.CANCEL)
            }
            sendButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(data.message, GiphyAction.SEND)
            }
            nextButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(data.message, GiphyAction.SHUFFLE)
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        data.message.attachments.firstOrNull()?.let(binding.mediaAttachmentView::showAttachment)
        binding.giphyTextLabel.text = trimText(data.message.text)
    }

    private fun trimText(text: String): String {
        return "\"${text.replace(GIPHY_PREFIX, "")}\""
    }

    private companion object {
        private const val GIPHY_PREFIX = "/giphy "
    }
}
