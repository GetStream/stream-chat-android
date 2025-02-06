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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.components.messages.factory.MessageContentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.EmptyThreadPlaceholderItemState
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListItemState
import io.getstream.chat.android.ui.common.state.messages.list.ModeratedMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.StartOfTheChannelItemState
import io.getstream.chat.android.ui.common.state.messages.list.SystemMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.ThreadDateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.TypingItemState
import io.getstream.chat.android.ui.common.state.messages.list.UnreadSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType

/**
 * Represents the message item container that allows us to customize each type of item in the MessageList.
 *
 * @param messageListItemState The state of the message list item.
 * @param reactionSorting The sorting of reactions for the message.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onReactionsClick Handler when the user taps on message reactions.
 * @param onThreadClick Handler when the user taps on a thread within a message item.
 * @param onGiphyActionClick Handler when the user taps on Giphy message actions.
 * @param onCastVote Handler for casting a vote on an option.
 * @param onClosePoll Handler for closing a poll.
 * @param onPollUpdated Handler for updating a poll.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onUserAvatarClick Handler when users avatar is clicked.
 * @param onLinkClick Handler for clicking on a link in the message.
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
public fun LazyItemScope.MessageContainer(
    messageListItemState: MessageListItemState,
    reactionSorting: ReactionSorting,
    messageContentFactory: MessageContentFactory = ChatTheme.messageContentFactory,
    onLongItemClick: (Message) -> Unit = {},
    onReactionsClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onPollUpdated: (Message, Poll) -> Unit = { _, _ -> },
    onCastVote: (Message, Poll, Option) -> Unit = { _, _, _ -> },
    onRemoveVote: (Message, Poll, Vote) -> Unit = { _, _, _ -> },
    selectPoll: (Message, Poll, PollSelectionType) -> Unit = { _, _, _ -> },
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit = { _, _, _ -> },
    onClosePoll: (String) -> Unit = {},
    onAddPollOption: (poll: Poll, option: String) -> Unit = { _, _ -> },
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onUserAvatarClick: ((User) -> Unit)? = null,
    onLinkClick: ((Message, String) -> Unit)? = null,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onUserMentionClick: (User) -> Unit = {},
    dateSeparatorContent: @Composable LazyItemScope.(DateSeparatorItemState) -> Unit = { dateSeparatorItem ->
        with(ChatTheme.componentFactory) {
            MessageListDateSeparatorItemContent(dateSeparatorItem = dateSeparatorItem)
        }
    },
    unreadSeparatorContent: @Composable LazyItemScope.(UnreadSeparatorItemState) -> Unit = { unreadSeparatorItem ->
        with(ChatTheme.componentFactory) {
            MessageListUnreadSeparatorItemContent(unreadSeparatorItem = unreadSeparatorItem)
        }
    },
    threadSeparatorContent: @Composable LazyItemScope.(
        ThreadDateSeparatorItemState,
    ) -> Unit = { threadDateSeparatorItem ->
        with(ChatTheme.componentFactory) {
            MessageListThreadDateSeparatorItemContent(threadDateSeparatorItem = threadDateSeparatorItem)
        }
    },
    systemMessageContent: @Composable LazyItemScope.(SystemMessageItemState) -> Unit = { systemMessageItem ->
        with(ChatTheme.componentFactory) {
            MessageListSystemItemContent(systemMessageItem = systemMessageItem)
        }
    },
    moderatedMessageContent: @Composable LazyItemScope.(ModeratedMessageItemState) -> Unit = { moderatedMessageItem ->
        with(ChatTheme.componentFactory) {
            MessageListModeratedItemContent(moderatedMessageItem = moderatedMessageItem)
        }
    },
    messageItemContent: @Composable LazyItemScope.(MessageItemState) -> Unit = { messageItem ->
        if (messageContentFactory == MessageContentFactory.Deprecated) {
            with(ChatTheme.componentFactory) {
                MessageListItemContent(
                    messageItem = messageItem,
                    reactionSorting = reactionSorting,
                    onLongItemClick = onLongItemClick,
                    onReactionsClick = onReactionsClick,
                    onThreadClick = onThreadClick,
                    onPollUpdated = onPollUpdated,
                    onCastVote = onCastVote,
                    onRemoveVote = onRemoveVote,
                    selectPoll = selectPoll,
                    onClosePoll = onClosePoll,
                    onAddPollOption = onAddPollOption,
                    onGiphyActionClick = onGiphyActionClick,
                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                    onQuotedMessageClick = onQuotedMessageClick,
                    onUserAvatarClick = onUserAvatarClick,
                    onMessageLinkClick = onLinkClick,
                    onUserMentionClick = onUserMentionClick,
                    onAddAnswer = onAddAnswer,
                )
            }
        } else {
            DefaultMessageItem(
                messageItem = messageItem,
                messageContentFactory = messageContentFactory,
                reactionSorting = reactionSorting,
                onLongItemClick = onLongItemClick,
                onReactionsClick = onReactionsClick,
                onThreadClick = onThreadClick,
                onPollUpdated = onPollUpdated,
                onCastVote = onCastVote,
                onRemoveVote = onRemoveVote,
                selectPoll = selectPoll,
                onClosePoll = onClosePoll,
                onAddPollOption = onAddPollOption,
                onGiphyActionClick = onGiphyActionClick,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onQuotedMessageClick = onQuotedMessageClick,
                onUserAvatarClick = { onUserAvatarClick?.invoke(messageItem.message.user) },
                onLinkClick = onLinkClick,
                onUserMentionClick = onUserMentionClick,
                onAddAnswer = onAddAnswer,
            )
        }
    },
    typingIndicatorContent: @Composable LazyItemScope.(TypingItemState) -> Unit = { typingItem ->
        with(ChatTheme.componentFactory) {
            MessageListTypingIndicatorItemContent(typingItem = typingItem)
        }
    },
    emptyThreadPlaceholderItemContent: @Composable LazyItemScope.(
        EmptyThreadPlaceholderItemState,
    ) -> Unit = { emptyThreadPlaceholderItem ->
        with(ChatTheme.componentFactory) {
            MessageListEmptyThreadPlaceholderItemContent(emptyThreadPlaceholderItem = emptyThreadPlaceholderItem)
        }
    },
    startOfTheChannelItemState: @Composable LazyItemScope.(
        StartOfTheChannelItemState,
    ) -> Unit = { startOfTheChannelItem ->
        with(ChatTheme.componentFactory) {
            MessageListStartOfTheChannelItemContent(startOfTheChannelItem = startOfTheChannelItem)
        }
    },
) {
    when (messageListItemState) {
        is DateSeparatorItemState -> dateSeparatorContent(messageListItemState)
        is ThreadDateSeparatorItemState -> threadSeparatorContent(messageListItemState)
        is SystemMessageItemState -> systemMessageContent(messageListItemState)
        is ModeratedMessageItemState -> moderatedMessageContent(messageListItemState)
        is MessageItemState -> messageItemContent(messageListItemState)
        is TypingItemState -> typingIndicatorContent(messageListItemState)
        is EmptyThreadPlaceholderItemState -> emptyThreadPlaceholderItemContent(messageListItemState)
        is UnreadSeparatorItemState -> unreadSeparatorContent(messageListItemState)
        is StartOfTheChannelItemState -> startOfTheChannelItemState(messageListItemState)
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
            color = ChatTheme.messageDateSeparatorTheme.backgroundColor,
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(vertical = 2.dp, horizontal = 16.dp)
                    .testTag("Stream_MessageDateSeparator"),
                text = ChatTheme.dateFormatter.formatRelativeDate(dateSeparator.date),
                style = ChatTheme.messageDateSeparatorTheme.textStyle,
            )
        }
    }
}

/**
 * Represents an unread separator item that shows whenever there are unread messages in the channel.
 *
 * @param unreadSeparatorItemState The data used to show the separator text.
 */
