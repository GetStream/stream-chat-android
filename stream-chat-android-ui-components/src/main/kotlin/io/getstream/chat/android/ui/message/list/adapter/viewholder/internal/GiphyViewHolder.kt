package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageGiphyBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

internal class GiphyViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer,
    internal val binding: StreamUiItemMessageGiphyBinding = StreamUiItemMessageGiphyBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            cancelButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(data.message, GiphyAction.CANCEL)
            }
            shuffleButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(data.message, GiphyAction.SHUFFLE)
            }
            sendButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(data.message, GiphyAction.SEND)
            }
            mediaAttachmentView.giphyBadgeEnabled = false
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)
        data.message
            .attachments
            .firstOrNull()
            ?.let(binding.mediaAttachmentView::showAttachment)

        binding.giphyQueryTextView.text = data.message
            .text
            .replace(GIPHY_PREFIX, "")
    }

    private companion object {
        private const val GIPHY_PREFIX = "/giphy "
    }
}
