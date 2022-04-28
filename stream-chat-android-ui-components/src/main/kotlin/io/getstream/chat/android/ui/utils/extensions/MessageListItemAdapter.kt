package io.getstream.chat.android.ui.utils.extensions

import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemAdapter

@InternalStreamChatApi
internal fun MessageListItemAdapter.isGroupedWithNextMessage(messageItem: MessageListItem.MessageItem): Boolean {
    if (messageItem.isBottomPosition()) return false

    val messageIndex = currentList.indexOf(
        currentList.find {
            (it as? MessageListItem.MessageItem)?.message?.id == messageItem.message.id
        }
    )
    if (messageIndex == -1) return false

    val nextMessage = currentList.takeLast(currentList.size - messageIndex - 1)
        .find { it is MessageListItem.MessageItem } as? MessageListItem.MessageItem ?: return false

    return (nextMessage.message.createdAt?.time ?: 0) - (messageItem.message.createdAt?.time ?: 0) < 1000 * 60
}