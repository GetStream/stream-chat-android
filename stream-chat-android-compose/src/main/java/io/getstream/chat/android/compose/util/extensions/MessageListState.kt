package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.common.messagelist.MessageListState
import io.getstream.chat.android.compose.state.messages.MessagesState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Converts common [MessageListState] to compose [MessagesState].
 *
 * @return Compose [MessagesState] derived from common [MessageListState].
 */
internal fun MessageListState.toComposeState(): MessagesState {
    return MessagesState(
        isLoading = isLoading,
        isLoadingMore = isLoadingOlderMessages || isLoadingNewerMessages,
        endOfMessages = endOfOldMessagesReached,
        currentUser = currentUser,
        parentMessageId = parentMessageId,
        unreadCount = unreadCount,
        startOfMessages = endOfNewMessagesReached,
        isLoadingMoreNewMessages = isLoadingNewerMessages,
        isLoadingMoreOldMessages = isLoadingOlderMessages,
        messageItems = messages.reversed().map { it.toMessageListItemState() },
        newMessageState = newMessageState?.toComposeState(),
        selectedMessageState = selectedMessageState?.toComposeState()
    )
}