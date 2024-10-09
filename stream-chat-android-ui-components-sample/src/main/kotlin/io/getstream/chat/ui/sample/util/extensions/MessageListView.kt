package io.getstream.chat.ui.sample.util.extensions

import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

/**
 * Notifies the [MessageListView] that a message has changed.
 */
@Suppress("UNCHECKED_CAST")
internal fun MessageListView.notifyMessageChanged(message: Message) {
    val adapter = getRecyclerView().adapter as? ListAdapter<MessageListItem, *> ?: return
    adapter.currentList.indexOfFirst { it is MessageListItem.MessageItem && it.message.id == message.id }
        .takeIf { it != -1 }
        ?.let { position ->
            adapter.notifyItemChanged(position)
        }
}