package io.getstream.chat.android.ui.utils.extensions

import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.common.messagelist.MessageListState
import io.getstream.chat.android.common.model.messsagelist.TypingItem

/**
 * Converts the common [MessageListState] to ui-components [MessageListItemWrapper].
 *
 * @return [MessageListItemWrapper] derived from [MessageListState].
 */
public fun MessageListState.toMessageListItemWrapper(): MessageListItemWrapper {
    var messagesList: List<MessageListItem> = messages.map { it.toUiMessageListItem() }

    if (isLoadingOlderMessages) messagesList = messagesList + listOf(MessageListItem.LoadingMoreIndicatorItem)
    if (isLoadingNewerMessages) messagesList = listOf(MessageListItem.LoadingMoreIndicatorItem) + messagesList

    return MessageListItemWrapper(
        items = messagesList,
        hasNewMessages = newMessageState != null,
        isTyping = messagesList.firstOrNull { it is MessageListItem.TypingItem } != null,
        areNewestMessagesLoaded = endOfNewMessagesReached
    )
}