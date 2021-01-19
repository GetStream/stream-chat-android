package io.getstream.chat.ui.sample.feature.chat

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.view.MessageListView
import io.getstream.chat.android.ui.utils.extensions.isDeleted
import io.getstream.chat.android.ui.utils.extensions.isGiphyEphemeral

object HiddenMessageListItemFilter : MessageListView.MessageListItemFilter {

    private val theirDeletedMessagePredicate: (MessageListItem) -> Boolean = { item ->
        item is MessageListItem.MessageItem && item.message.isDeleted() && item.isTheirs
    }

    private val theirGiphyEphemeralMessagePredicate: (MessageListItem) -> Boolean = { item ->
        item is MessageListItem.MessageItem && item.message.isGiphyEphemeral() && item.isTheirs
    }

    override fun filterMessageListItem(messageListItem: List<MessageListItem>): List<MessageListItem> {
        return messageListItem.asSequence()
            .filterNot(theirDeletedMessagePredicate)
            .filterNot(theirGiphyEphemeralMessagePredicate)
            .toList()
    }
}
