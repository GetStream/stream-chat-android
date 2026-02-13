/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
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
 * The content for different message item types is provided through [ChatTheme.componentFactory],
 * allowing for customization of date separators, thread separators, system messages, moderated messages,
 * regular messages, typing indicators, empty thread placeholders, unread separators, and start of channel items.
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
 */
@Composable
public fun LazyItemScope.MessageItem(
    messageListItemState: MessageListItemState,
    reactionSorting: ReactionSorting,
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
    onReply: (Message) -> Unit = {},
) {
    with(ChatTheme.componentFactory) {
        when (messageListItemState) {
            is DateSeparatorItemState -> MessageListDateSeparatorItemContent(messageListItemState)
            is ThreadDateSeparatorItemState -> MessageListThreadDateSeparatorItemContent(messageListItemState)
            is SystemMessageItemState -> MessageListSystemItemContent(messageListItemState)
            is ModeratedMessageItemState -> MessageListModeratedItemContent(messageListItemState)
            is MessageItemState -> {
                MessageContainer(
                    messageItem = messageListItemState,
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
                    onReply = onReply,
                )
            }

            is TypingItemState -> MessageListTypingIndicatorItemContent(messageListItemState)
            is EmptyThreadPlaceholderItemState -> MessageListEmptyThreadPlaceholderItemContent(messageListItemState)
            is UnreadSeparatorItemState -> MessageListUnreadSeparatorItemContent(messageListItemState)
            is StartOfTheChannelItemState -> MessageListStartOfTheChannelItemContent(messageListItemState)
        }
    }
}

/**
 * Represents a date separator item that shows whenever messages are too far apart in time.
 *
 * @param dateSeparator The data used to show the separator text.
 */
@Composable
internal fun DefaultMessageDateSeparatorContent(dateSeparator: DateSeparatorItemState) {
    Box(
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
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
            .semantics(mergeDescendants = true) {
                testTag = "Stream_UnreadMessagesBadge"
            }
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(ChatTheme.messageUnreadSeparatorTheme.backgroundColor),
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
            .semantics(mergeDescendants = true) {}
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
