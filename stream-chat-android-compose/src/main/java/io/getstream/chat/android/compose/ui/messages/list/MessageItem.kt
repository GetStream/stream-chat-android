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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.message.belongsToThread
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.client.utils.message.isPinned
import io.getstream.chat.android.client.utils.message.isPoll
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.components.messages.MessageBubble
import io.getstream.chat.android.compose.ui.components.messages.MessageContent
import io.getstream.chat.android.compose.ui.components.messages.MessageFooter
import io.getstream.chat.android.compose.ui.components.messages.MessageHeaderLabel
import io.getstream.chat.android.compose.ui.components.messages.MessageReactions
import io.getstream.chat.android.compose.ui.components.messages.MessageText
import io.getstream.chat.android.compose.ui.components.messages.OwnedMessageVisibilityContent
import io.getstream.chat.android.compose.ui.components.messages.PollMessageContent
import io.getstream.chat.android.compose.ui.components.messages.QuotedMessage
import io.getstream.chat.android.compose.ui.components.messages.UploadingFooter
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isEmojiOnlyWithoutBubble
import io.getstream.chat.android.compose.ui.util.isErrorOrFailed
import io.getstream.chat.android.compose.ui.util.isUploading
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageFocused
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType

/**
 * The default message container for all messages in the Conversation/Messages screen.
 *
 * It shows the avatar and the message details, which can have a header (reactions), the content which
 * can be a text message, file or image attachment, or a custom attachment and the footer, which can
 * be a deleted message footer (if we own the message) or the default footer, which contains a timestamp
 * or the thread information.
 *
 * It also allows for long click and thread click events.
 *
 * @param messageItem The message item to show, which holds the message and the group position, if the message is in
 * a group of messages from the same user.
 * @param reactionSorting The sorting for the reactions, if we have any.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param modifier Modifier for styling.
 * @param onReactionsClick Handler when the user taps on message reactions.
 * @param onThreadClick Handler for thread clicks, if this message has a thread going.
 * @param onCastVote Handler for casting a vote on an option.
 * @param onRemoveVote Handler for removing a vote on an option.
 * @param selectPoll Handler for selecting a poll.
 * @param onAddAnswer Handler for adding an answer to a poll.
 * @param onClosePoll Handler for closing a poll.
 * @param onAddPollOption Handler for adding a poll option.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onUserAvatarClick Handler when users avatar is clicked.
 * @param onLinkClick Handler for clicking on a link in the message.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param leadingContent The content shown at the start of a message list item. By default, we provide
 * [DefaultMessageItemLeadingContent], which shows a user avatar if the message doesn't belong to the
 * current user.
 * @param headerContent The content shown at the top of a message list item. By default, we provide
 * [DefaultMessageItemHeaderContent], which shows a list of reactions for the message.
 *  @param centerContent The content shown at the center of a message list item. By default, we provide
 * [DefaultMessageItemCenterContent], which shows the message bubble with text and attachments.
 * @param footerContent The content shown at the bottom of a message list item. By default, we provide
 * [DefaultMessageItemFooterContent], which shows the information like thread participants, upload status, etc.
 * @param trailingContent The content shown at the end of a message list item. By default, we provide
 * [DefaultMessageItemTrailingContent], which adds an extra spacing to the end of the message list item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun MessageItem(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
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
    onUserAvatarClick: (() -> Unit)? = null,
    onLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    leadingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        DefaultMessageItemLeadingContent(
            messageItem = it,
            onUserAvatarClick = onUserAvatarClick,
        )
    },
    headerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemHeaderContent(
            messageItem = it,
            reactionSorting = reactionSorting,
            onReactionsClick = onReactionsClick,
        )
    },
    centerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemCenterContent(
            messageItem = it,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onGiphyActionClick = onGiphyActionClick,
            onQuotedMessageClick = onQuotedMessageClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
            onPollUpdated = onPollUpdated,
            onCastVote = onCastVote,
            onRemoveVote = onRemoveVote,
            selectPoll = selectPoll,
            onAddAnswer = onAddAnswer,
            onClosePoll = onClosePoll,
            onAddPollOption = onAddPollOption,
        )
    },
    footerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemFooterContent(messageItem = it)
    },
    trailingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        DefaultMessageItemTrailingContent(messageItem = it)
    },
) {
    val message = messageItem.message
    val focusState = messageItem.focusState

    val clickModifier = if (message.isDeleted()) {
        Modifier
    } else {
        Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { if (message.belongsToThread()) onThreadClick(message) },
            onLongClick = { if (!message.isUploading()) onLongItemClick(message) },
        )
    }

    val backgroundColor = when (focusState is MessageFocused || message.isPinned(ChatTheme.timeProvider)) {
        true -> ChatTheme.colors.highlight
        else -> Color.Transparent
    }
    val shouldAnimateBackground = !message.pinned && focusState != null

    val color = if (shouldAnimateBackground) {
        animateColorAsState(
            targetValue = backgroundColor,
            animationSpec = tween(
                durationMillis = if (focusState is MessageFocused) {
                    AnimationConstants.DefaultDurationMillis
                } else {
                    HighlightFadeOutDurationMillis
                },
            ),
        ).value
    } else {
        backgroundColor
    }

    val messageAlignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)
    val description = stringResource(id = R.string.stream_compose_cd_message_item)

    Box(
        modifier = Modifier
            .testTag("Stream_MessageItem")
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = color)
            .semantics { contentDescription = description },
        contentAlignment = messageAlignment.itemAlignment,
    ) {
        Row(
            modifier
                .widthIn(max = 300.dp)
                .then(clickModifier),
        ) {
            leadingContent(messageItem)

            Column(horizontalAlignment = messageAlignment.contentAlignment) {
                headerContent(messageItem)

                centerContent(messageItem)

                footerContent(messageItem)
            }

            trailingContent(messageItem)
        }
    }
}

/**
 * Represents the default content shown at the start of the message list item.
 *
 * By default, we show a user avatar if the message doesn't belong to the current user.
 *
 * @param messageItem The message item to show the content for.
 */
