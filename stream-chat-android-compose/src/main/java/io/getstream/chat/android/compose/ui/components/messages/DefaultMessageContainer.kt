package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.messages.list.DateSeparatorState
import io.getstream.chat.android.compose.state.messages.list.GiphyAction
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.state.messages.list.SystemMessageState
import io.getstream.chat.android.compose.state.messages.list.ThreadSeparatorState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the default message item container that allows us to customize each type of item in the MessageList.
 *
 * @param messageListItem The state of the message list item.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onThreadClick Handler when the user taps on a thread within a message item.
 * @param onGiphyActionClick Handler when the user taps on Giphy message actions.
 * @param onImagePreviewResult Handler when the user receives a result from the Image Preview.
 * @param dateSeparatorContent Composable that represents date separators.
 * @param threadSeparatorContent Composable that represents thread separators.
 * @param systemMessageContent Composable that represents system messages.
 * @param messageItemContent Composable that represents regular messages.
 */
@Composable
public fun DefaultMessageContainer(
    messageListItem: MessageListItemState,
    onLongItemClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    dateSeparatorContent: @Composable (DateSeparatorState) -> Unit = {
        MessageDateSeparator(
            modifier = Modifier.fillMaxWidth(),
            dateSeparator = it
        )
    },
    threadSeparatorContent: @Composable (ThreadSeparatorState) -> Unit = {
        MessageThreadSeparator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = ChatTheme.dimens.threadSeparatorVerticalPadding),
            threadSeparator = it
        )
    },
    systemMessageContent: @Composable (SystemMessageState) -> Unit = {
        SystemMessage(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            systemMessageState = it
        )
    },
    messageItemContent: @Composable (MessageItemState) -> Unit = {
        DefaultMessageItem(
            messageItem = it,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onGiphyActionClick = onGiphyActionClick,
            onImagePreviewResult = onImagePreviewResult
        )
    },
) {
    when (messageListItem) {
        is DateSeparatorState -> dateSeparatorContent(messageListItem)
        is ThreadSeparatorState -> threadSeparatorContent(messageListItem)
        is SystemMessageState -> systemMessageContent(messageListItem)
        is MessageItemState -> messageItemContent(messageListItem)
    }
}