@Composable
internal fun DefaultMessageUnreadSeparatorContent(unreadSeparatorItemState: UnreadSeparatorItemState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.messageUnreadSeparatorTheme.backgroundColor)
            .testTag("Stream_UnreadMessagesBadge"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp),
            text = LocalContext.current.resources.getString(
                R.string.stream_compose_message_list_unread_separator,
            ),
            style = ChatTheme.messageUnreadSeparatorTheme.textStyle,
        )
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
            modifier = Modifier
                .padding(vertical = ChatTheme.dimens.threadSeparatorTextVerticalPadding)
                .testTag("Stream_RepliesCount"),
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
 * The default Moderated message content.
 *
 * @param moderatedMessageItemState The moderated message item.
 */
@Composable
internal fun DefaultMessageModeratedContent(moderatedMessageItemState: ModeratedMessageItemState) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        text = moderatedMessageItemState.message.text
            .takeUnless { it.isBlank() }
            ?: stringResource(id = R.string.stream_compose_message_moderated),
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
 * @param onCastVote Handler for casting a vote on an option.
 * @param onRemoveVote Handler for removing a vote on an option.
 * @param onClosePoll Handler for closing a poll.
 * @param onGiphyActionClick Handler when the user selects a Giphy action.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onLinkClick Handler for clicking on a link in the message.
 * @param onUserAvatarClick Handler when users avatar is clicked.
 * @param onMediaGalleryPreviewResult Handler when the user receives a result from the Media Gallery Preview.
 */
@Suppress("LongParameterList")
@Composable
internal fun DefaultMessageItem(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    messageContentFactory: MessageContentFactory = ChatTheme.messageContentFactory,
    onLongItemClick: (Message) -> Unit,
    onReactionsClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit,
    onGiphyActionClick: (GiphyAction) -> Unit,
    onLinkClick: ((Message, String) -> Unit)? = null,
    onPollUpdated: (Message, Poll) -> Unit = { _, _ -> },
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit = { _, _, _ -> },
    onCastVote: (Message, Poll, Option) -> Unit,
    onAddPollOption: (poll: Poll, option: String) -> Unit = { _, _ -> },
    onRemoveVote: (Message, Poll, Vote) -> Unit,
    selectPoll: (Message, Poll, PollSelectionType) -> Unit,
    onClosePoll: (String) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    onUserAvatarClick: () -> Unit,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onUserMentionClick: (User) -> Unit = {},
) {
    MessageItem(
        messageItem = messageItem,
        messageContentFactory = messageContentFactory,
        reactionSorting = reactionSorting,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onThreadClick = onThreadClick,
        onPollUpdated = onPollUpdated,
        onCastVote = onCastVote,
        onRemoveVote = onRemoveVote,
        selectPoll = selectPoll,
        onClosePoll = onClosePoll,
        onAddPollOption = onAddPollOption,
        onGiphyActionClick = onGiphyActionClick,
        onQuotedMessageClick = onQuotedMessageClick,
        onUserAvatarClick = onUserAvatarClick,
        onLinkClick = onLinkClick,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        onUserMentionClick = onUserMentionClick,
        onAddAnswer = onAddAnswer,
    )
}