@Composable
internal fun RowScope.DefaultMessageItemLeadingContent(
    messageItem: MessageItemState,
    onUserAvatarClick: (() -> Unit)? = null,
) {
    val modifier = Modifier
        .padding(start = 8.dp, end = 8.dp)
        .size(24.dp)
        .align(Alignment.Bottom)

    if (!messageItem.isMine && (
            messageItem.showMessageFooter || messageItem.groupPosition.contains(MessagePosition.BOTTOM) || messageItem.groupPosition.contains(
                MessagePosition.NONE,
            )
            )
    ) {
        UserAvatar(
            modifier = modifier,
            user = messageItem.message.user,
            textStyle = ChatTheme.typography.captionBold,
            showOnlineIndicator = false,
            onClick = onUserAvatarClick,
        )
    } else {
        Spacer(modifier = modifier)
    }
}

/**
 * Represents the default content shown at the top of the message list item.
 *
 * By default, we show if the message is pinned and a list of reactions for the message.
 *
 * @param messageItem The message item to show the content for.
 * @param reactionSorting The sorting for the reactions, if we have any.
 * @param onReactionsClick Handler when the user taps on message reactions.
 */
@Suppress("LongMethod")
@Composable
internal fun DefaultMessageItemHeaderContent(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    onReactionsClick: (Message) -> Unit = {},
) {
    val message = messageItem.message
    val currentUser = messageItem.currentUser

    if (message.isPinned(ChatTheme.timeProvider)) {
        val pinnedByUser = if (message.pinnedBy?.id == currentUser?.id) {
            stringResource(id = R.string.stream_compose_message_list_you)
        } else {
            message.pinnedBy?.name
        }

        val pinnedByText = if (pinnedByUser != null) {
            stringResource(id = R.string.stream_compose_pinned_to_channel_by, pinnedByUser)
        } else {
            null
        }

        MessageHeaderLabel(
            painter = painterResource(id = R.drawable.stream_compose_ic_message_pinned),
            text = pinnedByText,
        )
    }

    if (message.showInChannel) {
        val alsoSendToChannelTextRes = if (messageItem.isInThread) {
            R.string.stream_compose_also_sent_to_channel
        } else {
            R.string.stream_compose_replied_to_thread
        }

        MessageHeaderLabel(
            painter = painterResource(id = R.drawable.stream_compose_ic_thread),
            text = stringResource(alsoSendToChannelTextRes),
        )
    }

    if (!message.isDeleted()) {
        val ownReactions = message.ownReactions
        val reactionGroups = message.reactionGroups.ifEmpty { return }
        val iconFactory = ChatTheme.reactionIconFactory
        reactionGroups.filter { iconFactory.isReactionSupported(it.key) }.takeIf { it.isNotEmpty() }?.toList()
            ?.sortedWith { o1, o2 -> reactionSorting.compare(o1.second, o2.second) }?.map { (type, _) ->
                val isSelected = ownReactions.any { it.type == type }
                val reactionIcon = iconFactory.createReactionIcon(type)
                ReactionOptionItemState(
                    painter = reactionIcon.getPainter(isSelected),
                    type = type,
                )
            }?.let { options ->
                MessageReactions(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false),
                        ) {
                            onReactionsClick(message)
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    options = options,
                )
            }
    }
}

