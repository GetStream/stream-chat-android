package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextBinding
import io.getstream.chat.android.ui.messages.adapter.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.utils.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.utils.extensions.context

internal class MessagePlainTextViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer,
    internal val binding: StreamUiItemMessagePlainTextBinding =
        StreamUiItemMessagePlainTextBinding.inflate(
            parent.inflater,
            parent,
            false
        ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            root.setOnClickListener {
                listeners.messageClickListener.onMessageClick(data.message)
            }
            reactionsView.setReactionClickListener {
                listeners.reactionViewClickListener.onReactionViewClick(data.message)
            }
            threadRepliesFootnote.root.setOnClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }

            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }

            LongClickFriendlyLinkMovementMethod.set(
                textView = messageText,
                longClickTarget = root,
                onLinkClicked = { url ->
                    ChatUI.instance().navigator.navigate(WebLinkDestination(url, context))
                }
            )
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.text = data.message.text
    }
}
