/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.messages.list

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.EmptyThreadPlaceholderItemState
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListItemState
import io.getstream.chat.android.ui.common.state.messages.list.SystemMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.ThreadDateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.TypingItemState

/**
 * Represents the message item container that allows us to customize each type of item in the MessageList.
 *
 * @param messageListItemState The state of the message list item.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onReactionsClick Handler when the user taps on message reactions.
 * @param onThreadClick Handler when the user taps on a thread within a message item.
 * @param onGiphyActionClick Handler when the user taps on Giphy message actions.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onMediaGalleryPreviewResult Handler when the user receives a result from the Media Gallery Preview.
 * @param dateSeparatorContent Composable that represents date separators.
 * @param threadSeparatorContent Composable that represents thread separators.
 * @param systemMessageContent Composable that represents system messages.
 * @param messageItemContent Composable that represents regular messages.
 * @param typingIndicatorContent Composable that represents a typing indicator.
 * @param emptyThreadPlaceholderItemContent Composable that represents placeholders inside of an empty thread.
 * This content is disabled by default and can be enabled via [MessagesViewModelFactory.showDateSeparatorInEmptyThread]
 * or [MessageListController.showDateSeparatorInEmptyThread].
 */
@Composable
public fun MessageContainer(
    messageListItemState: MessageListItemState,
    onLongItemClick: (Message) -> Unit = {},
    onReactionsClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    dateSeparatorContent: @Composable (DateSeparatorItemState) -> Unit = {
        DefaultMessageDateSeparatorContent(dateSeparator = it)
    },
    threadSeparatorContent: @Composable (ThreadDateSeparatorItemState) -> Unit = {
        DefaultMessageThreadSeparatorContent(threadSeparator = it)
    },
    systemMessageContent: @Composable (SystemMessageItemState) -> Unit = {
        DefaultSystemMessageContent(systemMessageState = it)
    },
    messageItemContent: @Composable (MessageItemState) -> Unit = {
        DefaultMessageItem(
            messageItem = it,
            onLongItemClick = onLongItemClick,
            onReactionsClick = onReactionsClick,
            onThreadClick = onThreadClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
    typingIndicatorContent: @Composable (TypingItemState) -> Unit = { },
    emptyThreadPlaceholderItemContent: @Composable (EmptyThreadPlaceholderItemState) -> Unit = { },
) {
    when (messageListItemState) {
        is DateSeparatorItemState -> dateSeparatorContent(messageListItemState)
        is ThreadDateSeparatorItemState -> threadSeparatorContent(messageListItemState)
        is SystemMessageItemState -> systemMessageContent(messageListItemState)
        is MessageItemState -> messageItemContent(messageListItemState)
        is TypingItemState -> typingIndicatorContent(messageListItemState)
        is EmptyThreadPlaceholderItemState -> emptyThreadPlaceholderItemContent(messageListItemState)
    }
}

/**
 * Represents a date separator item that shows whenever messages are too far apart in time.
 *
 * @param dateSeparator The data used to show the separator text.
 */
@Composable
internal fun DefaultMessageDateSeparatorContent(dateSeparator: DateSeparatorItemState) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .padding(vertical = 8.dp),
            color = ChatTheme.colors.overlayDark,
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp),
                text = DateUtils.getRelativeTimeSpanString(
                    dateSeparator.date.time,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE,
                ).toString(),
                color = ChatTheme.colors.barsBackground,
                style = ChatTheme.typography.body,
            )
        }
    }
}

/**
 * Represents a thread separator item that is displayed in thread mode to separate a parent message
 * from thread replies.
 *
 * @param threadSeparator The data used to show the separator text.
 */
@Composable
internal fun DefaultMessageThreadSeparatorContent(threadSeparator: ThreadDateSeparatorItemState) {
    val backgroundGradient = Brush.verticalGradient(
        listOf(
            ChatTheme.colors.threadSeparatorGradientStart,
            ChatTheme.colors.threadSeparatorGradientEnd,
        ),
    )
    val replyCount = threadSeparator.replyCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ChatTheme.dimens.threadSeparatorVerticalPadding)
            .background(brush = backgroundGradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(vertical = ChatTheme.dimens.threadSeparatorTextVerticalPadding),
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_thread_separator,
                replyCount,
                replyCount,
            ),
            color = ChatTheme.colors.textLowEmphasis,
            style = ChatTheme.typography.body,
        )
    }
}

/**
 * The default System message content.
 *
 * A system message is a message generated by a system event, such as updating the channel or muting a user.
 *
 * @param systemMessageState The system message item to show.
 */
@Composable
internal fun DefaultSystemMessageContent(systemMessageState: SystemMessageItemState) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        text = systemMessageState.message.text,
        color = ChatTheme.colors.textLowEmphasis,
        style = ChatTheme.typography.footnoteBold,
        textAlign = TextAlign.Center,
    )
}

/**
 * The default message item content.
 *
 * @param messageItem The message item to show.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onReactionsClick Handler when the user taps on message reactions.
 * @param onThreadClick Handler when the user clicks on the message thread.
 * @param onGiphyActionClick Handler when the user selects a Giphy action.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onMediaGalleryPreviewResult Handler when the user receives a result from the Media Gallery Preview.
 */
@Composable
internal fun DefaultMessageItem(
    messageItem: MessageItemState,
    onLongItemClick: (Message) -> Unit,
    onReactionsClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit,
    onGiphyActionClick: (GiphyAction) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    MessageItem(
        messageItem = messageItem,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onThreadClick = onThreadClick,
        onGiphyActionClick = onGiphyActionClick,
        onQuotedMessageClick = onQuotedMessageClick,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
    )
}