/**
 * Represents the default content shown at the bottom of the message list item.
 *
 * By default, the following can be shown in the footer:
 * - uploading status
 * - thread participants
 * - message timestamp
 *
 * @param messageItem The message item to show the content for.
 */
@Composable
internal fun ColumnScope.DefaultMessageItemFooterContent(
    messageItem: MessageItemState,
) {
    val message = messageItem.message
    when {
        message.isUploading() -> {
            UploadingFooter(
                modifier = Modifier.align(End),
                message = message,
            )
        }

        message.isDeleted() && messageItem.deletedMessageVisibility == DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER -> {
            OwnedMessageVisibilityContent(message = message)
        }

        else -> {
            MessageFooter(messageItem = messageItem)
        }
    }

    val position = messageItem.groupPosition
    val spacerSize =
        if (position.contains(MessagePosition.NONE) || position.contains(MessagePosition.BOTTOM)) 4.dp else 2.dp

    Spacer(Modifier.size(spacerSize))
}

/**
 * Represents the default content shown at the end of the message list item.
 *
 * By default, we show an extra spacing at the end of the message list item.
 *
 * @param messageItem The message item to show the content for.
 */
@Composable
internal fun DefaultMessageItemTrailingContent(
    messageItem: MessageItemState,
) {
    if (messageItem.isMine) {
        Spacer(modifier = Modifier.width(8.dp))
    }
}

/**
 * Represents the default content shown at the center of the message list item.
 *
 * By default, we show a message bubble with attachments or emoji stickers if message is emoji only.
 *
 * @param messageItem The message item to show the content for.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onLinkClick Handler for clicking on a link in the message.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param onCastVote Handler when a user cast a vote on an option.
 * @param onRemoveVote Handler when a user cast a remove on an option.
 * @param onClosePoll Handler when a user close a poll.
 * @param onAddPollOption Handler when a user add a poll option.
 */
@Suppress("LongParameterList")
@Composable
public fun DefaultMessageItemCenterContent(
    modifier: Modifier = Modifier,
    messageItem: MessageItemState,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onPollUpdated: (Message, Poll) -> Unit,
    onCastVote: (Message, Poll, Option) -> Unit,
    onRemoveVote: (Message, Poll, Vote) -> Unit,
    selectPoll: (Message, Poll, PollSelectionType) -> Unit,
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit,
    onClosePoll: (String) -> Unit,
    onAddPollOption: (poll: Poll, option: String) -> Unit,
) {
    val finalModifier = modifier.widthIn(max = ChatTheme.dimens.messageItemMaxWidth)
    if (messageItem.message.isPoll()) {
        val poll = messageItem.message.poll
        LaunchedEffect(key1 = poll) {
            if (poll != null) {
                onPollUpdated.invoke(messageItem.message, poll)
            }
        }

        PollMessageContent(
            modifier = finalModifier,
            messageItem = messageItem,
            onCastVote = onCastVote,
            onRemoveVote = onRemoveVote,
            selectPoll = selectPoll,
            onClosePoll = onClosePoll,
            onAddPollOption = onAddPollOption,
            onLongItemClick = onLongItemClick,
            onAddAnswer = onAddAnswer,
        )
    } else if (messageItem.message.isEmojiOnlyWithoutBubble()) {
        EmojiMessageContent(
            modifier = finalModifier,
            messageItem = messageItem,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    } else {
        RegularMessageContent(
            modifier = finalModifier,
            messageItem = messageItem,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
        )
    }
}

/**
 * Message content when the message consists only of emoji.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onMediaGalleryPreviewResult Handler used when the user selects an option in the Media Gallery Preview screen.
 */
@Composable
public fun EmojiMessageContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val message = messageItem.message

    if (!messageItem.isErrorOrFailed()) {
        MessageContent(
            message = message,
            currentUser = messageItem.currentUser,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    } else {
        Box(modifier = modifier) {
            MessageContent(
                message = message,
                currentUser = messageItem.currentUser,
                onLongItemClick = onLongItemClick,
                onGiphyActionClick = onGiphyActionClick,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onQuotedMessageClick = onQuotedMessageClick,
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(BottomEnd),
                painter = painterResource(id = R.drawable.stream_compose_ic_error),
                contentDescription = null,
                tint = ChatTheme.colors.errorAccent,
            )
        }
    }
}

/**
 * Message content for messages which consist of more than just emojis.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onLinkClick Handler for clicking on a link in the message.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 */
@Composable
public fun RegularMessageContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val message = messageItem.message
    val position = messageItem.groupPosition
    val ownsMessage = messageItem.isMine

    val messageBubblePadding = when (ownsMessage) {
        true -> ChatTheme.ownMessageTheme.contentPadding
        else -> ChatTheme.otherMessageTheme.contentPadding
    }

    val messageBubbleShape = getMessageBubbleShape(position = position, ownsMessage = ownsMessage)

    val messageBubbleColor = getMessageBubbleColor(message = message, ownsMessage = ownsMessage)

    val messageBubbleBorder = when (ownsMessage) {
        true -> ChatTheme.ownMessageTheme.backgroundBorder
        else -> ChatTheme.otherMessageTheme.backgroundBorder
    }

    if (!messageItem.isErrorOrFailed()) {
        MessageBubble(
            modifier = modifier.padding(messageBubblePadding),
            shape = messageBubbleShape,
            color = messageBubbleColor,
            border = messageBubbleBorder,
            content = {
                MessageContent(
                    message = message,
                    currentUser = messageItem.currentUser,
                    onLongItemClick = onLongItemClick,
                    onGiphyActionClick = onGiphyActionClick,
                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                    onQuotedMessageClick = onQuotedMessageClick,
                    onLinkClick = onLinkClick,
                    onUserMentionClick = onUserMentionClick,
                )
            },
        )
    } else {
        Box(modifier = modifier) {
            MessageBubble(
                modifier = Modifier.padding(end = 12.dp),
                shape = messageBubbleShape,
                color = messageBubbleColor,
                content = {
                    MessageContent(
                        message = message,
                        currentUser = messageItem.currentUser,
                        onLongItemClick = onLongItemClick,
                        onGiphyActionClick = onGiphyActionClick,
                        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                        onQuotedMessageClick = onQuotedMessageClick,
                        onLinkClick = onLinkClick,
                    )
                },
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(BottomEnd),
                painter = painterResource(id = R.drawable.stream_compose_ic_error),
                contentDescription = null,
                tint = ChatTheme.colors.errorAccent,
            )
        }
    }
}

