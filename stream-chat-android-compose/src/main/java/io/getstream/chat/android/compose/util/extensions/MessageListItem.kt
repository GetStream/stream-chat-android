package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.common.model.DateSeparatorItem
import io.getstream.chat.android.common.model.MessageItem
import io.getstream.chat.android.common.model.MessageListItem
import io.getstream.chat.android.common.model.MessagePosition
import io.getstream.chat.android.common.model.SystemMessageItem
import io.getstream.chat.android.common.model.ThreadSeparatorItem
import io.getstream.chat.android.common.model.TypingItem
import io.getstream.chat.android.compose.state.messages.list.DateSeparatorState
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.state.messages.list.SystemMessageState
import io.getstream.chat.android.compose.state.messages.list.ThreadSeparatorState
import io.getstream.chat.android.compose.state.messages.list.TypingItemState

public fun MessageListItem.toMessageListItemState(): MessageListItemState {
    return when(this) {
        is DateSeparatorItem -> DateSeparatorState(this.date)
        is MessageItem -> MessageItemState(
            message = this.message,
            groupPosition = messagePosition.toMessageItemGroupPosition(),
            parentMessageId = this.parentMessageId,
            isMine = isMine,
            isInThread = isInThread,
            currentUser = currentUser,
            isMessageRead = isMessageRead,
            shouldShowFooter = showMessageFooter,
            deletedMessageVisibility = deletedMessageVisibility
        )
        is SystemMessageItem -> SystemMessageState(this.message)
        is ThreadSeparatorItem -> ThreadSeparatorState(this.messageCount)
        is TypingItem -> TypingItemState(this.typingUsers) // TODO
    }
}

public fun MessagePosition.toMessageItemGroupPosition(): MessageItemGroupPosition {
    return when(this) {
        MessagePosition.TOP -> MessageItemGroupPosition.Top
        MessagePosition.MIDDLE -> MessageItemGroupPosition.Middle
        MessagePosition.BOTTOM -> MessageItemGroupPosition.Bottom
        MessagePosition.NONE -> MessageItemGroupPosition.None
    }
}