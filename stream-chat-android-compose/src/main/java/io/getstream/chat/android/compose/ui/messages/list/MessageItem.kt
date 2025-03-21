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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import io.getstream.chat.android.client.utils.message.belongsToThread
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.client.utils.message.isPinned
import io.getstream.chat.android.client.utils.message.isPoll
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.components.messages.MessageContent
import io.getstream.chat.android.compose.ui.components.messages.MessageHeaderLabel
import io.getstream.chat.android.compose.ui.components.messages.MessageReactions
import io.getstream.chat.android.compose.ui.components.messages.PollMessageContent
import io.getstream.chat.android.compose.ui.components.messages.factory.MessageContentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.isEmojiOnlyWithoutBubble
import io.getstream.chat.android.compose.ui.util.isErrorOrFailed
import io.getstream.chat.android.compose.ui.util.isUploading
import io.getstream.chat.android.compose.util.extensions.canReplyToMessage
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
import io.getstream.chat.android.ui.common.utils.extensions.initials
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
 * @param leadingContent The content shown at the start of a message list item.
 * @param headerContent The content shown at the top of a message list item.
 * @param centerContent The content shown at the center of a message list item.
 * @param footerContent The content shown at the bottom of a message list item.
 * @param trailingContent The content shown at the end of a message list item.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun MessageItem(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    messageContentFactory: MessageContentFactory = ChatTheme.messageContentFactory,
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
    onReply: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    leadingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageItemLeadingContent(
                messageItem = messageItem,
                onUserAvatarClick = onUserAvatarClick,
            )
        }
    },
    headerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageItemHeaderContent(
                messageItem = messageItem,
                reactionSorting = reactionSorting,
                onReactionsClick = onReactionsClick,
            )
        }
    },
    centerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        if (messageContentFactory == MessageContentFactory.Deprecated) {
            with(ChatTheme.componentFactory) {
                MessageItemCenterContent(
                    messageItem = messageItem,
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
            }
        } else {
            DefaultMessageItemCenterContent(
                messageItem = messageItem,
                onLongItemClick = onLongItemClick,
                messageContentFactory = messageContentFactory,
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
        }
    },
    footerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageItemFooterContent(
                messageItem = messageItem,
            )
        }
    },
    trailingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageItemTrailingContent(
                messageItem = messageItem,
            )
        }
    },
    swipeToReplyContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            SwipeToReplyContent()
        }
    },
) {
    val message = messageItem.message
    val focusState = messageItem.focusState
    val haptic = LocalHapticFeedback.current

    val clickModifier = if (message.isDeleted()) {
        Modifier
    } else {
        Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { if (message.belongsToThread()) onThreadClick(message) },
            onLongClick = {
                if (!message.isUploading()) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongItemClick(message)
                }
            },
        )
    }

    val backgroundColor = when (focusState is MessageFocused || message.isPinned(ChatTheme.timeProvider)) {
        true -> ChatTheme.colors.highlight
        else -> ChatTheme.colors.highlight.copy(alpha = 0f) // Ensures a smooth fade-out without unwanted color shifts.
    }

    val color by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = HighlightFadeOutDurationMillis),
    )

    val messageAlignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)
    val description = stringResource(id = R.string.stream_compose_cd_message_item)
    val isSwipable = ChatTheme.messageOptionsTheme.optionVisibility
        .canReplyToMessage(
            message = message,
            ownCapabilities = messageItem.ownCapabilities,
        )

    SwipeToReply(
        modifier = modifier,
        onReply = { onReply(message) },
        isSwipeable = { isSwipable },
        swipeToReplyContent = swipeToReplyContent,
    ) {
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
                    .then(clickModifier)
                    .testTag("Stream_MessageCell"),
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

    @Suppress("ComplexCondition")
    if (!messageItem.isMine && (
            messageItem.showMessageFooter ||
                messageItem.groupPosition.contains(MessagePosition.BOTTOM) ||
                messageItem.groupPosition.contains(MessagePosition.NONE)
            )
    ) {
        ChatTheme.componentFactory.Avatar(
            modifier = modifier.testTag("Stream_UserAvatar"),
            imageUrl = messageItem.message.user.image,
            initials = messageItem.message.user.initials,
            shape = ChatTheme.shapes.avatar,
            textStyle = ChatTheme.typography.captionBold,
            placeholderPainter = null,
            contentDescription = messageItem.message.user.name,
            initialsAvatarOffset = DpOffset.Zero,
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
                        .clickable(bounded = false) {
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
    messageContentFactory: MessageContentFactory,
) {
    val message = messageItem.message
    when {
        message.isUploading() -> {
            if (messageContentFactory == MessageContentFactory.Deprecated) {
                ChatTheme.componentFactory.MessageFooterUploadingContent(
                    modifier = Modifier.align(End),
                    messageItem = messageItem,
                )
            } else {
                messageContentFactory.UploadingFooterContent(
                    modifier = Modifier.align(End),
                    messageItem = messageItem,
                )
            }
        }

        message.isDeleted() && messageItem.deletedMessageVisibility == DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER -> {
            if (messageContentFactory == MessageContentFactory.Deprecated) {
                ChatTheme.componentFactory.MessageFooterOnlyVisibleToYouContent(
                    messageItem = messageItem,
                )
            } else {
                messageContentFactory.OwnedMessageVisibilityContent(messageItem = messageItem)
            }
        }

        else -> if (messageContentFactory == MessageContentFactory.Deprecated) {
            ChatTheme.componentFactory.MessageFooterContent(messageItem = messageItem)
        } else {
            messageContentFactory.MessageFooterContent(messageItem = messageItem)
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
    messageContentFactory: MessageContentFactory = ChatTheme.messageContentFactory,
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
            messageContentFactory = messageContentFactory,
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

            ChatTheme.componentFactory.MessageFailedIcon(
                modifier = Modifier
                    .size(24.dp)
                    .align(BottomEnd),
                message = message,
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
    messageContentFactory: MessageContentFactory = ChatTheme.messageContentFactory,
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

    val messageTheme = if (ownsMessage) ChatTheme.ownMessageTheme else ChatTheme.otherMessageTheme
    val messageBubbleShape = getMessageBubbleShape(position = position, ownsMessage = ownsMessage)
    val messageBubbleColor = getMessageBubbleColor(message = message, ownsMessage = ownsMessage)

    val content = @Composable {
        MessageContent(
            message = message,
            messageContentFactory = messageContentFactory,
            currentUser = messageItem.currentUser,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
        )
    }
    if (!messageItem.isErrorOrFailed()) {
        ChatTheme.componentFactory.MessageBubble(
            modifier = modifier,
            message = message,
            shape = messageBubbleShape,
            color = messageBubbleColor,
            border = messageTheme.backgroundBorder,
            contentPadding = messageTheme.contentPadding.values,
            content = content,
        )
    } else {
        Box(modifier = modifier) {
            ChatTheme.componentFactory.MessageBubble(
                modifier = Modifier.padding(end = 12.dp),
                message = message,
                shape = messageBubbleShape,
                color = messageBubbleColor,
                border = BorderStroke(1.dp, ChatTheme.colors.borders),
                contentPadding = PaddingValues(),
                content = content,
            )

            ChatTheme.componentFactory.MessageFailedIcon(
                modifier = Modifier
                    .size(24.dp)
                    .align(BottomEnd)
                    .testTag("Stream_MessageFailedIcon"),
                message = message,
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
    val theme = if (ownsMessage) ChatTheme.ownMessageTheme else ChatTheme.otherMessageTheme
    return when {
        position.contains(MessagePosition.TOP) -> theme.backgroundShapes.top
        position.contains(MessagePosition.MIDDLE) -> theme.backgroundShapes.middle
        position.contains(MessagePosition.BOTTOM) -> theme.backgroundShapes.bottom
        else -> theme.backgroundShapes.none
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
    val theme = if (ownsMessage) ChatTheme.ownMessageTheme else ChatTheme.otherMessageTheme
    return when {
        message.isGiphyEphemeral() -> ChatTheme.colors.giphyMessageBackground
        message.isDeleted() -> theme.deletedBackgroundColor
        else -> theme.backgroundColor
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
    messageContentFactory: MessageContentFactory,
    onLongItemClick: (Message) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    onUserMentionClick: (User) -> Unit = {},
    onLinkClick: ((Message, String) -> Unit)? = null,
) {
    Column {
        if (messageContentFactory == MessageContentFactory.Deprecated) {
            val quotedMessage = message.replyTo
            if (quotedMessage != null) {
                ChatTheme.componentFactory.MessageQuotedContent(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    message = quotedMessage,
                    currentUser = currentUser,
                    replyMessage = message,
                    onLongItemClick = onLongItemClick,
                    onQuotedMessageClick = onQuotedMessageClick,
                )
            }

            ChatTheme.componentFactory.MessageTextContent(
                message = message,
                currentUser = currentUser,
                onLongItemClick = onLongItemClick,
                onLinkClick = onLinkClick,
                onUserMentionClick = onUserMentionClick,
            )
        } else {
            messageContentFactory.QuotedMessageContent(
                message = message,
                currentUser = currentUser,
                onLongItemClick = onLongItemClick,
                onQuotedMessageClick = onQuotedMessageClick,
            )

            messageContentFactory.MessageTextContent(
                message = message,
                currentUser = currentUser,
                onLongItemClick = onLongItemClick,
                onLinkClick = onLinkClick,
                onUserMentionClick = onUserMentionClick,
            )
        }
    }
}

/**
 * Represent a swipe to reply content.
 *
 * @param modifier Modifier for styling.
 * @param onReply Handler when the user swipes to reply.
 * @param isSwipeable Handler to determine if the message is swipeable.
 * @param swipeToReplyContent The content to show when swiping to reply.
 * @param content The swipeable content to show when not swiping to reply.
 */
@Composable
private fun SwipeToReply(
    modifier: Modifier = Modifier,
    onReply: () -> Unit = {},
    isSwipeable: () -> Boolean = { true },
    swipeToReplyContent: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    var swipeToReplyWidth by remember { mutableFloatStateOf(0f) }
    var rowWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .onSizeChanged { swipeToReplyWidth = it.width.toFloat() }
                .offset {
                    val roundToInt = swipeToReplyWidth.roundToInt()
                    IntOffset(
                        (offset.value.roundToInt() - roundToInt)
                            .coerceIn(-roundToInt, roundToInt),
                        0,
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            swipeToReplyContent()
        }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .onSizeChanged { rowWidth = it.width.toFloat() }
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(swipeToReplyWidth) {
                    if (isSwipeable()) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    val newOffset = (offset.value + dragAmount)
                                        .coerceIn(0f, maxOf((rowWidth / 2), swipeToReplyWidth))
                                    offset.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offset.value >= swipeToReplyWidth) {
                                        view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                                        onReply()
                                    }
                                    offset.animateTo(0f)
                                }
                            },
                        )
                    }
                },
        ) {
            content()
        }
    }
}

/**
 * Represents the time the highlight fade out transition will take.
 */
public const val HighlightFadeOutDurationMillis: Int = 2000