/**
 * Determines the shape of the message bubble based on the message position and ownership.
 *
 * @param position The position of the message in the group (top, middle, etc.).
 * @param ownsMessage Indicates if the current user owns the message.
 * @return A shape for the message bubble.
 */
@Composable
private fun getMessageBubbleShape(position: List<MessagePosition>, ownsMessage: Boolean): Shape {
    val isTopOrMiddleInGroup = position.contains(MessagePosition.TOP) || position.contains(MessagePosition.MIDDLE)
    return when (ownsMessage) {
        true -> when (isTopOrMiddleInGroup) {
            true -> ChatTheme.ownMessageTheme.backgroundShapes.regular
            else -> ChatTheme.ownMessageTheme.backgroundShapes.bottom
        }
        else -> when (isTopOrMiddleInGroup) {
            true -> ChatTheme.otherMessageTheme.backgroundShapes.regular
            else -> ChatTheme.otherMessageTheme.backgroundShapes.bottom
        }
    }
}

/**
 * Determines the background color of the message bubble based on the message content and ownership.
 *
 * @param message The message data.
 * @param ownsMessage Indicates if the current user owns the message.
 * @return A color for the message bubble.
 */
@Composable
private fun getMessageBubbleColor(message: Message, ownsMessage: Boolean): Color {
    return when {
        message.isGiphyEphemeral() -> ChatTheme.colors.giphyMessageBackground
        message.isDeleted() -> when (ownsMessage) {
            true -> ChatTheme.ownMessageTheme.deletedBackgroundColor
            else -> ChatTheme.otherMessageTheme.deletedBackgroundColor
        }
        else -> when (ownsMessage) {
            true -> ChatTheme.ownMessageTheme.backgroundColor
            else -> ChatTheme.otherMessageTheme.backgroundColor
        }
    }
}

/**
 * The default text message content. It holds the quoted message in case there is one.
 *
 * @param message The message to show.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onLinkClick Handler for link clicks.
 */
@Composable
internal fun DefaultMessageTextContent(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    onUserMentionClick: (User) -> Unit = {},
    onLinkClick: ((Message, String) -> Unit)? = null,
) {
    val quotedMessage = message.replyTo

    Column {
        if (quotedMessage != null) {
            QuotedMessage(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                message = quotedMessage,
                currentUser = currentUser,
                replyMessage = message,
                onLongItemClick = { onLongItemClick(message) },
                onQuotedMessageClick = onQuotedMessageClick,
            )
        }
        MessageText(
            message = message,
            currentUser = currentUser,
            onLongItemClick = onLongItemClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
        )
    }
}

/**
 * Represents the time the highlight fade out transition will take.
 */
public const val HighlightFadeOutDurationMillis: Int = 1000
