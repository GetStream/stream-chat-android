package io.getstream.chat.android.ui.utils.extensions

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.common.model.messsagelist.SystemMessageItem
import io.getstream.chat.android.common.model.messsagelist.MessageListItem as MessageListItemCommon
import io.getstream.chat.android.common.model.messsagelist.DateSeparatorItem
import io.getstream.chat.android.common.model.messsagelist.ThreadSeparatorItem
import io.getstream.chat.android.common.model.messsagelist.TypingItem
import io.getstream.chat.android.common.model.messsagelist.MessageItem

/**
 * Converts [MessageListItemCommon] to [MessageListItem] to be shown inside
 * [io.getstream.chat.android.ui.message.list.MessageListView].
 *
 * @return [MessageListItem] derived from [MessageListItemCommon].
 */
public fun MessageListItemCommon.toUiMessageListItem(): MessageListItem {
    return when (this) {
        is DateSeparatorItem -> MessageListItem.DateSeparatorItem(date = date)
        is SystemMessageItem -> MessageListItem.ThreadPlaceholderItem
        is ThreadSeparatorItem -> MessageListItem.ThreadSeparatorItem(date = date, messageCount = messageCount)
        is TypingItem -> MessageListItem.TypingItem(users = typingUsers)
        is MessageItem -> MessageListItem.MessageItem(
            message = message,
            positions = groupPosition.mapNotNull { it.toUiPosition() },
            isMine = isMine,
            messageReadBy = messageReadBy,
            isThreadMode = isInThread,
            isMessageRead = isMessageRead,
            showMessageFooter = showMessageFooter
        )
    }
}
